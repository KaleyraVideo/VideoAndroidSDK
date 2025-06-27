/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.ui.call.voice_settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.voice_settings.model.ModalVoiceSettingsUi
import com.kaleyra.video_sdk.call.voice_settings.view.ModalVoiceSettingsContent
import com.kaleyra.video_sdk.call.voice_settings.view.ModalVoiceSettingsItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModalVoiceSettingsContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var modalVoiceSettingsUiTarget: ModalVoiceSettingsUi? = null

    private var audioOutputUiState = AudioOutputUiState()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ModalVoiceSettingsContent (
                items = ImmutableList(ModalVoiceSettingsUi.entries),
                audioOutputUiState = audioOutputUiState,
                onItemClick = { modalVoiceSettingsUiTarget = it }
            )
        }
    }

    @After
    fun tearDown() {
        modalVoiceSettingsUiTarget = null
    }

    @Test
    fun voiceSettingsCallSoundsSwitch_disableAllCallSoundsDisplayed() {
        val disableAllCallSounds = composeTestRule.activity.getString(R.string.kaleyra_strings_action_disable_all_sounds)
        composeTestRule.onNodeWithText(disableAllCallSounds).assertIsDisplayed()
    }

    @Test
    fun voiceSettingsChangeAudioOutput_changeAudioOutputDisplayed() {
        val changeAudioOutput = composeTestRule.activity.getString(R.string.kaleyra_strings_action_voice_change_audio_output)
        composeTestRule.onNodeWithText(changeAudioOutput).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItemDisableAllCallSounds_onItemClickInvoked() {
        val disableAllCallSounds = composeTestRule.activity.getString(R.string.kaleyra_strings_action_disable_all_sounds)
        composeTestRule.onNodeWithText(disableAllCallSounds).performClick()
        Assert.assertEquals(ModalVoiceSettingsUi.CallSoundsSwitch, modalVoiceSettingsUiTarget)
    }

    @Test
    fun userClicksOnItemChangeAudioOutput_onItemClickInvoked() {
        val changeAudioOutput = composeTestRule.activity.getString(R.string.kaleyra_strings_action_voice_change_audio_output)
        composeTestRule.onNodeWithText(changeAudioOutput).performClick()
        Assert.assertEquals(ModalVoiceSettingsUi.AudioDeviceSelection, modalVoiceSettingsUiTarget)
    }
}