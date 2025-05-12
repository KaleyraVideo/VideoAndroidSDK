package com.kaleyra.video_sdk.call.bottomsheet.inputmessage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.FullScreenMessage
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.MicMessage
import org.junit.Rule
import org.junit.Test

class InputMessageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testMicEnabledMessage() {
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_strings_action_microphone)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            MicMessage(enabled = true)
        }
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
    }

    @Test
    fun testMicDisabledMessage() {
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_strings_action_microphone)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            MicMessage(enabled = false)
        }
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
    }

    @Test
    fun testCameraEnabledMessage() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_strings_action_camera)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            CameraMessage(enabled = true)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
    }

    @Test
    fun testCameraDisabledMessage() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_strings_action_camera)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            CameraMessage(enabled = false)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
    }

    @Test
    fun testFullScreenModeEnabledMessage() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_fullscreen)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_active)
        composeTestRule.setContent {
            FullScreenMessage(enabled = true)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
    }

    @Test
    fun testFullScreenModeDisabledMessage() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_fullscreen)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_disabled)
        composeTestRule.setContent {
            FullScreenMessage(enabled = false)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
    }
}