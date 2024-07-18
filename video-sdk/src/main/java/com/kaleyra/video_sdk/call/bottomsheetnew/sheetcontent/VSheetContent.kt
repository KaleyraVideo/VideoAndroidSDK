package com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.VSheetItemsLayout
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.CallActionDefaults
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.CompactScreenMaxActions
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.call.screennew.NotifiableCallAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

private const val MaxVSheetItems = 5

@Composable
internal fun VSheetContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    isMoreToggled: Boolean,
    onMoreToggle: (Boolean) -> Unit,
    onActionsOverflow: (overflowedActions: ImmutableList<CallActionUI>) -> Unit,
    onModalSheetComponentRequest: (ModalSheetComponent) -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VSheetContent(
        callActions = uiState.actionList,
        maxActions = maxActions,
        showAnswerAction = uiState.isRinging,
        isMoreToggled = isMoreToggled,
        onActionsPlaced = { itemsPlaced ->
            val actions = uiState.actionList.value
            val dragActions = actions.takeLast(max(0, actions.count() - itemsPlaced))
            onActionsOverflow(dragActions.toImmutableList())
        },
        onAnswerClick = viewModel::accept,
        onHangUpClick = viewModel::hangUp,
        onMicToggle = remember(viewModel) { { viewModel.toggleMic(activity) } },
        onCameraToggle = remember(viewModel) { { viewModel.toggleCamera(activity) } } ,
        onScreenShareToggle = remember(viewModel) {
            { if (!viewModel.tryStopScreenShare()) onModalSheetComponentRequest(ModalSheetComponent.ScreenShare) }
        },
        onFlipCameraClick = viewModel::switchCamera,
        onAudioClick = { onModalSheetComponentRequest(ModalSheetComponent.Audio) },
        onChatClick = remember(viewModel) { { activity.unlockDevice(onUnlocked = { viewModel.showChat(activity) }) } },
        onFileShareClick = { onModalSheetComponentRequest(ModalSheetComponent.FileShare) },
        onWhiteboardClick = { onModalSheetComponentRequest(ModalSheetComponent.Whiteboard) },
        onVirtualBackgroundClick = { onModalSheetComponentRequest(ModalSheetComponent.VirtualBackground) },
        onMoreToggle = onMoreToggle,
        modifier = modifier
    )
}

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
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onMoreToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems
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
                            onHangUpClick = onHangUpClick,
                            onMicToggle = onMicToggle,
                            onCameraToggle = onCameraToggle,
                            onScreenShareToggle = onScreenShareToggle,
                            onFlipCameraClick = onFlipCameraClick,
                            onAudioClick = onAudioClick,
                            onChatClick = onChatClick,
                            onFileShareClick = onFileShareClick,
                            onWhiteboardClick = onWhiteboardClick,
                            onVirtualBackgroundClick = onVirtualBackgroundClick
                        )
                    }
                }
            }
        )
    }
}