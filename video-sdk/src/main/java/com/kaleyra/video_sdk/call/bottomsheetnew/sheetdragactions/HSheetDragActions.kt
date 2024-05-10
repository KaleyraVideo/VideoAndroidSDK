package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.screennew.ActionComposable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

internal val HDragActionModifier = Modifier.animatePlacement()

@Composable
internal fun HSheetDragActions(
    actions: ImmutableList<ActionComposable>,
    itemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    val actionsCount = actions.count()
    val hasOneRow = actionsCount < itemsPerRow
    VerticalGrid(
        columns = SimpleGridCells.Fixed(itemsPerRow),
        horizontalArrangement = Arrangement.spacedBy(HSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(HSheetDragVerticalPadding),
        modifier = modifier
    ) {
        actions.value.fastForEachIndexed { index, composable ->
            val isLastIndex = index == actionsCount - 1
            val mod = if (isLastIndex && hasOneRow) {
                val spanCount = itemsPerRow - (actionsCount % itemsPerRow) + 1
                HDragActionModifier.span(spanCount)
            } else HDragActionModifier
            composable(true, mod)
        }
    }
}