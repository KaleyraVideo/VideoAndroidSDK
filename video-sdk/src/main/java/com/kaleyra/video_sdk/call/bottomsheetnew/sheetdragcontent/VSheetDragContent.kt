package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val VSheetDragHorizontalPadding = 20.dp
internal val VSheetDragVerticalPadding = SheetItemsSpacing

private const val MaxVSheetDragItems = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VSheetDragContent(
    callActions: ImmutableList<CallActionUI>,
    onHangUpClick: () -> Unit,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    modifier: Modifier = Modifier,
    itemsPerColumn : Int = MaxVSheetDragItems
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
                onMicToggle = onMicToggled,
                onCameraToggle = onCameraToggled,
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