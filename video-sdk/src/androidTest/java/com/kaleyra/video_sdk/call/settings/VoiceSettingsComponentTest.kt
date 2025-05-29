package com.kaleyra.video_sdk.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsTag
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VoiceSettingsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private var hasRequestedAudioOutputChange = false
    private var hasRequestedMuteAllSounds = false

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        hasRequestedAudioOutputChange = false
        hasRequestedMuteAllSounds = false
        unmockkAll()
    }

    @Test
    fun testVoiceSettingsDisplayed() {
        composeTestRule.setContent {
            VoiceSettingsComponent(
                AudioOutputUiState(),
                { hasRequestedMuteAllSounds = true },
                { hasRequestedAudioOutputChange = true }
            )
        }

        composeTestRule.onNodeWithTag(VoiceSettingsTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(
                com.kaleyra.video_sdk.R.string.kaleyra_strings_action_disable_all_sounds
            )).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(
                com.kaleyra.video_sdk.R.string.kaleyra_strings_action_voice_change_audio_output
            )).assertIsDisplayed()
    }

    @Test
    fun testDisableAllSoundClicked_mutedAudioDeviceUiSetOnViewModel() {
        composeTestRule.setContent {
            VoiceSettingsComponent(
                AudioOutputUiState(),
                { hasRequestedMuteAllSounds = true },
                { hasRequestedAudioOutputChange = true }
            )
        }

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(
                com.kaleyra.video_sdk.R.string.kaleyra_strings_action_disable_all_sounds
            )).performClick()

        Assert.assertTrue(hasRequestedMuteAllSounds)
    }

    @Test
    fun testChangeAudioOutputClicked_onChangeAudioOutputRequestedCallbackInvoked() {
        composeTestRule.setContent {
            VoiceSettingsComponent(
                AudioOutputUiState(),
                { hasRequestedMuteAllSounds = true },
                { hasRequestedAudioOutputChange = true }
            )
        }

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(
                com.kaleyra.video_sdk.R.string.kaleyra_strings_action_voice_change_audio_output
            )).performClick()

        Assert.assertTrue(hasRequestedAudioOutputChange)
    }
}
