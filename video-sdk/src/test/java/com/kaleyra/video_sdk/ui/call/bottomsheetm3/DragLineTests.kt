package com.kaleyra.video_sdk.call.bottomsheetm3

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.bottomsheet.LineTag
import com.kaleyra.video_sdk.call.bottomsheetm3.view.DragLine
import com.kaleyra.video_sdk.call.bottomsheetm3.view.VerticalDragLine
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DragLineTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testHorizontalDragLineClicked() {
        var hasClicked = false
        composeTestRule.setContent {
            DragLine(onClickLabel = "drag") {
                hasClicked = true
            }
        }
        composeTestRule.onNodeWithTag(LineTag).performClick()
        Assert.assertEquals(true, hasClicked)
    }

    @Test
    fun testVerticalDragLineClicked() {
        var hasClicked = false
        composeTestRule.setContent {
            VerticalDragLine(onClickLabel = "drag") {
                hasClicked = true
            }
        }
        composeTestRule.onNodeWithTag(LineTag).performClick()
        Assert.assertEquals(true, hasClicked)
    }
}