@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.performance

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.deviceThermalManager
import com.kaleyra.video_utils.thermal.DeviceThermalManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class CallPerformanceManagerTest {

    private val myAudio = mockk<Input.Audio.My>(relaxed = true)

    private val myVideo = mockk<Input.Video.My>(relaxed = true)

    private val myStream = mockk<Stream.Mutable> {
        every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { video } returns MutableStateFlow(myVideo)
        every { audio } returns MutableStateFlow(myAudio)
    }

    private val meParticipant = mockk<CallParticipant.Me> {
        every { streams } returns MutableStateFlow(listOf(myStream))
    }

    private val participantsMock = mockk<CallParticipants> {
        every { me } returns meParticipant
    }

    private val callMock: Call = mockk<Call> {
        every { type } returns MutableStateFlow(Call.Type.audioVideo())
        every { state } returns MutableStateFlow(Call.State.Connected)
        every { participants } returns MutableStateFlow(participantsMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun throttlingStatusUnknown_blurVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusNone_blurVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusLight_blurVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusModerate_blurVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusSevere_notDisabledBlurVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusCritical_disabledBlurVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusEmergency_disabledBlurVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusShutdown_disabledBlurVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blur", factor = 0.5f))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    // image virtual background

    @Test
    fun throttlingStatusUnknown_ImageVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusNone_ImageVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusLight_ImageVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusModerate_ImageVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusSevere_notDisabledImageVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusCritical_disabledImageVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusEmergency_disabledImageVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusShutdown_disabledImageVirtualBackground() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image("image", mockk()))
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    // no virtual background

    @Test
    fun throttlingStatusUnknown_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusNone_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusLight_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusModerate_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusSevere_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusCritical_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusEmergency_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun throttlingStatusShutdown_noneVirtualBackgroundNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myVideo.tryApplyEffect(Effect.Video.None) }
    }

    // audio ai noise filter

    @Test
    fun throttlingStatusUnknown_aiNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusNone_aiNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusLight_aiNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusModerate_aiNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusSevere_aiNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard) }
    }

    @Test
    fun throttlingStatusCritical_aiNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard) }
    }

    @Test
    fun throttlingStatusEmergency_aiNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard) }
    }

    @Test
    fun throttlingStatusShutdown_aiNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.DeepFilterAi)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 1) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard) }
    }


    // audio standard noise filter

    @Test
    fun throttlingStatusUnknown_standardNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusNone_standardNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusLight_standardNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusModerate_standardNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusSevere_standardNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusCritical_standardNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusEmergency_standardNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusShutdown_standardNoiseFilterDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Standard)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    // disabled noise filter

    // audio standard noise filter

    @Test
    fun throttlingStatusUnknown_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.UNKNOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusNone_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.NONE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusLight_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.LIGHT)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusModerate_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.MODERATE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusSevere_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SEVERE)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusCritical_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.CRITICAL)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusEmergency_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.EMERGENCY)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }

    @Test
    fun throttlingStatusShutdown_noNoiseFilterNotDisabled() = runTest {
        callMock.deviceThermalManager = mockk {
            every { throttlingStatus } returns MutableStateFlow(DeviceThermalManager.ThrottlingStatus.SHUTDOWN)
        }
        every { myAudio.noiseFilterMode } returns MutableStateFlow(Input.Audio.My.NoiseFilterMode.Disabled)
        every { myVideo.currentEffect } returns MutableStateFlow(Effect.Video.None)
        CallPerformanceManager(backgroundScope).bind(callMock)

        runCurrent()

        verify(exactly = 0) { myAudio.setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Disabled) }
    }
}