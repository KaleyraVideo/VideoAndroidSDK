package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetCallActionModifier
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

private const val MaxHSheetDragItems = 5

@Composable
internal fun HSheetDragContent(
    modifier: Modifier = Modifier,
    itemsPerRow: Int = MaxHSheetDragItems,
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
    onVirtualBackgroundClick: () -> Unit
) {
    val actionsCount = callActions.count()
    val hasOneRow = actionsCount < itemsPerRow
    VerticalGrid(
        columns = SimpleGridCells.Fixed(itemsPerRow),
        horizontalArrangement = Arrangement.spacedBy(HSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(HSheetDragVerticalPadding),
        modifier = modifier
    ) {
        callActions.value.fastForEachIndexed { index, callAction ->
            val isLastIndex = index == actionsCount - 1
            val callActionModifier = if (isLastIndex && hasOneRow) {
                val spanCount = itemsPerRow - (actionsCount % itemsPerRow) + 1
                SheetCallActionModifier.span(spanCount)
            } else SheetCallActionModifier

            key(callAction.id) {
                CallSheetItem(
                    callAction = callAction,
                    modifier = callActionModifier,
                    label = true,
                    extended = false,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
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
}