package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

private const val MaxHSheetDragItems = 5

@Composable
internal fun HSheetDragContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    onModalSheetComponentRequest: (ModalSheetComponent) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetActionsCount by remember {
        derivedStateOf {
            uiState.actionList.count()
        }
    }
    val answerActionSlots = if (uiState.isRinging) AnswerActionMultiplier else 1
    val itemsPerRow by remember(callActions) {
        derivedStateOf {
            max(1, sheetActionsCount - callActions.count() + answerActionSlots)
        }
    }

    HSheetDragContent(
        modifier = modifier,
        callActions = callActions,
        itemsPerRow = itemsPerRow,
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
        onVirtualBackgroundToggle = { onModalSheetComponentRequest(ModalSheetComponent.VirtualBackground) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HSheetDragContent(
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
    modifier: Modifier = Modifier,
    itemsPerRow: Int = MaxHSheetDragItems,
    labels: Boolean = true,
) {
    val shouldExtendLastButton = callActions.count() / itemsPerRow < 1

    val chunkedActions = remember(callActions, itemsPerRow) {
        callActions.value.chunked(itemsPerRow).flatten()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(itemsPerRow),
        verticalArrangement = Arrangement.spacedBy(HSheetDragVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(HSheetDragHorizontalPadding),
        modifier = modifier
    ) {
       itemsIndexed(
           key = { _, item -> item.id },
           span = { index, _ ->
               if (shouldExtendLastButton && index == chunkedActions.size - 1) GridItemSpan(itemsPerRow - (chunkedActions.size % itemsPerRow - 1))
               else  GridItemSpan(1)
           },
           items = chunkedActions
       ) { _, callAction, ->
           CallSheetItem(
               callAction = callAction,
               modifier = Modifier.animateItemPlacement(),
               label = labels,
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
               onVirtualBackgroundToggle = onVirtualBackgroundToggle
           )
       }
    }
}