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

package com.kaleyra.video_common_ui.mapper

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.mapper.InputMapper.hasActiveVirtualBackground
import com.kaleyra.video_common_ui.mapper.InputMapper.hasAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasInternalCameraInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.mapper.InputMapper.isAnyScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isAppScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isDeviceScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.toAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraStreamAudio
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraVideoInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toMuteEvents
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.robolectric.shadows.ShadowLog.stream

class InputMapperTests {

    private val activeMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeScreenInputMock = mockk<Input.Video.Screen.My> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveScreenInputMock = mockk<Input.Video.Screen.My> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }

    private val callMock = mockk<Call>()
    
    private val audioMock = mockk<Input.Audio>()

    private val streamMock = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns participantMeMock
        })
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
        with(streamMock) {
            every { audio } returns MutableStateFlow(audioMock)
        }
    }

    @Test
    fun noInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        Assert.assertEquals(false, callMock.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun wrongInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(false, callMock.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun expectedInput_isInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(true, callMock.isInputActive<Input.Audio>().first())
    }

    @Test
    fun expectedInputNotActive_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveMicrophoneInputMock))
        Assert.assertEquals(false, callMock.isInputActive<Input.Audio>().first())
    }

    @Test
    fun deviceScreenActive_isDeviceScreenInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callMock.isDeviceScreenInputActive().first())
    }

    @Test
    fun deviceScreenNotActive_isDeviceScreenInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock))
        Assert.assertEquals(false, callMock.isDeviceScreenInputActive().first())
    }

    @Test
    fun applicationScreenActive_isDeviceApplicationInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeApplicationInputMock))
        Assert.assertEquals(true, callMock.isAppScreenInputActive().first())
    }

    @Test
    fun applicationScreenNotActive_isDeviceApplicationInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveApplicationInputMock))
        Assert.assertEquals(false, callMock.isAppScreenInputActive().first())
    }

    @Test
    fun deviceScreenActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callMock.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock, activeApplicationInputMock))
        Assert.assertEquals(true, callMock.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceNotActive_isAnyScreenInputActive_false()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock, inactiveApplicationInputMock))
        Assert.assertEquals(false, callMock.isAnyScreenInputActive().first())
    }

    @Test
    fun cameraStreamAudio_toCameraStreamAudio_streamAudio() = runTest {
        with(streamMock) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        val result = callMock.toCameraStreamAudio()
        val actual = result.first()
        Assert.assertEquals(audioMock, actual)
    }

    @Test
    fun cameraStreamAudioNull_toCameraStreamAudio_null() = runTest {
        with(streamMock) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        val result = callMock.toCameraStreamAudio()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun cameraStreamNotFound_toCameraStreamAudio_null() = runTest {
        every { streamMock.id } returns "randomId"
        val result = callMock.toCameraStreamAudio()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun inputAudioRequestMute_toMuteEvent_inputEvent() = runTest {
        val event = mockk<Input.Audio.Event.Request.Mute>()
        every { streamMock.id } returns CAMERA_STREAM_ID
        every { audioMock.events } returns MutableStateFlow(event)
        val result = callMock.toMuteEvents()
        val actual = result.first()
        Assert.assertEquals(event, actual)
    }

    @Test
    fun screenShareInAvailableInputs_hasScreenSharingInput_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Screen.My>()))
        val result = callMock.hasScreenSharingInput()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun screenShareNotInAvailableInputs_hasScreenSharingInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Application>()))
        val result = callMock.hasScreenSharingInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun internalCameraInAvailableInputs_hasInternalCameraInput_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        val result = callMock.hasInternalCameraInput()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun usbCameraInAvailableInputs_hasInternalCameraInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        val result = callMock.hasInternalCameraInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun genericCameraInAvailableInputs_hasInternalCameraInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera>()))
        val result = callMock.hasInternalCameraInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun internalCameraNotInAvailableInputs_hasInternalCameraInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input>()))
        val result = callMock.hasInternalCameraInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun audioInAvailableInputs_hasAudioInput_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Audio>()))
        val result = callMock.hasAudioInput()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun audioNotInAvailableInputs_hasAudioInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input>()))
        val result = callMock.hasAudioInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun noAvailableInput_toAudioInput_null() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        val result = callMock.toAudioInput()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun audioAvailable_toAudioInput_audioInput() = runTest {
        val audio = mockk<Input.Audio>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(audio))
        val result = callMock.toAudioInput()
        val actual = result.first()
        Assert.assertEquals(audio, actual)
    }

    @Test
    fun noAvailableInput_toCameraVideoInput_null() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        val result = callMock.toCameraVideoInput()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun internalCameraAvailable_toCameraVideoInput_videoInput() = runTest {
        val video = mockk<Input.Video.Camera.Internal>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(video))
        val result = callMock.toCameraVideoInput()
        val actual = result.first()
        Assert.assertEquals(video, actual)
    }

    @Test
    fun usbCameraAvailable_toCameraVideoInput_videoInput() = runTest {
        val video = mockk<Input.Video.Camera.Usb>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(video))
        val result = callMock.toCameraVideoInput()
        val actual = result.first()
        Assert.assertEquals(video, actual)
    }

    @Test
    fun applicationVideoAvailable_toCameraVideoInput_null() = runTest {
        val video = mockk<Input.Video.Application>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(video))
        val result = callMock.toCameraVideoInput()
        Assert.assertEquals(null, result.first())
    }

    @Test
    fun screenVideoAvailable_toCameraVideoInput_null() = runTest {
        val video = mockk<Input.Video.Screen>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(video))
        val result = callMock.toCameraVideoInput()
        Assert.assertEquals(null, result.first())
    }

    @Test
    fun customVideoAvailable_toCameraVideoInput_null() = runTest {
        val video = mockk<Input.Video.Custom>()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(video))
        val result = callMock.toCameraVideoInput()
        Assert.assertEquals(null, result.first())
    }

    @Test
    fun cameraStream_toMyCameraStream_stream() = runTest {
        val stream = mockk<Stream.Mutable>()
        every { stream.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { participantMeMock.streams } returns MutableStateFlow(listOf(stream))
        val result = callMock.toMyCameraStream()
        val actual = result.first()
        Assert.assertEquals(stream, actual)
    }

    @Test
    fun cameraStreamHasNoneVideoEffect_hasActiveVirtualBackground_false() = runTest {
        val videoEffect = Effect.Video.None
        val myVideo = mockk<Input.Video.My> {
            every { this@mockk.currentEffect } returns MutableStateFlow(videoEffect)
        }
        val stream = mockk<Stream.Mutable> {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideo)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(stream))

        val hasActiveVirtualBackground = callMock.hasActiveVirtualBackground().first()

        Assert.assertFalse(hasActiveVirtualBackground)
    }

    @Test
    fun cameraStreamHasVideoEffect_hasActiveVirtualBackground_true() = runTest {
        val videoEffect = Effect.Video.Background.Image(id = "id", image = mockk<Uri>())
        val myVideo = mockk<Input.Video.My> {
            every { this@mockk.currentEffect } returns MutableStateFlow(videoEffect)
        }
        val stream = mockk<Stream.Mutable> {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideo)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(stream))

        val hasActiveVirtualBackground = callMock.hasActiveVirtualBackground().first()

        Assert.assertTrue(hasActiveVirtualBackground)
    }
}