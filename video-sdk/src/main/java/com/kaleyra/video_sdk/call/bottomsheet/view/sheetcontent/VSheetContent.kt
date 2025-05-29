package com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
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
    onAskInputPermissions: (Boolean) -> Unit,
    onNextFocusRequest: () -> FocusRequester? = { null }
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
        onMicToggle = remember(viewModel, inputPermissions) {
            lambda@{
                val micPermission = inputPermissions.micPermission ?: return@lambda
                if (micPermission.status.isGranted) viewModel.toggleMic(activity)
                else micPermission.launchPermissionRequest()
            }
        },
        onCameraToggle = remember(viewModel, inputPermissions) {
            lambda@{
                val cameraPermission = inputPermissions.cameraPermission ?: return@lambda
                if (uiState.isCameraUsageRestricted || cameraPermission.status.isGranted) viewModel.toggleCamera(activity)
                else cameraPermission.launchPermissionRequest()
            }
        },
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
        onSignatureClick = {
            onModularComponentRequest(ModularComponent.SignDocuments)
            viewModel.clearSignatureBadge()
        },
        onVirtualBackgroundToggle = { onModularComponentRequest(ModularComponent.VirtualBackground) },
        onMoreToggle = onMoreToggle,
        onNextFocusRequest = onNextFocusRequest,
        modifier = modifier
    )
}

// N.B. Keyboard navigation tests are currently excluded due to past difficulties with
// programmatic focus management in UI tests. Exercise caution when modifying this code.
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
    onSignatureClick: () -> Unit,
    onMoreToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems,
    inputPermissions: InputPermissions = InputPermissions(),
    onNextFocusRequest: () -> FocusRequester? = { null }
) {
    var showMoreAction by remember { mutableStateOf(false) }
    var moreNotificationCount by remember { mutableIntStateOf(0) }

    // Focus Requester for the fixed Answer/More button
    val answerOrMoreButtonRequester = remember { FocusRequester() }
    var actualVisibleActionCount by remember { mutableIntStateOf(0) }
    // Derived state for the index of the last focusable dynamic action
    val lastFocusableActionIndex by remember(callActions) {
        derivedStateOf {
            callActions.value
                .take(actualVisibleActionCount) // Only consider currently visible items
                .indexOfLast { it.isEnabled } // Find the last one that's interactable
        }
    }

    Column(modifier.focusGroup()) {
        Column(
            modifier = Modifier
                .focusRequester(answerOrMoreButtonRequester)
                .focusProperties {
                    // When the "Answer" or "More" button is focused and Tab is pressed, focus will exit this component.
                    next = onNextFocusRequest() ?: next
                }
        ) {
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
                        badgeCount = moreNotificationCount,
                        checked = isMoreToggled,
                        onCheckedChange = onMoreToggle,
                    )
                    Spacer(Modifier.height(SheetItemsSpacing))
                }
            }
        }

        VSheetItemsLayout(
            modifier = Modifier.focusGroup(),
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = callActions.count() > itemsPlaced
                moreNotificationCount = computeMoreActionNotificationCount(callActions, itemsPlaced)
                actualVisibleActionCount = itemsPlaced
                onActionsPlaced(itemsPlaced)
            },
            maxItems = maxActions - if (showAnswerAction || showMoreAction) 1 else 0,
            content = {
                callActions.value.fastForEachIndexed { index, callAction ->
                    key(callAction.id) {
                        CallSheetItem(
                            modifier = Modifier.let {
                                // Keyboard Accessibility Logic:
                                // This block defines the focus traversal behavior when navigating with the keyboard (e.g., pressing Tab).
                                // It ensures that focus moves predictably through the call action buttons and then to specific
                                // "Answer" or "More" buttons if they are present, or exits the component otherwise.
                                if (index == lastFocusableActionIndex) {
                                    // When on the last focusable call action button:
                                    it.focusProperties {
                                        next = if (showMoreAction || showAnswerAction) {
                                            // If either "Answer" or "More" buttons are visible, move focus to them.
                                            answerOrMoreButtonRequester
                                        } else {
                                            // Otherwise, trigger the external 'onNextFocusRequest' to exit focus from this component.
                                            onNextFocusRequest() ?: next
                                        }
                                    }
                                } else {
                                    // For all other intermediate call action buttons, focus moves to the next logical item by default.
                                    it // No specific focus properties needed, default traversal applies.
                                }
                            },
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
                            onSignatureClick = onSignatureClick,
                            onVirtualBackgroundToggle = onVirtualBackgroundToggle
                        )
                    }
                }
            }
        )
    }
}