package com.kaleyra.video_sdk.common.row

import androidx.compose.material3.Text
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.common.row.ReversibleRow
import org.junit.Rule
import org.junit.Test

class ReversibleRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testReverseFalse() {
        val text1 = "text1"
        val text2 = "text2"
        composeTestRule.setContent {
            ReversibleRow(reverseLayout = false) {
                Text(text1)
                Text(text2)
            }
        }
        val childBounds1 = composeTestRule.onNodeWithText(text1).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText(text2).getBoundsInRoot()
        assert(childBounds1.left < childBounds2.left)
    }

    @Test
    fun testReverseTrue() {
        val text1 = "text1"
        val text2 = "text2"
        composeTestRule.setContent {
            ReversibleRow(reverseLayout = true) {
                Text(text1)
                Text(text2)
            }
        }
        val childBounds1 = composeTestRule.onNodeWithText(text1).getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText(text2).getBoundsInRoot()
        assert(childBounds1.left > childBounds2.left)
    }
}