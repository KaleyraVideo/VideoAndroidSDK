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

import com.kaleyra.video.conference.*
import com.kaleyra.video.conference.Call.PreferredType
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.call.CameraStreamManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraStreamManagerTest {

    private val callMock = mockk<Call>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val myStream = mockk<Stream.Mutable>(relaxed = true)

    @Before
    fun setUp() {
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
    fun handleCameraStreamVideo_streamUpdatedOnMyCameraVideoInput() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))

        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = meMock.streams.value.first().video.value
        assertEquals(videoMock, actual)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnMeParticipantUpdated() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        val participantsWithMeNull = mockk<CallParticipants>(relaxed = true) {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(participantsWithMeNull)
        every { callMock.participants } returns participantsFlow

        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = meMock.streams.value.first().video.value
        assertEquals(null, actual)
        participantsFlow.value = participantsMock
        val new = meMock.streams.value.first().video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_streamUpdatedOnPreferredTypeChanged() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        val preferredType = MutableStateFlow(PreferredType.audioOnly())
        every { callMock.preferredType } returns preferredType

        cameraStreamManager.handleCameraStreamVideo(callMock)

        val actual = meMock.streams.value.first().video.value
        assertEquals(null, actual)
        preferredType.value = PreferredType.audioVideo()
        val new = meMock.streams.value.first().video.value
        assertEquals(videoMock, new)
    }

    @Test
    fun handleCameraStreamVideo_setHDQualityOnCameraVideoStream() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))

        cameraStreamManager.handleCameraStreamVideo(callMock)

        verify { videoMock.setQuality(Input.Video.Quality.Definition.HD, any()) }
    }

    @Test
    fun usbCameraInput_handleCameraStreamVideo_awaitPermissionToSetVideo() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val videoState = MutableStateFlow<Input.State>(Input.State.Closed.AwaitingPermission)
        val videoMock = mockk<Input.Video.Camera.Usb>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(Input.Video.Quality(Input.Video.Quality.Definition.SD, 30))
            every { state } returns videoState
        }
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))

        cameraStreamManager.handleCameraStreamVideo(callMock)

        runCurrent()
        assertEquals(null, meMock.streams.value.first().video.value)

        videoState.value = Input.State.Idle
        runCurrent()
        assertEquals(videoMock, meMock.streams.value.first().video.value)
    }

    @Test
    fun handleCameraStreamAudio_streamUpdatedOnAudioInput() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(audioMock))

        cameraStreamManager.handleCameraStreamAudio(callMock)

        val actual = meMock.streams.value.first().audio.value
        assertEquals(audioMock, actual)
    }

    @Test
    fun handleCameraStreamAudio_streamUpdatedOnMeParticipantUpdated() = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(audioMock))
        val participantsWithMeNull = mockk<CallParticipants>(relaxed = true) {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(participantsWithMeNull)
        every { callMock.participants } returns participantsFlow

        cameraStreamManager.handleCameraStreamAudio(callMock)

        val actual = meMock.streams.value.first().audio.value
        assertEquals(null, actual)
        participantsFlow.value = participantsMock
        val new = meMock.streams.value.first().audio.value
        assertEquals(audioMock, new)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnCameraVideoInputWhenCallIsAudioOnly() {
        every { callMock.preferredType } returns MutableStateFlow(PreferredType.audioOnly())
        val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnApplicationVideoInput() {
        val videoMock = mockk<Input.Video.Application>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnScreenVideoInput() {
        val videoMock = mockk<Input.Video.Screen>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    @Test
    fun handleCameraStreamVideo_streamNotUpdatedOnCustomVideoInput() {
        val videoMock = mockk<Input.Video.Custom>(relaxed = true) {
            every { currentQuality } returns MutableStateFlow(mockk(relaxed = true))
        }
        checkStreamNotUpdatedOnVideoInput(videoMock)
    }

    private fun checkStreamNotUpdatedOnVideoInput(videoMock: Input.Video) = runTest(UnconfinedTestDispatcher()) {
        val cameraStreamManager = CameraStreamManager(backgroundScope)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))
        cameraStreamManager.handleCameraStreamVideo(callMock)
        val actual = meMock.streams.value.first().video.value
        assertEquals(null, actual)
    }

}