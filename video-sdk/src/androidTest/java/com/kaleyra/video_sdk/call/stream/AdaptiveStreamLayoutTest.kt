package com.kaleyra.video_sdk.call.stream

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.kaleyra.video_sdk.assertBottomPositionInRootIsEqualTo
import com.kaleyra.video_sdk.assertLeftPositionInRootIsEqualTo
import com.kaleyra.video_sdk.assertRightPositionInRootIsEqualTo
import com.kaleyra.video_sdk.assertTopPositionInRootIsEqualTo
import com.kaleyra.video_sdk.call.screen.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.streamnew.utils.AdaptiveGridCalculator
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Rule
import org.junit.Test

class AdaptiveStreamLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testEmptyGrid() {
        composeTestRule.setContent {
            AdaptiveStreamLayout(content = {})
        }
    }

    @Test
    fun testThumbnailsSize() {
        val thumbnailsTag = "thumbnailsTag"
        val thumbnailSize = 90.dp
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailSize = thumbnailSize,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                }
            )
        }
        val thumbnail = composeTestRule.onNodeWithTag(thumbnailsTag)
        thumbnail.assertWidthIsEqualTo(thumbnailSize)
        thumbnail.assertHeightIsEqualTo(thumbnailSize)
    }

    @Test
    fun testThumbnailsCount() {
        val thumbnailsTag = "thumbnailsTag"
        val thumbnailsCount = 2
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsCount = thumbnailsCount,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    repeat(thumbnailsCount + 1) {
                        Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                    }
                }
            )
        }
        val thumbnails = composeTestRule.onAllNodesWithTag(thumbnailsTag)
        thumbnails[0].assertIsDisplayed()
        thumbnails[1].assertIsDisplayed()
        thumbnails[2].assertIsNotDisplayed()
    }

    @Test
    fun testThumbnailsArrangementBottom() {
        val thumbnailsTag = "thumbnailsTag"
        val itemCount = 2
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Bottom,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnails = composeTestRule.onAllNodesWithTag(thumbnailsTag)
        val thumbnailWidth = thumbnails[0].getUnclippedBoundsInRoot().width

        val startX = (containerWidth - thumbnailWidth * itemCount) / 2
        val startY = containerHeight - thumbnailWidth
        repeat(itemCount) { index ->
            val itemStartX = startX + thumbnailWidth * index
            val item = thumbnails[index]
            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + thumbnailWidth)
            item.assertTopPositionInRootIsEqualTo(startY)
            item.assertBottomPositionInRootIsEqualTo(containerHeight)
        }
    }

    @Test
    fun testThumbnailsArrangementTop() {
        val thumbnailsTag = "thumbnailsTag"
        val itemCount = 2
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Top,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val thumbnails = composeTestRule.onAllNodesWithTag(thumbnailsTag)
        val thumbnailSize = thumbnails[0].getUnclippedBoundsInRoot().width

        val startX = (containerWidth - thumbnailSize * itemCount) / 2
        val startY = 0.dp
        repeat(itemCount) { index ->
            val itemStartX = startX + thumbnailSize * index
            val item = thumbnails[index]
            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + thumbnailSize)
            item.assertTopPositionInRootIsEqualTo(startY)
            item.assertBottomPositionInRootIsEqualTo(startY + thumbnailSize)
        }
    }

    @Test
    fun testThumbnailsArrangementStart() {
        val thumbnailsTag = "thumbnailsTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Start,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                }
            )
        }
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnails = composeTestRule.onAllNodesWithTag(thumbnailsTag)
        val thumbnailSize = thumbnails[0].getBoundsInRoot().width

        val itemCount = 2
        val startX = 0.dp
        val startY = (containerHeight - thumbnailSize * itemCount) / 2
        repeat(itemCount) { index ->
            val itemStartY = startY + thumbnailSize * index
            val item = thumbnails[index]
            item.assertLeftPositionInRootIsEqualTo(startX)
            item.assertRightPositionInRootIsEqualTo(startX + thumbnailSize)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + thumbnailSize)
        }
    }

    @Test
    fun testThumbnailsArrangementEnd() {
        val thumbnailsTag = "thumbnailsTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.End,
                content = {
                    Box(Modifier.fillMaxSize().pin(true))
                    Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailsTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnails = composeTestRule.onAllNodesWithTag(thumbnailsTag)
        val thumbnailSize = thumbnails[0].getBoundsInRoot().width

        val itemCount = 2
        val startX = containerWidth - thumbnailSize
        val startY = (containerHeight - thumbnailSize * itemCount) / 2
        repeat(itemCount) { index ->
            val itemStartY = startY + thumbnailSize * index
            val item = thumbnails[index]
            item.assertLeftPositionInRootIsEqualTo(startX)
            item.assertRightPositionInRootIsEqualTo(containerWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + thumbnailSize)
        }
    }

    @Test
    fun testFeaturedItem() {
        val featuredTag = "featuredTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    Box(Modifier.fillMaxSize().testTag(featuredTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val item = composeTestRule.onNodeWithTag(featuredTag)
        item.assertLeftPositionInRootIsEqualTo(0.dp)
        item.assertRightPositionInRootIsEqualTo(containerWidth)
        item.assertTopPositionInRootIsEqualTo(0.dp)
        item.assertBottomPositionInRootIsEqualTo(containerHeight)
    }

    @Test
    fun testPortraitTwoFeaturedItems() {
        val featuredTag = "featuredTag"
        val itemCount = 2
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    repeat(itemCount) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(featuredTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemHeight = containerHeight / itemCount
        repeat(itemCount) { index ->
            val itemStartY = itemHeight * index
            val item = featured[index]
            item.assertLeftPositionInRootIsEqualTo(0.dp)
            item.assertRightPositionInRootIsEqualTo(containerWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)
        }
    }

    @Test
    fun testLandscapeTwoFeaturedItems() {
        val featuredTag = "featuredTag"
        val itemCount = 2
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(featuredTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / itemCount
        repeat(itemCount) { index ->
            val itemStartX = itemWidth * index
            val item = featured[index]
            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(0.dp)
            item.assertBottomPositionInRootIsEqualTo(containerHeight)
        }
    }

    @Test
    fun testPortraitThreeFeaturedItems() {
        val featuredTag = "featuredTag"
        val itemCount = 3
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(featuredTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemHeight = containerHeight / itemCount
        repeat(itemCount) { index ->
            val itemStartY = itemHeight * index
            val item = featured[index]
            item.assertLeftPositionInRootIsEqualTo(0.dp)
            item.assertRightPositionInRootIsEqualTo(containerWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)
        }
    }

    @Test
    fun testLandscapeThreeFeaturedItems() {
        val featuredTag = "featuredTag"
        val itemCount = 3
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(featuredTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnails = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / itemCount
        repeat(itemCount) { index ->
            val itemStartX = itemWidth * index
            val item = thumbnails[index]
            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(0.dp)
            item.assertBottomPositionInRootIsEqualTo(containerHeight)
        }
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementStart() {
        val pinnedTag = "pinnedTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Start,
                content = {
                    Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnailWidth = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val pinned = composeTestRule.onNodeWithTag(pinnedTag)
        pinned.assertLeftPositionInRootIsEqualTo(thumbnailWidth)
        pinned.assertRightPositionInRootIsEqualTo(containerWidth)
        pinned.assertTopPositionInRootIsEqualTo(0.dp)
        pinned.assertBottomPositionInRootIsEqualTo(containerHeight)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementEnd() {
        val pinnedTag = "pinnedTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.End,
                content = {
                    Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnailWidth = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val pinned = composeTestRule.onNodeWithTag(pinnedTag)
        pinned.assertLeftPositionInRootIsEqualTo(0.dp)
        pinned.assertRightPositionInRootIsEqualTo(containerWidth - thumbnailWidth)
        pinned.assertTopPositionInRootIsEqualTo(0.dp)
        pinned.assertBottomPositionInRootIsEqualTo(containerHeight)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementTop() {
        val pinnedTag = "pinnedTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Top,
                content = {
                    Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnailHeight = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().height
        val pinned = composeTestRule.onNodeWithTag(pinnedTag)
        pinned.assertLeftPositionInRootIsEqualTo(0.dp)
        pinned.assertRightPositionInRootIsEqualTo(containerWidth)
        pinned.assertTopPositionInRootIsEqualTo(thumbnailHeight)
        pinned.assertBottomPositionInRootIsEqualTo(containerHeight)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementBottom() {
        val pinnedTag = "pinnedTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Bottom,
                content = {
                    Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val thumbnailHeight = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().height
        val pinned = composeTestRule.onNodeWithTag(pinnedTag)
        pinned.assertLeftPositionInRootIsEqualTo(0.dp)
        pinned.assertRightPositionInRootIsEqualTo(containerWidth)
        pinned.assertTopPositionInRootIsEqualTo(0.dp)
        pinned.assertBottomPositionInRootIsEqualTo(containerHeight - thumbnailHeight)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementStartAndNoThumbnails() {
        mockkObject(AdaptiveGridCalculator)
        val pinnedTag = "pinnedTag"
        val rows = 3
        val columns = 2
        val pinnedCount = 5
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Start,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val pinned = composeTestRule.onAllNodesWithTag(pinnedTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0

        repeat(pinnedCount) { index ->
            val item = pinned[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementEndAndNoThumbnails() {
        mockkObject(AdaptiveGridCalculator)
        val pinnedTag = "pinnedTag"
        val rows = 3
        val columns = 2
        val pinnedCount = 5
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.End,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val pinned = composeTestRule.onAllNodesWithTag(pinnedTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0

        repeat(pinnedCount) { index ->
            val item = pinned[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementTopAndNoThumbnails() {
        mockkObject(AdaptiveGridCalculator)
        val pinnedTag = "pinnedTag"
        val rows = 3
        val columns = 2
        val pinnedCount = 5
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Top,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val pinned = composeTestRule.onAllNodesWithTag(pinnedTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0

        repeat(pinnedCount) { index ->
            val item = pinned[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testPinnedBoundsWithThumbnailsArrangementBottomAndNoThumbnails() {
        mockkObject(AdaptiveGridCalculator)
        val pinnedTag = "pinnedTag"
        val rows = 3
        val columns = 2
        val pinnedCount = 5
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Bottom,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(pinnedTag))
                    }
                }
            )
        }
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val pinned = composeTestRule.onAllNodesWithTag(pinnedTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0

        repeat(pinnedCount) { index ->
            val item = pinned[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testMultipleFeaturedItems() {
        mockkObject(AdaptiveGridCalculator)
        val rows = 3
        val columns = 3
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        val itemCount = 8
        val featuredTag = "featuredTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                content = {
                    repeat(itemCount) {
                        Box(Modifier.fillMaxSize().testTag(featuredTag))
                    }
                }
            )
        }

        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = itemCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0
        repeat(itemCount) { index ->
            val item = featured[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX)
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth)
            item.assertTopPositionInRootIsEqualTo(itemStartY)
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight)

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testMultiplePinnedItemsThumbnailArrangementBottom() {
        mockkObject(AdaptiveGridCalculator)
        val rows = 3
        val columns = 3
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        val pinnedCount = 8
        val featuredTag = "featuredTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Bottom,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(featuredTag))
                    }
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val thumbnailSize = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height - thumbnailSize
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0
        repeat(pinnedCount) { index ->
            val item = featured[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX, Dp(1f))
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth, Dp(1f))
            item.assertTopPositionInRootIsEqualTo(itemStartY, Dp(1f))
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight, Dp(1f))

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testMultiplePinnedItemsThumbnailArrangementLeft() {
        mockkObject(AdaptiveGridCalculator)
        val rows = 3
        val columns = 3
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        val pinnedCount = 8
        val featuredTag = "featuredTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Start,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(featuredTag))
                    }
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val thumbnailSize = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width - thumbnailSize
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0
        repeat(pinnedCount) { index ->
            val item = featured[index]
            val itemStartX = thumbnailSize + if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX, Dp(1f))
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth, Dp(1f))
            item.assertTopPositionInRootIsEqualTo(itemStartY, Dp(1f))
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight, Dp(1f))

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testMultiplePinnedItemsThumbnailArrangementTop() {
        mockkObject(AdaptiveGridCalculator)
        val rows = 3
        val columns = 3
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        val pinnedCount = 8
        val featuredTag = "featuredTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.Top,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(featuredTag))
                    }
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val thumbnailSize = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height - thumbnailSize
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0
        repeat(pinnedCount) { index ->
            val item = featured[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = thumbnailSize + itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX, Dp(1f))
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth, Dp(1f))
            item.assertTopPositionInRootIsEqualTo(itemStartY, Dp(1f))
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight, Dp(1f))

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }

    @Test
    fun testMultiplePinnedItemsThumbnailArrangementEnd() {
        mockkObject(AdaptiveGridCalculator)
        val rows = 3
        val columns = 3
        every { AdaptiveGridCalculator.calculateGridAndFeaturedSize(any(), any(), any()) } answers {
            Triple(rows, columns, IntSize(firstArg<Int>() / columns, secondArg<Int>() / rows))
        }
        val pinnedCount = 8
        val featuredTag = "featuredTag"
        val thumbnailTag = "thumbnailTag"
        composeTestRule.setContent {
            AdaptiveStreamLayout(
                thumbnailsArrangement = ThumbnailsArrangement.End,
                content = {
                    repeat(pinnedCount) {
                        Box(Modifier.fillMaxSize().pin(true).testTag(featuredTag))
                    }
                    Box(Modifier.fillMaxSize().testTag(thumbnailTag))
                }
            )
        }
        val thumbnailSize = composeTestRule.onNodeWithTag(thumbnailTag).getUnclippedBoundsInRoot().width
        val containerWidth = composeTestRule.onRoot().getBoundsInRoot().width - thumbnailSize
        val containerHeight = composeTestRule.onRoot().getBoundsInRoot().height
        val featured = composeTestRule.onAllNodesWithTag(featuredTag)

        val itemWidth = containerWidth / columns
        val itemHeight = containerHeight / rows

        val lastRowItemsCount = pinnedCount - (columns * (rows - 1))
        val lastRowPadding = (containerWidth - itemWidth * lastRowItemsCount) / 2
        var row = 0
        var column = 0
        repeat(pinnedCount) { index ->
            val item = featured[index]
            val itemStartX = if (row == rows - 1) {
                lastRowPadding + itemWidth * column
            } else itemWidth * column
            val itemStartY = itemHeight * row

            item.assertLeftPositionInRootIsEqualTo(itemStartX, Dp(1f))
            item.assertRightPositionInRootIsEqualTo(itemStartX + itemWidth, Dp(1f))
            item.assertTopPositionInRootIsEqualTo(itemStartY, Dp(1f))
            item.assertBottomPositionInRootIsEqualTo(itemStartY + itemHeight, Dp(1f))

            row = if (index % columns == columns - 1) row + 1 else row
            column = if (index % columns == columns - 1) 0 else column + 1
        }
        unmockkObject(AdaptiveGridCalculator)
    }
}