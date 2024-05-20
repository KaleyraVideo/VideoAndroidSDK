package com.kaleyra.video_sdk.call.bottomsheetnew.streammenu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactionnew.CancelAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class HStreamMenuContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCancelActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fullscreenFalse_fullscreenActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fullscreenFalse_minimizeActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_off)
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = true,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pinFalse_pinActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pinTrue_unpinActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = true,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnCancelClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        var clicked = false
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = { clicked = true },
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testOnFullscreenClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        var clicked = false
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = { clicked = true },
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testOnPinClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        var clicked = false
        composeTestRule.setContent {
            HStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = { clicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }
}