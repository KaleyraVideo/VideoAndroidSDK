package com.kaleyra.video_sdk.ui.call.streams

import androidx.compose.ui.unit.IntSize
import com.kaleyra.video_sdk.call.stream.utils.AdaptiveStreamLayoutHelper.calculateGridAndFeaturedSize
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveStreamLayoutHelperTest {

    @Test
    fun testRatioInsideRatioRange() {
        val width = 1920
        val height = 800
        val itemsCount = 5
        val (rows, cols, size) = calculateGridAndFeaturedSize(width, height, itemsCount)

        val expectedRows = 2
        val expectedColumns = 3
        assertEquals(expectedRows, rows)
        assertEquals(expectedColumns, cols)
        assertEquals(IntSize(width / expectedColumns, height / expectedRows), size)
    }

    @Test
    fun testRatioCloserToMinRatio() {
        val width = 1000
        val height = 500
        val itemsCount = 5
        val (rows, cols, size) = calculateGridAndFeaturedSize(width, height, itemsCount)

        val expectedRows = 2
        val expectedColumns = 3
        assertEquals(expectedRows, rows)
        assertEquals(expectedColumns, cols)
        assertEquals(IntSize(width / expectedColumns, height / expectedRows), size)
    }

    @Test
    fun testRatioCloserToMaxRatio() {
        val width = 1000
        val height = 250
        val itemsCount = 5
        val (rows, cols, size) = calculateGridAndFeaturedSize(width, height, itemsCount)

        val expectedRows = 2
        val expectedColumns = 4
        assertEquals(rows, expectedRows)
        assertEquals(cols, expectedColumns)
        assertEquals(IntSize(width / expectedColumns, height / expectedRows), size)
    }

    @Test
    fun testZeroContainerWidth() {
        val width = 0
        val height = 500
        val itemsCount = 5
        val (rows, cols, size) = calculateGridAndFeaturedSize(width, height, itemsCount)

        assertEquals(1, rows)
        assertEquals(1, cols)
        assertEquals(IntSize(width, height), size)
    }

    @Test
    fun testZeroContainerHeight() {
        val width = 500
        val height = 0
        val itemsCount = 5
        val (rows, cols, size) = calculateGridAndFeaturedSize(width, height, itemsCount)

        assertEquals(1, rows)
        assertEquals(1, cols)
        assertEquals(IntSize(width, height), size)
    }
}