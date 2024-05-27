package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.cheonjaeung.compose.grid.HorizontalGrid
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetCallActionModifier
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val VSheetDragHorizontalPadding = 20.dp
internal val VSheetDragVerticalPadding = SheetItemsSpacing

private const val MaxVSheetDragItems = 5

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
    val actions = remember(callActions, itemsPerColumn) {
        callActions.value.chunked(itemsPerColumn, transform = { it.reversed() }).flatten()
    }
    HorizontalGrid(
        rows = SimpleGridCells.Fixed(itemsPerColumn),
        horizontalArrangement = Arrangement.spacedBy(VSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(VSheetDragVerticalPadding),
        modifier = modifier
    ) {
        actions.fastForEach { callAction ->
            key(callAction.id) {
                CallSheetItem(
                    callAction = callAction,
                    modifier = SheetCallActionModifier,
                    label = false,
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