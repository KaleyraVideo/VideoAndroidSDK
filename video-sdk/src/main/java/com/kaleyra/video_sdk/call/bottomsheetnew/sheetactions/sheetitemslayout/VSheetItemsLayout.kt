package com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
internal fun VSheetItemsLayout(
    modifier: Modifier = Modifier,
    maxItems: Int = Int.MAX_VALUE,
    verticalItemSpacing: Dp = SheetItemsSpacing,
    onItemsPlaced: ((itemsPlaced: Int) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val padding = with(density) { verticalItemSpacing.toPx().roundToInt() }
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        var height = 0
        val placeables = mutableListOf<Placeable>()
        for (index in 0..< min(measurables.size, maxItems)) {
            val placeable = measurables[index].measure(constraints.copy(minHeight = 0))
            val newHeight = height + (padding.takeIf { index != 0 } ?: 0) + placeable.height
            // if no more items can be laid out..
            if (newHeight > constraints.maxHeight) {
                onItemsPlaced?.invoke(index)
                break
            }
            height = newHeight
            placeables += placeable
        }
        // if all the items are laid out...
        if (placeables.size == min(measurables.size, maxItems)) {
            onItemsPlaced?.invoke(placeables.size)
        }

        val width = placeables.maxOfOrNull { it.width } ?: 0
        layout(width, height) {
            var y = height - (placeables.getOrNull(0)?.height ?: 0)
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(0, y)
                y -= placeable.height + if (index < placeables.size - 1) padding else 0
            }
        }
    }
}