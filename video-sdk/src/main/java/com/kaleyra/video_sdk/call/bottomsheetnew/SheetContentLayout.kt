package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun SheetContentLayout(
    modifier: Modifier = Modifier,
    horizontalItemSpacing: Dp = 24.dp,
    onLayout: ((itemsCount: Int) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val padding = with(density) { horizontalItemSpacing.toPx().roundToInt() }
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        var width = 0
        val placeables = mutableListOf<Placeable>()
        for (index in measurables.indices) {
            val placeable = measurables[index].measure(constraints)
            val newWidth = width + (padding.takeIf { index != 0 } ?: 0) + placeable.width
            // if no more items can be laid out..
            if (newWidth > constraints.maxWidth) {
                onLayout?.invoke(index)
                break
            }
            width = newWidth
            placeables += placeable
        }
        // if all the items are laid out...
        if (placeables.size == measurables.size) {
            onLayout?.invoke(placeables.size)
        }

        val height = placeables.maxOf { it.height }
        layout(width, height) {
            var x = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x, 0)
                x += placeable.width + if (index < placeables.size - 1) padding else 0
            }
        }
    }
}

@Composable
internal fun VerticalSheetContentLayout(
    modifier: Modifier = Modifier,
    verticalItemSpacing: Dp = 24.dp,
    onLayout: ((itemsCount: Int) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val padding = with(density) { verticalItemSpacing.toPx().roundToInt() }
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        var height = 0
        val placeables = mutableListOf<Placeable>()
        for (index in measurables.indices) {
            val placeable = measurables[index].measure(constraints)
            val newHeight = height + (padding.takeIf { index != 0 } ?: 0) + placeable.height
            // if no more items can be laid out..
            if (newHeight > constraints.maxHeight) {
                onLayout?.invoke(index)
                break
            }
            height = newHeight
            placeables += placeable
        }
        // if all the items are laid out...
        if (placeables.size == measurables.size) {
            onLayout?.invoke(placeables.size)
        }

        val width = placeables.maxOf { it.width }
        layout(width, height) {
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height + if (index < placeables.size - 1) padding else 0
            }
        }
    }
}