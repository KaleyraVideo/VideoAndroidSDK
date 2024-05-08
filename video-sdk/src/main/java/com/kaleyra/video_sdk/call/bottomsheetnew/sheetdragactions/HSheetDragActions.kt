package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

@Composable
internal fun HSheetDragActions(
    actions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    itemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    val shouldExtendLastButton = actions.count() / itemsPerRow < 1
    val chunkedActions = actions.value.chunked(itemsPerRow)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HSheetDragVerticalPadding)
    ) {
        chunkedActions.fastForEachIndexed { actionsIndex, actions ->
            Row(horizontalArrangement = Arrangement.spacedBy(HSheetDragHorizontalPadding)) {
                actions.fastForEachIndexed { index, action ->
                    val itemModifier = if (shouldExtendLastButton && actionsIndex == chunkedActions.size - 1 && index == actions.size - 1) Modifier.weight(1f) else Modifier
                    action(true, itemModifier)
                }
            }
        }
    }
}