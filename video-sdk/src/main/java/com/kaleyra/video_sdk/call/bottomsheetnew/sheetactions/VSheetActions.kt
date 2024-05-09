package com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.VSheetItemsLayout
import com.kaleyra.video_sdk.call.callactionnew.AnswerAction
import com.kaleyra.video_sdk.call.callactionnew.CallActionDefaults
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.call.screennew.ActionComposable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Composable
internal fun VSheetActions(
    modifier: Modifier = Modifier,
    actions: ImmutableList<ActionComposable>,
    maxActions: Int = Int.MAX_VALUE,
    showAnswerAction: Boolean,
    onAnswerActionClick: () -> Unit,
    onMoreActionClick: () -> Unit,
    onActionsPlaced: (actionsPlaced: Int) -> Unit
) {
    var showMoreAction by remember { mutableStateOf(true) }

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
                MoreAction(onClick = onMoreActionClick)
                Spacer(Modifier.height(SheetItemsSpacing))
            }
        }

        VSheetItemsLayout(
            onItemsPlaced = { itemsPlaced ->
                showMoreAction = actions.count() > itemsPlaced
                onActionsPlaced(itemsPlaced)
            },
            maxItems = maxActions - if (showAnswerAction) 1 else 0,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}