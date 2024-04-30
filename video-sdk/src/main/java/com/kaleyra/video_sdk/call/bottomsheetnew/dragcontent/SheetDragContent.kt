package com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetActionsSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

internal val SheetDragHorizontalPadding = SheetActionsSpacing
internal val SheetDragVerticalPadding = 30.dp

@Composable
internal fun SheetDragContent(
    dragActions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    itemsPerRow: Int,
    modifier: Modifier = Modifier
) {
    val shouldExtendLastButton = dragActions.count() / itemsPerRow < 1
    val chunkedActions = dragActions.value.chunked(itemsPerRow)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SheetDragVerticalPadding)
    ) {
        chunkedActions.forEachIndexed { actionsIndex, actions ->
            Row(horizontalArrangement = Arrangement.spacedBy(SheetDragHorizontalPadding)) {
                actions.forEachIndexed { index, action ->
                    val itemModifier =
                        if (shouldExtendLastButton && actionsIndex == chunkedActions.size - 1 && index == actions.size - 1) {
                            Modifier.weight(1f)
                        } else Modifier
                    action(true, itemModifier)
                }
            }
        }
    }
}

@Composable
internal fun VerticalSheetDragContent(
    dragActions: ImmutableList<@Composable (Boolean, Modifier) -> Unit>,
    itemsPerColumn: Int,
    modifier: Modifier = Modifier
) {
    val chunkedActions = dragActions.value.chunked(itemsPerColumn)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SheetDragVerticalPadding)
    ) {
        chunkedActions.forEach { actions ->
            Column(verticalArrangement = Arrangement.spacedBy(SheetDragHorizontalPadding)) {
                actions.reversed().forEach { action ->
                    action(false, Modifier)
                }
            }
        }
    }
}