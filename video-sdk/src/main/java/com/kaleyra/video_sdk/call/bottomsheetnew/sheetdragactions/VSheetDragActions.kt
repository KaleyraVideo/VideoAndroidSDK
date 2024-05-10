package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import com.cheonjaeung.compose.grid.HorizontalGrid
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

internal val VSheetDragHorizontalPadding = 20.dp
internal val VSheetDragVerticalPadding = SheetItemsSpacing

internal val VDragActionModifier = Modifier.animatePlacement()

@Composable
internal fun VSheetDragActions(
    actions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    itemsPerColumn: Int,
    modifier: Modifier = Modifier
) {
    val chunkedActions = actions.value.chunked(itemsPerColumn)
    HorizontalGrid(
        rows = SimpleGridCells.Fixed(itemsPerColumn),
        horizontalArrangement = Arrangement.spacedBy(VSheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(VSheetDragVerticalPadding),
        modifier = modifier
    ) {
        chunkedActions.fastForEach { actions ->
            actions.fastForEachReversed { action ->
                action(false, VDragActionModifier)
            }
        }
    }
}