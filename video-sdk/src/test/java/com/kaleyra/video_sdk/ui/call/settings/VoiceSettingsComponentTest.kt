package com.kaleyra.video_sdk.ui.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.settings.view.ChangeAudioOutputTestTag
import com.kaleyra.video_sdk.call.settings.view.MuteAllSoundsSwitchTestTag
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsTag
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
        composeTestRule.onNodeWithTag(MuteAllSoundsSwitchTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ChangeAudioOutputTestTag).assertIsDisplayed()
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

        composeTestRule.onNodeWithTag(MuteAllSoundsSwitchTestTag).performClick()

        Assert.assertTrue(hasRequestedMuteAllSounds)
    }

    @Test
    fun testDisableAndEnableAllSoundClicked_mutedAndEnabledAudioDeviceUiSetOnViewModel() {
        composeTestRule.setContent {
            VoiceSettingsComponent(
                AudioOutputUiState(),
                {
                    hasRequestedMuteAllSounds = it
                },
                {
                    hasRequestedAudioOutputChange = true
                }
            )
        }

        val switch = composeTestRule.onNodeWithTag(MuteAllSoundsSwitchTestTag)

        switch.performClick()
        Assert.assertTrue(hasRequestedMuteAllSounds)

        switch.performClick()
        Assert.assertFalse(hasRequestedMuteAllSounds)
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

        composeTestRule.onNodeWithTag(ChangeAudioOutputTestTag).performClick()

        Assert.assertTrue(hasRequestedAudioOutputChange)
    }
}
