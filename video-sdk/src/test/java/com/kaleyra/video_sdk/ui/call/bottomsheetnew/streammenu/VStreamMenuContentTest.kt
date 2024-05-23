package com.kaleyra.video_sdk.ui.call.bottomsheetnew.streammenu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.VStreamMenuContent
import com.kaleyra.video_sdk.call.callactionnew.CancelAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VStreamMenuContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testCancelActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        composeTestRule.setContent {
            VStreamMenuContent(
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
            VStreamMenuContent(
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
            VStreamMenuContent(
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
            VStreamMenuContent(
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
            VStreamMenuContent(
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
            VStreamMenuContent(
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
    fun testOnFullscreenClickFalse() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        var fullscreenClick: Boolean? = null
        composeTestRule.setContent {
            VStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = { fullscreenClick = it },
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(false, fullscreenClick)
    }

    @Test
    fun testOnFullscreenClickTrue() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_off)
        var fullscreenClick: Boolean? = null
        composeTestRule.setContent {
            VStreamMenuContent(
                fullscreen = true,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = { fullscreenClick = it },
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, fullscreenClick)
    }

    @Test
    fun testOnPinClickFalse() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        var pinClick: Boolean? = null
        composeTestRule.setContent {
            VStreamMenuContent(
                fullscreen = false,
                pin = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = { pinClick = it }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(false, pinClick)
    }

    @Test
    fun testOnPinClickTrue() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        var pinClick: Boolean? = null
        composeTestRule.setContent {
            VStreamMenuContent(
                fullscreen = false,
                pin = true,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = { pinClick = it }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, pinClick)
    }
}