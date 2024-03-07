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

package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Call.PreferredType
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.call.CameraStreamManager
import com.kaleyra.video_common_ui.mapper.InputMapper
import com.kaleyra.video_common_ui.mapper.InputMapper.toAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraVideoInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_common_ui.utils.DeviceUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraStreamManagerTest {

    private val callMock = mockk<Call>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val myStream = mockk<Stream.Mutable>(relaxed = true)

    private val callFlow = flowOf(callMock)

    @Before
    fun setUp() {
        mockkObject(DeviceUtils, InputMapper)
        mockkStatic("kotlinx.coroutines.flow.FlowKt")
        every { DeviceUtils.isSmartGlass } returns false
        every { flowOf<Call>(any()) } returns callFlow
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { preferredType } returns MutableStateFlow(PreferredType.audioVideo())
        }
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStream))
        with(myStream) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
            every { video } returns MutableStateFlow(null)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBind() = runTest {
        val cameraStreamManager = spyk(CameraStreamManager(backgroundScope))
        cameraStreamManager.bind(callMock)
        verifyOrder {
            cameraStreamManager.stop()
            cameraStreamManager.addCameraStream(callMock)
            cameraStreamManager.handleCameraStreamAudio(callMock)
            cameraStreamManager.handleCameraStreamVideo(callMock)
        }
    }

    @Test
    fun testStop() = runTest {
        val cameraStreamManager = spyk(CameraStreamManager(this))
        cameraStreamManager.bind(callMock)
        cameraStreamManager.stop()
    }

    @Test
    fun meIsNull_addCameraStream_waitForMeToAddStreamIsCalled() = runTest {
        val cameraStreamManager = CameraStreamManager(this)
        val nullMeParticipantsMock = mockk<CallParticipants>(relaxed = true) {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(nullMeParticipantsMock)
        every { callMock.participants } returns participantsFlow
        every { meMock.streams } returns MutableStateFlow(listOf())

        cameraStreamManager.addCameraStream(callMock)
        runCurrent()

        verify(exactly = 0) { meMock.addStream(CAMERA_STREAM_ID) }

        participantsFlow.value = participantsMock
        runCurrent()

        verify(exactly = 1) { meMock.addStream(CAMERA_STREAM_ID) }
    }

    @Test
    fun streamNotExists_addCameraStream_addStreamIsCalled() = runTest {
        val cameraStreamManager = CameraStreamManager(this)
        every { meMock.streams } returns MutableStateFlow(listOf())
        every { callMock.participants } returns MutableStateFlow(participantsMock)

        cameraStreamManager.addCameraStream(callMock)
        runCurrent()

        verify(exactly = 1) { meMock.addStream(CAMERA_STREAM_ID) }
    }

    @Test
    fun streamAlreadyExists_addCameraStream_addStreamIsNotPerformed() = runTest {
        val cameraStreamManager = CameraStreamManager(this)
        val streamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
        }
        every { meMock.streams } returns MutableStateFlow(listOf(streamMock))
        every { callMock.participants } returns MutableStateFlow(participantsMock)

        cameraStreamManager.addCameraStream(callMock)
        runCurrent()

        verify(exactly = 0) { meMock.addStream(CAMERA_STREAM_ID) }
    }

    @Test
    fun handleCameraStreamAudio_streamUpdatedOnAudioInput() = runTest(UnconfinedTestDispatcher()) {
        val audio = mockk<Input.Audio>(relaxed = true)
        val audioFlow = MutableStateFlow(audio)
        every { callFlow.toAudioInput() } returns audioFlow
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamAudio(callMock)

        val actual = myStream.audio.value
        assertEquals(audio, actual)

        val newAudio = mockk<Input.Audio>(relaxed = true)
        audioFlow.value = newAudio

        val new = myStream.audio.value
        assertEquals(newAudio, new)
    }

    @Test
    fun handleCameraStreamAudio_streamUpdatedOnMyCameraStreamUpdated() = runTest(UnconfinedTestDispatcher()) {
        val audio = mockk<Input.Audio>(relaxed = true)
        val streamFlow = MutableStateFlow(myStream)
        every { callFlow.toAudioInput() } returns MutableStateFlow(audio)
        every { callFlow.toMyCameraStream() } returns streamFlow

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamAudio(callMock)

        val actual = myStream.audio.value
        assertEquals(audio, actual)

        val newStream = mockk<Stream.Mutable> {
            every { this@mockk.audio } returns MutableStateFlow(null)
        }
        streamFlow.value = newStream

        val new = newStream.audio.value
        assertEquals(audio, new)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnMyCameraVideoInput() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        val cameraFlow = MutableStateFlow(videoMock)
        every { callFlow.toCameraVideoInput() } returns cameraFlow
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = myStream.video.value
        assertEquals(videoMock, actual)

        val newVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        cameraFlow.value = newVideoMock

        val new = myStream.video.value
        assertEquals(newVideoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnMeParticipantUpdated() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        val streamFlow = MutableStateFlow(myStream)
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns streamFlow

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = myStream.video.value
        assertEquals(videoMock, actual)

        val newStream = mockk<Stream.Mutable> {
            every { this@mockk.video } returns MutableStateFlow(null)
        }
        streamFlow.value = newStream

        val new = newStream.video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnPreferredTypeChanged() = runTest(UnconfinedTestDispatcher()) {
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        val preferredType = MutableStateFlow(PreferredType.audioOnly())
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)
        every { callMock.preferredType } returns preferredType

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = myStream.video.value
        assertEquals(null, actual)

        preferredType.value = PreferredType.audioVideo()

        val new = myStream.video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun smartglassDevice_handleCameraStreamVideo_setSDQualityOnCameraVideoStream() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
        }
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        verify { videoMock.setQuality(Input.Video.Quality.Definition.HD, any()) }
    }

    @Test
    fun smartphoneDevice_handleCameraStreamVideo_setHDQualityOnCameraVideoStream() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.HD, 30))
        }
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        verify { videoMock.setQuality(Input.Video.Quality.Definition.SD, any()) }
    }

    @Test
    fun usbCameraInput_handleCameraStreamVideo_awaitPermissionToSetVideo() = runTest(UnconfinedTestDispatcher()) {
        val videoState = MutableStateFlow<Input.State>(Input.State.Closed.AwaitingPermission)
        val videoMock = mockk<Input.Video.Camera.Usb>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
            every { state } returns videoState
        }
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = myStream.video.value
        assertEquals(null, actual)

        videoState.value = Input.State.Idle

        val new = myStream.video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnCameraVideoInputWhenCallIsAudioOnly() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.preferredType } returns MutableStateFlow(PreferredType.audioOnly())
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callFlow.toCameraVideoInput() } returns MutableStateFlow(videoMock)
        every { callFlow.toMyCameraStream() } returns MutableStateFlow(myStream)

        val cameraStreamManager = CameraStreamManager(backgroundScope)
        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = myStream.video.value
        assertEquals(null, actual)
    }
}