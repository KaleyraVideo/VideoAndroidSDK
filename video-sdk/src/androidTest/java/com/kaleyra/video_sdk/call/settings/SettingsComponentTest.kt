@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Effects
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_sdk.Mocks.callMock
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toAvailableAudioDevicesUi
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionDeepFilterOptionTag
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionSettingsTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundBlurOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundImageOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundNoneOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundSettingsTag
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsTag
import com.kaleyra.video_sdk.call.settings.viewmodel.NoiseFilterViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var noiseFilterViewModel: NoiseFilterViewModel
    private lateinit var virtualBackgroundViewModel: VirtualBackgroundViewModel
    private lateinit var audioOutputViewModel: AudioOutputViewModel

    private var onChangeAudioOutputRequested = false

    @Before
    fun setUp() {
        ContextRetainer().create(composeTestRule.activity.applicationContext)
        mockkObject(NoiseFilterMapper)
        every { NoiseFilterMapper.getSupportedNoiseFilterModes() } returns listOf(NoiseFilterModeUi.Standard, NoiseFilterModeUi.DeepFilterAi)
        mockkObject(AudioOutputMapper)
        every { callMock.toCurrentAudioDeviceUi() } returns MutableStateFlow(AudioDeviceUi.LoudSpeaker)
        every { callMock.toAvailableAudioDevicesUi() } returns MutableStateFlow(listOf(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.EarPiece))
        mockkObject(VirtualBackgroundMapper)
        every { callMock.toVirtualBackgroundsUi() } returns MutableStateFlow(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("blur"), VirtualBackgroundUi.Image("image")))
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns mockk {
                every { streams } returns MutableStateFlow(listOf(mockk {
                    every { id } returns CAMERA_STREAM_ID
                    val noiseFilterModeFlow = MutableStateFlow<Input.Audio.My.NoiseFilterMode>(Input.Audio.My.NoiseFilterMode.Standard)
                    every { audio } returns MutableStateFlow<Input.Audio.My?>(mockk(relaxed = true) {
                        every { noiseFilterMode } returns noiseFilterModeFlow
                        every { setNoiseFilterMode(any<Input.Audio.My.NoiseFilterMode>()) } answers {
                            noiseFilterModeFlow.value = firstArg()
                        }
                    })
                    every { video } returns MutableStateFlow(mockk(relaxed = true))
                }))
            }
        })
        every { callMock.effects } returns object : Effects {
            override val preselected: StateFlow<Effect> = MutableStateFlow(Effect.Video.Background.Blur("blur", 0.5f))
            override val available: StateFlow<Set<Effect>> = MutableStateFlow(
                setOf(
                    Effect.Video.Background.Blur(id = "blur", factor = 0.5f),
                    Effect.Video.Background.Image(id = "image", image = mockk(relaxed = true)),
                    Effect.Video.None
                )
            )

        }
        virtualBackgroundViewModel = spyk(VirtualBackgroundViewModel({ Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }, mockk()))
        noiseFilterViewModel = spyk(NoiseFilterViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })
        audioOutputViewModel = spyk(AudioOutputViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

    }

    @After
    fun tearDown() {
        unmockkAll()
        onChangeAudioOutputRequested = false
    }

    @Test
    fun testSettingsComponentLoaded() = runTest {
        lateinit var scrollState: ScrollState
        composeTestRule.setContent {
            scrollState = rememberScrollState()
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = scrollState,
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        advanceUntilIdle()

        with(composeTestRule) {
            onNodeWithTag(VoiceSettingsTag).assertIsDisplayed()
            onNodeWithTag(NoiseSuppressionSettingsTag).assertIsDisplayed()
            scrollState.scrollTo(100)
            onNodeWithTag(VirtualBackgroundSettingsTag).assertIsDisplayed()
        }
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionTextCLicked_deepFilterNetNoiseFilterModeSetOnViewModel() {
        composeTestRule.setContent {
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = rememberScrollState(),
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        composeTestRule.onNodeWithTag(NoiseSuppressionDeepFilterOptionTag).performClick()

        verify { noiseFilterViewModel.setNoiseSuppressionMode(NoiseFilterModeUi.DeepFilterAi) }
    }

    @Test
    fun testStandardNoiseFilterOptionTextCLicked_standardModeSetOnViewModel() {
        composeTestRule.setContent {
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = rememberScrollState(),
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_strings_action_noise_suppression_standard)).performClick()

        verify { noiseFilterViewModel.setNoiseSuppressionMode(NoiseFilterModeUi.Standard) }
    }

    @Test
    fun testNoneVirtualBackgroundClicked_noneCameraEffectsSet() = runTest {
        lateinit var scrollState: ScrollState
        composeTestRule.setContent {
            scrollState = rememberScrollState()
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = scrollState,
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        scrollState.scrollTo(400)
        composeTestRule.onNodeWithTag(VirtualBackgroundNoneOptionTag).performClick()

        verify { virtualBackgroundViewModel.setEffect(VirtualBackgroundUi.None) }
    }

    @Test
    fun testBlurVirtualBackgroundClicked_blurCameraEffectsSet() = runTest {
        lateinit var scrollState: ScrollState
        composeTestRule.setContent {
            scrollState = rememberScrollState()
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = scrollState,
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        scrollState.scrollTo(400)
        composeTestRule.onNodeWithTag(VirtualBackgroundBlurOptionTag).performClick()

        verify { virtualBackgroundViewModel.setEffect(VirtualBackgroundUi.Blur("blur")) }
    }

    @Test
    fun testImageVirtualBackgroundClicked_imageCameraEffectsSet() = runTest {
        lateinit var scrollState: ScrollState
        composeTestRule.setContent {
            scrollState = rememberScrollState()
            SettingsComponent(
                audioOutputViewModel,
                noiseFilterViewModel,
                virtualBackgroundViewModel,
                onDismiss = {},
                onUserMessageActionClick = { },
                onChangeAudioOutputRequested = { },
                scrollState = scrollState,
                modifier = Modifier,
                isLargeScreen = false
            )
        }

        scrollState.scrollTo(400)
        composeTestRule.onNodeWithTag(VirtualBackgroundImageOptionTag).performClick()

        verify { virtualBackgroundViewModel.setEffect(VirtualBackgroundUi.Image("image")) }
    }
}
