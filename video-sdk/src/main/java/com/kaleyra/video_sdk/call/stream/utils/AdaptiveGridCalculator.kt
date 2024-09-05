package com.kaleyra.video_sdk.call.stream.utils

import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.ceil

internal object AdaptiveGridCalculator {

    private const val ASPECT_RATIO_16_9 = 1.77f
    private const val ASPECT_RATIO_4_3 = 1.33f

    fun calculateGridAndFeaturedSize(
        containerWidth: Int,
        containerHeight: Int,
        itemsCount: Int
    ): Triple<Int, Int, IntSize> {
        if (containerWidth == 0 || containerHeight == 0) {
            return Triple(1, 1, IntSize.Zero)
        }

        var bestGrid: Triple<Int, Int, IntSize>? = null
        var closestRatioDiff = Float.MAX_VALUE

        for (cols in itemsCount downTo 1) {
            val rows = ceil(itemsCount / cols.toFloat()).toInt()

            val itemWidth = containerWidth / cols
            val itemHeight = containerHeight / rows
            val ratio = itemWidth.toFloat() / itemHeight

            val ratioDiffTo16by9 = abs(ratio - ASPECT_RATIO_16_9)
            val ratioDiffTo4by3 = abs(ratio - ASPECT_RATIO_4_3)

            if (ratio in ASPECT_RATIO_4_3..ASPECT_RATIO_16_9) {
                // Found a perfect fit within the acceptable range
                return Triple(rows, cols, IntSize(itemWidth, itemHeight))
            } else if (ratioDiffTo16by9 < closestRatioDiff || ratioDiffTo4by3 < closestRatioDiff) {
                // Keep track of the grid with the closest aspect ratio to either 16:9 or 4:3
                bestGrid = Triple(rows, cols, IntSize(itemWidth, itemHeight))
                closestRatioDiff = minOf(ratioDiffTo16by9, ratioDiffTo4by3)
            }
        }

        // Return the best grid found, or a default 1x1 grid if none were suitable
        return bestGrid ?: Triple(1, 1, IntSize(containerWidth, containerHeight))
    }
}

