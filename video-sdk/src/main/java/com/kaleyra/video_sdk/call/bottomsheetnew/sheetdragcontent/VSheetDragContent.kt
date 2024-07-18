package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

internal val VSheetDragHorizontalPadding = 20.dp
internal val VSheetDragVerticalPadding = SheetItemsSpacing

private const val MaxVSheetDragItems = 5

@Composable
internal fun VSheetDragContent(
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
    val itemsPerColumn by remember(callActions) {
        derivedStateOf {
            max(1, sheetActionsCount - callActions.count() + 1)
        }
    }

    VSheetDragContent(
        modifier = modifier,
        callActions = callActions,
        itemsPerColumn = itemsPerColumn,
        onHangUpClick = viewModel::hangUp,
        onMicToggle = remember(viewModel) { { viewModel.toggleMic(activity) } },
        onCameraToggle = remember(viewModel) { { viewModel.toggleCamera(activity) } },
        onScreenShareToggle = remember(viewModel) {
            { if (!viewModel.tryStopScreenShare()) onModalSheetComponentRequest(ModalSheetComponent.ScreenShare) }
        },
        onFlipCameraClick = viewModel::switchCamera,
        onAudioClick = { onModalSheetComponentRequest(ModalSheetComponent.Audio) },
        onChatClick = remember(viewModel) { { activity.unlockDevice(onUnlocked = { viewModel.showChat(activity) }) } },
        onFileShareClick = { onModalSheetComponentRequest(ModalSheetComponent.FileShare) },
        onWhiteboardClick = { onModalSheetComponentRequest(ModalSheetComponent.Whiteboard) },
        onVirtualBackgroundClick = { onModalSheetComponentRequest(ModalSheetComponent.VirtualBackground) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VSheetDragContent(
    callActions: ImmutableList<CallActionUI>,
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
    modifier: Modifier = Modifier,
    itemsPerColumn: Int = MaxVSheetDragItems,
) {
    val chunkedActions = remember(callActions, itemsPerColumn) {
        callActions.value.chunked(itemsPerColumn, transform = { it.reversed() }).flatten()
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(itemsPerColumn),
        horizontalArrangement = Arrangement.spacedBy(VSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(VSheetDragVerticalPadding),
        modifier = modifier
    ) {
        items(key = { it.id }, items = chunkedActions) { callAction ->
            CallSheetItem(
                callAction = callAction,
                modifier = Modifier.animateItemPlacement(),
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