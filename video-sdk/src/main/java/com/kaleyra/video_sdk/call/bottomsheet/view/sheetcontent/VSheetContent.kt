package com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.NotifiableCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.view.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.VSheetItemsLayout
import com.kaleyra.video_sdk.call.callactions.view.AnswerAction
import com.kaleyra.video_sdk.call.callactions.view.CallActionDefaults
import com.kaleyra.video_sdk.call.callactions.view.MoreAction
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

private const val MaxVSheetItems = 5

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun VSheetContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    screenShareViewModel: ScreenShareViewModel = viewModel(factory = ScreenShareViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    isMoreToggled: Boolean,
    onMoreToggle: (Boolean) -> Unit,
    onActionsOverflow: (overflowedActions: ImmutableList<CallActionUI>) -> Unit,
    onModularComponentRequest: (ModularComponent) -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems,
    inputPermissions: InputPermissions = InputPermissions(),
    onAskInputPermissions: (Boolean) -> Unit
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var screenShareMode: ScreenShareAction? by remember { mutableStateOf(null) }
    screenShareMode = uiState.actionList.value.firstOrNull { it is ScreenShareAction } as? ScreenShareAction

    VSheetContent(
        callActions = uiState.actionList,
        maxActions = maxActions,
        showAnswerAction = uiState.isRinging,
        isMoreToggled = isMoreToggled,
        inputPermissions = inputPermissions,
        onActionsPlaced = { itemsPlaced ->
            val actions = uiState.actionList.value
            val dragActions = actions.takeLast(max(0, actions.count() - itemsPlaced))
            onActionsOverflow(dragActions.toImmutableList())
        },
        onAnswerClick = viewModel::accept,
        onHangUpClick = viewModel::hangUp,
        onMicToggle = remember(viewModel, inputPermissions) { lambda@ {
            val micPermission = inputPermissions.micPermission ?: return@lambda
            if (micPermission.status.isGranted) viewModel.toggleMic(activity)
            else micPermission.launchPermissionRequest()
        } },
        onCameraToggle = remember(viewModel, inputPermissions) { lambda@ {
            val cameraPermission = inputPermissions.cameraPermission ?: return@lambda
            if (uiState.isCameraUsageRestricted || cameraPermission.status.isGranted) viewModel.toggleCamera(activity)
            else cameraPermission.launchPermissionRequest()
        } } ,
        onScreenShareToggle = remember(viewModel) {
            {
                if (!viewModel.tryStopScreenShare()) {

                    when (screenShareMode) {
                        null -> Unit
                        is ScreenShareAction.UserChoice -> onModularComponentRequest(ModularComponent.ScreenShare)
                        is ScreenShareAction.App -> screenShareViewModel?.shareApplicationScreen(activity, {}, {})
                        is ScreenShareAction.WholeDevice -> {
                            onAskInputPermissions(true)
                            activity.unlockDevice(
                                onUnlocked = {
                                    screenShareViewModel?.shareDeviceScreen(
                                        activity,
                                        onScreenSharingStarted = {
                                            onAskInputPermissions(false)
                                        },
                                        onScreenSharingAborted = {
                                            onAskInputPermissions(false)
                                        }
                                    )
                                },
                                onDismiss = {
                                    onAskInputPermissions(false)
                                })
                        }
                    }
                }
            }
        },
        onFlipCameraClick = viewModel::switchCamera,
        onAudioClick = { onModularComponentRequest(ModularComponent.Audio) },
        onChatClick = remember(viewModel) { { activity.unlockDevice(onUnlocked = { viewModel.showChat(activity) }) } },
        onFileShareClick = {
            onModularComponentRequest(ModularComponent.FileShare)
            viewModel.clearFileShareBadge()
        },
        onWhiteboardClick = { onModularComponentRequest(ModularComponent.Whiteboard) },
        onVirtualBackgroundToggle = { onModularComponentRequest(ModularComponent.VirtualBackground) },
        onMoreToggle = onMoreToggle,
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun VSheetContent(
    callActions: ImmutableList<CallActionUI>,
    showAnswerAction: Boolean,
    isMoreToggled: Boolean,
    onActionsPlaced: (actionsPlaced: Int) -> Unit,
    onAnswerClick: () -> Unit,
    onHangUpClick: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onCameraToggle: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onVirtualBackgroundToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onMoreToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems,
    inputPermissions: InputPermissions = InputPermissions()
) {
    var showMoreAction by remember { mutableStateOf(false) }
    val moreNotificationCount = remember(callActions) { callActions.value.filterIsInstance<NotifiableCallAction>().sumOf { it.notificationCount } }

    Column(modifier) {
        when {
            showAnswerAction -> {
                AnswerAction(
                    onClick = onAnswerClick,
                    modifier = Modifier.size(CallActionDefaults.MinButtonSize)
                )
                Spacer(Modifier.height(SheetItemsSpacing))
            }
            showMoreAction -> {
                MoreAction(
                    badgeText = if (moreNotificationCount != 0) "$moreNotificationCount" else null,
                    checked = isMoreToggled,
                    onCheckedChange = onMoreToggle,
                )
                Spacer(Modifier.height(SheetItemsSpacing))
            }
        }

        VSheetItemsLayout(
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = callActions.count() > itemsPlaced
                onActionsPlaced(itemsPlaced)
            },
            maxItems = maxActions - if (showAnswerAction || showMoreAction) 1 else 0,
            content = {
                callActions.value.fastForEach { callAction ->
                    key(callAction.id) {
                        CallSheetItem(
                            callAction = callAction,
                            label = false,
                            extended = false,
                            inputPermissions = inputPermissions,
                            onHangUpClick = onHangUpClick,
                            onMicToggle = onMicToggle,
                            onCameraToggle = onCameraToggle,
                            onScreenShareToggle = onScreenShareToggle,
                            onFlipCameraClick = onFlipCameraClick,
                            onAudioClick = onAudioClick,
                            onChatClick = onChatClick,
                            onFileShareClick = onFileShareClick,
                            onWhiteboardClick = onWhiteboardClick,
                            onVirtualBackgroundToggle = onVirtualBackgroundToggle
                        )
                    }
                }
            }
        )
    }
}