package com.kaleyra.video_sdk.ui.call.bottomsheetnew.inputmessage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.model.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageDuration
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageHost
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InputMessageHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testMicMessageEnabled() {
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_microphone)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            InputMessageHost(inputMessage = MicMessage.Enabled)
        }
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration - 200)
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithText(microphone).assertDoesNotExist()
        composeTestRule.onNodeWithText(on).assertDoesNotExist()
    }

    @Test
    fun testMicMessageDisabled() {
        val microphone = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_microphone)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            InputMessageHost(inputMessage = MicMessage.Disabled)
        }
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration - 200)
        composeTestRule.onNodeWithText(microphone).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithText(microphone).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
    }

    @Test
    fun testCameraMessageEnabled() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_camera)
        val on = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_on)
        composeTestRule.setContent {
            InputMessageHost(inputMessage = CameraMessage.Enabled)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration - 200)
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(on).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(on).assertDoesNotExist()
    }

    @Test
    fun testCameraMessageDisabled() {
        val camera = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_camera)
        val off = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_off)
        composeTestRule.setContent {
            InputMessageHost(inputMessage = CameraMessage.Disabled)
        }
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(InputMessageDuration - 200)
        composeTestRule.onNodeWithText(camera).assertIsDisplayed()
        composeTestRule.onNodeWithText(off).assertIsDisplayed()
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.onNodeWithText(camera).assertDoesNotExist()
        composeTestRule.onNodeWithText(off).assertDoesNotExist()
    }
}