package com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.HSheetItemsLayout
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.call.screennew.ActionComposable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.row.ReversibleRow

@Composable
internal fun HSheetActions(
    modifier: Modifier = Modifier,
    actions: ImmutableList<ActionComposable>,
    maxActions: Int = Int.MAX_VALUE,
    showAnswerAction: Boolean,
    extendedAnswerAction: Boolean,
    onAnswerActionClick: () -> Unit,
    onMoreActionClick: () -> Unit,
    onActionsPlaced: (actionsPlaced: Int) -> Unit
) {
    var showMoreAction by remember { mutableStateOf(false) }

    ReversibleRow(modifier, reverseLayout = true) {
        when {
            showAnswerAction -> {
                AnswerAction(extended = extendedAnswerAction, onClick = onAnswerActionClick)
                Spacer(Modifier.width(SheetItemsSpacing))
            }
            showMoreAction -> {
                MoreAction(onClick = onMoreActionClick)
                Spacer(Modifier.width(SheetItemsSpacing))
            }
        }

        HSheetItemsLayout(
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = actions.count() > itemsPlaced && !showAnswerAction
                onActionsPlaced(itemsPlaced)
            },
            maxItems = maxActions - if (showAnswerAction || showMoreAction) 1 else 0,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}