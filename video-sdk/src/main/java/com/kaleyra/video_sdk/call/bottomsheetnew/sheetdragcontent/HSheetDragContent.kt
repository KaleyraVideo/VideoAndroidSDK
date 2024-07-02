package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

private const val MaxHSheetDragItems = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HSheetDragContent(
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
               if (shouldExtendLastButton && index == chunkedActions.size - 1) GridItemSpan(itemsPerRow % chunkedActions.size + 1)
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