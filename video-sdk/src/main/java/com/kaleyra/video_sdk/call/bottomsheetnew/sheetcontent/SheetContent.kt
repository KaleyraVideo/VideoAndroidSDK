package com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetActionsLayout
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetActionsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.VerticalSheetActionsLayout
import com.kaleyra.video_sdk.call.callactionnew.MoreAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.row.ReversibleRow

// TODO add participant item if ringing
@Composable
internal fun SheetContent(
    actions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    showMoreItem: Boolean,
    onMoreItemClick: (() -> Unit),
    onItemsPlaced: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ReversibleRow(modifier, reverseLayout = true) {
        if (showMoreItem) {
            MoreAction(onClick = onMoreItemClick)
            Spacer(Modifier.width(SheetActionsSpacing))
        }
        SheetActionsLayout(
            onItemsPlaced = onItemsPlaced,
            horizontalItemSpacing = SheetActionsSpacing,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}

@Composable
internal fun VerticalSheetContent(
    actions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    showMoreItem: Boolean,
    onMoreItemClick: (() -> Unit),
    onItemsPlaced: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (showMoreItem) {
            MoreAction(onClick = onMoreItemClick)
            Spacer(Modifier.height(SheetActionsSpacing))
        }
        VerticalSheetActionsLayout(
            onItemsPlaced = onItemsPlaced,
            verticalItemSpacing = SheetActionsSpacing,
            content = { actions.value.forEach { action -> action(false, Modifier) } }
        )
    }
}