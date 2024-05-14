package com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt

internal val SheetItemsSpacing = 20.dp

/**
 * Try to place all the items in the available space in a row like manner
 *
 * @param modifier Modifier
 * @param maxItems The max number of item to place
 * @param horizontalItemSpacing The horizontal item spacing in Dp
 * @param onItemsPlaced Callback called when the items are lay out
 * @param content The content containing the item composables
 */
@Composable
internal fun HSheetItemsLayout(
    modifier: Modifier = Modifier,
    maxItems: Int = Int.MAX_VALUE,
    horizontalItemSpacing: Dp = SheetItemsSpacing,
    onItemsPlaced: ((itemsPlaced: Int) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val padding = with(density) { horizontalItemSpacing.toPx().roundToInt() }
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        var width = 0
        val placeables = mutableListOf<Placeable>()
        for (index in 0..< min(measurables.size, maxItems)) {
            val placeable = measurables[index].measure(constraints.copy(minWidth = 0))
            val newWidth = width + (padding.takeIf { index != 0 } ?: 0) + placeable.width
            // if no more items can be laid out..
            if (newWidth > constraints.maxWidth) {
                onItemsPlaced?.invoke(index)
                break
            }
            width = newWidth
            placeables += placeable
        }
        // if all the items are laid out...
        if (placeables.size == min(measurables.size, maxItems)) {
            onItemsPlaced?.invoke(placeables.size)
        }

        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            var x = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x, 0)
                x += placeable.width + if (index < placeables.size - 1) padding else 0
            }
        }
    }
}