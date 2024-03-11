package com.kaleyra.video_sdk.call.bottomsheetm3

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.bottomsheetm3.view.Table
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
}