package com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEach
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.VSheetItemsLayout
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.CallActionDefaults
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.NotifiableCallAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

private const val MaxVSheetItems = 5

@Composable
internal fun VSheetContent(
    callActions: ImmutableList<CallActionUI>,
    showAnswerAction: Boolean,
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
    onMoreActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxActions: Int = MaxVSheetItems
) {
    var showMoreAction by remember { mutableStateOf(false) }
    val moreNotificationCount = remember(callActions) { callActions.value.filterIsInstance<NotifiableCallAction>().sumOf { it.notificationCount } }

    Column(modifier) {
        when {
            showAnswerAction -> {
                AnswerAction(
                    onClick = onAnswerActionClick,
                    modifier = Modifier.size(CallActionDefaults.minButtonSize)
                )
                Spacer(Modifier.height(SheetItemsSpacing))
            }
            showMoreAction -> {
                MoreAction(
                    badgeText = if (moreNotificationCount != 0) "$moreNotificationCount" else null,
                    onClick = onMoreActionClick
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