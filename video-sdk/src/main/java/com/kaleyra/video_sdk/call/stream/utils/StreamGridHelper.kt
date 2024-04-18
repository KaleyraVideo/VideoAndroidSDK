package com.kaleyra.video_sdk.call.stream.utils

import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

internal object StreamGridHelper {

    const val MAX_STREAM_RATIO = 1.77f // 16:9 ratio

    const val MIN_STREAM_RATIO = 1.33f // 4:3 ratio

    fun calculateGridAndFeaturedSize(
        containerWidth: Int,
        containerHeight: Int,
        itemsCount: Int
    ): Triple<Int, Int, IntSize> {
        var result = Triple(1, 1, IntSize(containerWidth, containerHeight))
        var fallbackRatio = 0f

        if (containerWidth == 0 || containerHeight == 0) return result

        for (cols in itemsCount downTo 1) {
            val rows = ceil(itemsCount / cols.toFloat()).toInt()

            val itemWidth = containerWidth / cols
            val itemHeight = containerHeight / rows
            val ratio = itemWidth / itemHeight.toFloat()

            when {
                ratio in MIN_STREAM_RATIO..MAX_STREAM_RATIO -> {
                    result = Triple(rows, cols, IntSize(itemWidth, itemHeight))
                    break
                }

                fallbackRatio == 0f || isRatioCloserToFallbackRatio(ratio, fallbackRatio) -> {
                    result = Triple(rows, cols, IntSize(itemWidth, itemHeight))
                    fallbackRatio = ratio
                }
            }
        }

        return result
    }

    private fun isRatioCloserToFallbackRatio(ratio: Float, fallbackRatio: Float): Boolean =
        min(abs(ratio - MIN_STREAM_RATIO), abs(ratio - MAX_STREAM_RATIO)) <= min(abs(fallbackRatio - MIN_STREAM_RATIO), abs(fallbackRatio - MAX_STREAM_RATIO))
}