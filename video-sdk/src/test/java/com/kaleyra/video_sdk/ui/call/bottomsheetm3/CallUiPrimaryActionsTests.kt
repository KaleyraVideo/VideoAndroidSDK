package com.kaleyra.video_sdk.ui.call.bottomsheetm3

import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetm3.view.portraitActionContainerWidth
import com.kaleyra.video_sdk.call.bottomsheetm3.view.portraitActionWidth
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallUiPrimaryActionsTests {

    @Test
    fun testPortraitPrimaryActionsWidthOneItem() {
        val itemWidth = portraitActionWidth(maxWidth = 1000.dp, itemsPerRow = 1, index = 1)
        Assert.assertEquals(1000.dp, itemWidth)
    }

    @Test
    fun testPortraitPrimaryActionsWidthTwoItems() {
        val itemsWidth = (0..1).map { portraitActionWidth(maxWidth = 1000.dp, itemsPerRow = 2, index = it) }
        Assert.assertEquals(true, itemsWidth.all { it == 405.dp })
    }

    @Test
    fun testPortraitPrimaryActionsWidthThreeItems() {
        val itemsWidth = (0..2).map { portraitActionWidth(maxWidth = 1000.dp, itemsPerRow = 3, index = it) }
        Assert.assertEquals(true, itemsWidth.subList(0,2).all { it == 48.dp })
        Assert.assertEquals(524.dp, itemsWidth.last())
    }

    @Test
    fun testPortraitPrimaryActionsWidthFourItems() {
        val itemsWidth = (0..3).map { portraitActionWidth(maxWidth = 1000.dp, maxItemsPerRow = 5, itemsPerRow = 4, index = it) }
        Assert.assertEquals(true, itemsWidth.subList(0,3).all { it == 48.dp })
        Assert.assertEquals(286.dp, itemsWidth.last())
    }

    @Test
    fun testPortraitPrimaryActionsWidthFiveItems() {
        val itemsWidth = (0..4).map { portraitActionWidth(maxWidth = 1000.dp, itemsPerRow = 5, index = it) }
        Assert.assertEquals(true, itemsWidth.all { it == 48.dp })
    }

    @Test
    fun testPortraitActionContainerWidthOneAction() {
        val maxWidth = 1000.dp
        Assert.assertEquals(200.dp, portraitActionContainerWidth(0, 5, maxWidth))
    }

    @Test
    fun testPortraitActionContainerWidthTwoActions() {
        val maxWidth = 1000.dp
        Assert.assertEquals(500.dp, portraitActionContainerWidth(0, 2, maxWidth))
        Assert.assertEquals(500.dp, portraitActionContainerWidth(1, 2, maxWidth))
    }

    @Test
    fun testPortraitActionContainerWidthThreeActions() {
        val maxWidth = 1000.dp
        Assert.assertEquals(200.dp, portraitActionContainerWidth(0, 3, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(1, 3, maxWidth))
        Assert.assertEquals(600.dp, portraitActionContainerWidth(2, 3, maxWidth))
    }

    @Test
    fun testPortraitActionContainerWidthFourActions() {
        val maxWidth = 1000.dp
        Assert.assertEquals(200.dp, portraitActionContainerWidth(0, 4, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(1, 4, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(2, 4, maxWidth))
        Assert.assertEquals(400.dp, portraitActionContainerWidth(3, 4, maxWidth))
    }

    @Test
    fun testPortraitActionContainerWidthFiveActions() {
        val maxWidth = 1000.dp
        Assert.assertEquals(200.dp, portraitActionContainerWidth(0, 5, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(1, 5, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(2, 5, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(3, 5, maxWidth))
        Assert.assertEquals(200.dp, portraitActionContainerWidth(4, 5, maxWidth))
    }
}