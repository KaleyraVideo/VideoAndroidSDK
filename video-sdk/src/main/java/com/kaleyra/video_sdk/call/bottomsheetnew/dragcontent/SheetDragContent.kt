package com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.SheetContentItemSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val SheetDragHorizontalPadding = SheetContentItemSpacing
internal val SheetDragVerticalPadding = 30.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SheetDragContent(
    dragActions: ImmutableList<@Composable (Modifier, Boolean) -> Unit>,
    itemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    val shouldExtendLastButton = dragActions.count() / itemsPerRow < 1
    FlowRow(
        modifier = modifier,
        maxItemsInEachRow = itemsPerRow,
        horizontalArrangement = Arrangement.spacedBy(SheetDragHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(SheetDragVerticalPadding)
    ) {
        dragActions.value.forEachIndexed { index, action ->
            val itemModifier = if (shouldExtendLastButton && index == dragActions.count() - 1) Modifier.weight(1f) else Modifier
            action(itemModifier, true)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VerticalSheetDragContent(
    dragActions: ImmutableList<@Composable (Modifier, Boolean) -> Unit>,
    itemsPerColumn: Int,
    modifier: Modifier = Modifier
) {
    val chunkedActions = dragActions.value.chunked(itemsPerColumn)
    FlowColumn(
        modifier = modifier,
        maxItemsInEachColumn = itemsPerColumn,
        horizontalArrangement = Arrangement.spacedBy(SheetDragVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(SheetDragHorizontalPadding)
    ) {
        chunkedActions.forEach { actions ->
            actions.reversed().forEach { action ->
                action(Modifier, false)
            }
        }
    }
}