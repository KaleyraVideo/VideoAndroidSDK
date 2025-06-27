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

package com.kaleyra.video_sdk.call.voice_settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass

class ModalVoiceSettingsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var audioOutputViewModel = mockk<AudioOutputViewModel>(relaxed = true)

    private var audioOutputUiState = MutableStateFlow(AudioOutputUiState())

    @Before
    fun setUp() {
        mockkObject(AudioOutputViewModel)
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<AudioOutputViewModel>>(), any()) } returns audioOutputViewModel
        }
        every { audioOutputViewModel.uiState } returns audioOutputUiState
    }

    @Test
    fun testDisableAllCallSoundsClicked_viewModelCalled() {
        audioOutputUiState.value = AudioOutputUiState(areCallSoundsEnabled = true)
        var isDismissed = false
        composeTestRule.setContent {
            ModalVoiceSettingsComponent(
                modifier = Modifier,
                viewModel = audioOutputViewModel,
                onChangeAudioOutputRequested = {},
                onDismiss = { isDismissed = true },
            )
        }
        val disableAllCallSounds = composeTestRule.activity.getString(R.string.kaleyra_strings_action_disable_all_sounds)

        composeTestRule.onNodeWithText(disableAllCallSounds).performClick()
        verify(exactly = 1) { audioOutputViewModel.disableCallSounds() }

        assertEquals(false, isDismissed)
    }

    @Test
    fun testEnableAllCallSoundsClicked_viewModelCalled() {
        audioOutputUiState.value = AudioOutputUiState(areCallSoundsEnabled = false)
        var isDismissed = false
        composeTestRule.setContent {
            ModalVoiceSettingsComponent(
                modifier = Modifier,
                viewModel = audioOutputViewModel,
                onChangeAudioOutputRequested = {},
                onDismiss = { isDismissed = true },
            )
        }
        val disableAllCallSounds = composeTestRule.activity.getString(R.string.kaleyra_strings_action_disable_all_sounds)

        composeTestRule.onNodeWithText(disableAllCallSounds).performClick()
        verify(exactly = 1) { audioOutputViewModel.enableCallSounds() }

        assertEquals(false, isDismissed)
    }

    @Test
    fun testChangeAudioOutputClicked_onChangeAudioOutputRequestedCallbackCalled() {
        var isDismissed = false
        var onChangeAudioOutputRequested = false
        composeTestRule.setContent {
            ModalVoiceSettingsComponent(
                modifier = Modifier,
                viewModel = audioOutputViewModel,
                onChangeAudioOutputRequested = { onChangeAudioOutputRequested = true },
                onDismiss = { isDismissed = true },
            )
        }
        val changeAudioOutput = composeTestRule.activity.getString(R.string.kaleyra_strings_action_voice_change_audio_output)

        composeTestRule.onNodeWithText(changeAudioOutput).performClick()

        assertEquals(true, onChangeAudioOutputRequested)
        assertEquals(false, isDismissed)
    }

    @Test
    fun settingsTitleDisplayed() {
        composeTestRule.setContent {
            ModalVoiceSettingsComponent(
                modifier = Modifier,
                viewModel = audioOutputViewModel,
                onChangeAudioOutputRequested = {},
                onDismiss = {},
            )
        }
        val title = composeTestRule.activity.getString(R.string.kaleyra_strings_action_settings)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        var isCloseClicked = false
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.setContent {
            ModalVoiceSettingsComponent(
                modifier = Modifier,
                viewModel = audioOutputViewModel,
                onChangeAudioOutputRequested = {},
                onDismiss = { isCloseClicked = true },
            )
        }
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isCloseClicked)
    }
}
