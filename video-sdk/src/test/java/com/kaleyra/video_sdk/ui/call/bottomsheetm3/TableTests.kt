package com.kaleyra.video_sdk.ui.call.bottomsheetm3

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.call.bottomsheetm3.view.Table
import com.kaleyra.video_sdk.call.bottomsheetm3.view.lastRowItemsCount
import com.kaleyra.video_sdk.call.bottomsheetm3.view.rowsCount
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TableTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCompleteTableItemsDisplayed() {
        val data: List<String> = (0..11).map { "$it" }.toList()

        composeTestRule.setContent {
            Column {
                Table(
                    columnCount = 5,
                    data = data
                ) { index, item, itemsPerRow ->
                    Text(text = item)
                }
            }
        }
        data.forEach { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun testLastRowItemsDisplayed() {
        val data: List<String> = (0..2).map { "$it" }.toList()

        composeTestRule.setContent {
            Column {
                Table(
                    columnCount = 5,
                    data = data
                ) { index, item, itemsPerRow ->
                    Text(text = item)
                }
            }
        }
        data.forEach { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun testTableRowsCount() {
        val rowsCount = rowsCount(6, 5)
        Assert.assertEquals(2, rowsCount)
    }

    @Test
    fun testLastRowItemsCount() {
        val lastRowItemsCount = lastRowItemsCount(2, 5)
        Assert.assertEquals(2, lastRowItemsCount)
    }

    @Test
    fun testCompleteLastRowItemsCount() {
        val lastRowItemsCount = lastRowItemsCount(5, 5)
        Assert.assertEquals(5, lastRowItemsCount)
    }
}
