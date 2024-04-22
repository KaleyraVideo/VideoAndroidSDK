package com.kaleyra.video_sdk.ui.call.callactionnew

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.callactionnew.AudioAction
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioActionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testButtonTextIsNotDisplayedOnDefaultWidth() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            AudioAction(
                audioDevice = AudioDeviceUi.LoudSpeaker,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun testLabelIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            AudioAction(
                audioDevice = AudioDeviceUi.LoudSpeaker,
                label = true,
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testButtonTextIsDisplayedOnWideWidth() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            AudioAction(
                audioDevice = AudioDeviceUi.LoudSpeaker,
                modifier = Modifier.width(200.dp),
                onClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        var clicked = false
        composeTestRule.setContent {
            AudioAction(audioDevice = AudioDeviceUi.LoudSpeaker, onClick = { clicked = true })
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testClickOnButtonDisabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.setContent {
            AudioAction(
                audioDevice = AudioDeviceUi.LoudSpeaker,
                enabled = false,
                onClick = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsNotEnabled()
    }
}