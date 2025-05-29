package com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

internal val VSheetDragVerticalPadding = SheetItemsSpacing
internal val VSheetDragHorizontalPadding = VSheetDragVerticalPadding

private const val MaxVSheetDragItems = 5

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun VSheetDragContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    screenShareViewModel: ScreenShareViewModel = viewModel<ScreenShareViewModel>(factory = ScreenShareViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    areChildrenKeyboardFocusable: Boolean = false,
    onModularComponentRequest: (ModularComponent) -> Unit,
    inputPermissions: InputPermissions = InputPermissions(),
    onAskInputPermissions: (Boolean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
    onNextFocusRequester: () -> FocusRequester? = { null },
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetActionsCount by remember {
        derivedStateOf {
            uiState.actionList.count()
        }
    }
    val itemsPerColumn by remember(callActions) {
        derivedStateOf {
            max(1, sheetActionsCount - callActions.count() + 1)
        }
    }

    var screenShareMode: ScreenShareAction? by remember { mutableStateOf(null) }
    screenShareMode = uiState.actionList.value.firstOrNull { it is ScreenShareAction } as? ScreenShareAction

    VSheetDragContent(
        modifier = modifier,
        callActions = callActions,
        itemsPerColumn = itemsPerColumn,
        inputPermissions = inputPermissions,
        areChildrenKeyboardFocusable = areChildrenKeyboardFocusable,
        contentPadding = contentPadding,
        onNextFocusRequester = onNextFocusRequester,
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
                        is ScreenShareAction.App -> screenShareViewModel.shareApplicationScreen(activity, {}, {})
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
        onVirtualBackgroundToggle = { onModularComponentRequest(ModularComponent.VirtualBackground) }
    )
}

// N.B. Keyboard navigation tests are currently excluded due to past difficulties with
// programmatic focus management in UI tests. Exercise caution when modifying this code.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun VSheetDragContent(
    callActions: ImmutableList<CallActionUI>,
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
    areChildrenKeyboardFocusable: Boolean = false,
    modifier: Modifier = Modifier,
    itemsPerColumn: Int = MaxVSheetDragItems,
    inputPermissions: InputPermissions = InputPermissions(),
    onNextFocusRequester: () -> FocusRequester? = { null },
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val chunkedActions = remember(callActions, itemsPerColumn) {
        callActions.value.chunked(itemsPerColumn, transform = { it.reversed() }).flatten()
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(itemsPerColumn),
        horizontalArrangement = Arrangement.spacedBy(VSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(VSheetDragVerticalPadding),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        itemsIndexed(key = { _, item -> item.id }, items = chunkedActions) { index, callAction ->
            CallSheetItem(
                callAction = callAction,
                modifier = Modifier
                    .animateItem()
                    .focusProperties {
                        // Controls whether this item can receive keyboard focus.
                        // If 'false', it will be skipped during tab navigation.
                        canFocus = areChildrenKeyboardFocusable

                        // This focus property logic only applies to the last item in the grid.
                        // For other items, the default LazyHorizontalGrid traversal order (top-to-bottom, then left-to-right) will apply.
                        if (index != callActions.count() - 1) return@focusProperties

                        // When the last item in the grid is focused and 'Tab' is pressed,
                        // it will attempt to move focus to the FocusRequester provided by 'onNextFocusRequester()'.
                        // If 'onNextFocusRequester()' returns null, it falls back to the default 'next' behavior
                        // of the LazyHorizontalGrid, which typically involves looping within the grid or exiting.
                        next = onNextFocusRequester() ?: next
                    },
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