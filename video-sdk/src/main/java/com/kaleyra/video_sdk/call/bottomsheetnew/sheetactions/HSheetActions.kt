package com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetCallAction
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.HSheetItemsLayout
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.row.ReversibleRow

private const val MaxHSheetActions = 5

@Composable
internal fun HSheetActions(
    modifier: Modifier = Modifier,
    maxActions: Int = MaxHSheetActions,
    callActions: ImmutableList<CallActionUI>,
    showAnswerAction: Boolean,
    isLargeScreen: Boolean,
    onActionsPlaced: (actionsPlaced: Int) -> Unit,
    onAnswerActionClick: () -> Unit,
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
    onMoreActionClick: () -> Unit
) {
    var showMoreAction by remember { mutableStateOf(false) }

    ReversibleRow(modifier, reverseLayout = true) {
        when {
            showAnswerAction -> {
                AnswerAction(extended = isLargeScreen, onClick = onAnswerActionClick)
                Spacer(Modifier.width(SheetItemsSpacing))
            }
            showMoreAction -> {
                MoreAction(onClick = onMoreActionClick)
                Spacer(Modifier.width(SheetItemsSpacing))
            }
        }

        HSheetItemsLayout(
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = callActions.count() > itemsPlaced
                onActionsPlaced(itemsPlaced)
            },
            maxItems = maxActions - if (showAnswerAction || showMoreAction) 1 else 0,
            content = {
                callActions.value.fastForEach { callAction ->
                    key(callAction.id) {
                        SheetCallAction(
                            callAction = callAction,
                            label = false,
                            extended = isLargeScreen,
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
        )
    }
}