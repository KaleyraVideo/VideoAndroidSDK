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

package com.kaleyra.video_sdk.mapper.call

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.Contact
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.UsbCameraUtils
import com.kaleyra.video_extension_audio.extensions.AudioOutputConnectionError
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasAudio
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasCameraUsageRestriction
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasUsbCamera
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioVideo
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isSharingScreen
import com.kaleyra.video_sdk.call.mapper.InputMapper.isUsbCameraWaitingPermission
import com.kaleyra.video_sdk.call.mapper.InputMapper.toAudioConnectionFailureMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toMutedMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toUsbCameraMessage
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class InputMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val videoMock = mockk<Input.Video.Camera.Internal>()

    private val usbMock = mockk<Input.Video.Camera.Usb>()

    private val audioMock = mockk<Input.Audio.My>()

    private val streamMock = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val inputsMock = mockk<Inputs>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns participantMeMock
        })
        every { callMock.inputs } returns inputsMock
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
        with(streamMock) {
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
        }
    }

    @Test
    fun cameraStreamVideoEnabled_isMyCameraEnabled_true() = runTest {
        every { streamMock.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { videoMock.enabled } returns MutableStateFlow(Input.Enabled.Both)
        val result = callMock.isMyCameraEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun cameraStreamVideoDisabled_isMyCameraEnabled_false() = runTest {
        every { streamMock.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { videoMock.enabled } returns MutableStateFlow(Input.Enabled.None)
        val result = callMock.isMyCameraEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun cameraStreamVideoNull_isMyCameraEnabled_null() = runTest {
        with(streamMock) {
            every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(null)
        }
        val myCameraFlow = callMock.isMyCameraEnabled()

        launch {
            val result = withTimeoutOrNull(1000L) {
                myCameraFlow.first()
            }
            assertEquals(null, result)
        }
    }

    @Test
    fun cameraStreamNotExists_isMyCameraEnabled_null() = runTest {
        every { streamMock.id } returns "streamId"
        every { videoMock.enabled } returns MutableStateFlow(Input.Enabled.None)
        val myCameraFlow = callMock.isMyCameraEnabled()
        launch {
            val result = withTimeoutOrNull(1000L) {
                myCameraFlow.first()
            }
            assertEquals(null, result)
        }
    }

    @Test
    fun cameraStreamAudioEnabled_isMyMicEnabled_true() = runTest {
        every { streamMock.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { audioMock.enabled } returns MutableStateFlow(Input.Enabled.Both)
        val result = callMock.isMyMicEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun cameraStreamNotExists_isMyMicEnabled_null() = runTest {
        every { streamMock.id } returns "streamId"
        val myMicFlow = callMock.isMyMicEnabled()

        launch {
            val result = withTimeoutOrNull(1000L) {
                myMicFlow.first()
            }
            assertEquals(null, result)
        }
    }

    @Test
    fun cameraStreamAudioNull_isMyMicEnabled_false() = runTest {
        with(streamMock) {
            every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        val myMicFlow = callMock.isMyMicEnabled()

        launch {
            val result = withTimeoutOrNull(1000L) {
                myMicFlow.first()
            }
            assertEquals(null, result)
        }
    }

    @Test
    fun cameraStreamAudioDisable_isMyMicEnabled_false() = runTest {
        every { streamMock.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { audioMock.enabled } returns MutableStateFlow(Input.Enabled.None)
        val result = callMock.isMyMicEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun sharingStreamInStreamList_isSharingScreen_true() = runTest {
        every { streamMock.id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
        val result = callMock.isSharingScreen()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun sharingStreamNotInStreamList_isSharingScreen_false() = runTest {
        every { streamMock.id } returns "id"
        val result = callMock.isSharingScreen()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasVideoNull_isAudioOnly_true() = runTest {
        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        val result = callMock.isAudioOnly()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun preferredTypeChanged_isAudioOnly_changes() = runTest {
        val preferredTypeFlow =  MutableStateFlow(Call.PreferredType.audioOnly())
        every { callMock.preferredType } returns preferredTypeFlow
        val result = callMock.isAudioOnly()
        val actual = result.first()
        assertEquals(true, actual)
        preferredTypeFlow.value = Call.PreferredType.audioVideo()
        val new = result.first()
        assertEquals(false, new)
    }

    @Test
    fun preferredTypeHasVideo_isAudioOnly_false() = runTest {
        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        val result = callMock.isAudioOnly()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasVideoEnabled_isAudioVideo_true() = runTest {
        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioVideo())
        val result = callMock.isAudioVideo()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun preferredTypeChanged_isAudioVideo_changes() = runTest {
        val preferredTypeFlow =  MutableStateFlow(Call.PreferredType.audioOnly())
        every { callMock.preferredType } returns preferredTypeFlow
        val result = callMock.isAudioVideo()
        val actual = result.first()
        assertEquals(false, actual)
        preferredTypeFlow.value = Call.PreferredType.audioVideo()
        val new = result.first()
        assertEquals(true, new)
    }

    @Test
    fun preferredTypeHasVideoDisabled_isAudioVideo_false() = runTest {
        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioUpgradable())
        val result = callMock.isAudioVideo()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasAudio_hasAudio_true() = runTest {
        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioVideo())
        val result = callMock.hasAudio()
        val actual = result.first()
        assertEquals(true, actual)
    }



    @Test
    fun inputAudioRequestMute_toMutedMessage_adminDisplayName() = runTest {
        mockkObject(ContactDetailsManager)
        val event = mockk<Input.Audio.Event.Request.Mute>()
        val producer = mockk<CallParticipant>()
        every { producer.combinedDisplayName } returns MutableStateFlow("username")
        every { event.producer } returns producer
        every { streamMock.id } returns CameraStreamConstants.CAMERA_STREAM_ID
        every { audioMock.events } returns MutableStateFlow(event)
        val result = callMock.toMutedMessage()
        val actual = result.first()
        assertEquals("username", actual.admin)
    }

    @Test
    fun usbCameraNotInAvailableInputs_hasUsbCamera_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        val actual = callMock.hasUsbCamera().first()
        assertEquals(false, actual)
    }

    @Test
    fun usbCameraInAvailableInputs_hasUsbCamera_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        val actual = callMock.hasUsbCamera().first()
        assertEquals(true, actual)
    }

    @Test
    fun usbCameraNotInAvailableInputs_toUsbCameraMessage_usbCameraDisconnectedMessage() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        val actual = callMock.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.Disconnected)
    }

    @Test
    fun usbCameraInAvailableInputsAndItIsSupported_toUsbCameraMessage_usbCameraConnectedMessage() = runTest {
        mockkObject(UsbCameraUtils)
        every { UsbCameraUtils.isSupported() } returns true
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>(relaxed = true)))
        val actual = callMock.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.Connected)
    }

    @Test
    fun usbCameraNotSupported_toUsbCameraMessage_usbCameraNotSupportedMessage() = runTest {
        mockkObject(UsbCameraUtils)
        every { UsbCameraUtils.isSupported() } returns false
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>(relaxed = true)))
        val actual = callMock.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.NotSupported)
    }

    @Test
    fun audioConnectionGenericError_toAudioConnectionFailureMessage_genericAudioConnectionFailure() = runTest {
        mockkObject(CollaborationAudioExtensions)
        with(CollaborationAudioExtensions) {
            every { callMock.failedAudioOutputDevice } returns MutableStateFlow(AudioOutputConnectionError(AudioOutputDevice.Loudspeaker(), isInSystemCall = false))
        }
        val actual = callMock.toAudioConnectionFailureMessage().first()
        assert(actual is AudioConnectionFailureMessage.Generic)
    }

    @Test
    fun audioConnectionInCallError_toAudioConnectionFailureMessage_inCallAudioConnectionFailure() = runTest {
        mockkObject(CollaborationAudioExtensions)
        with(CollaborationAudioExtensions) {
            every { callMock.failedAudioOutputDevice } returns MutableStateFlow(AudioOutputConnectionError(AudioOutputDevice.Loudspeaker(), isInSystemCall = true))
        }
        val actual = callMock.toAudioConnectionFailureMessage().first()
        assert(actual is AudioConnectionFailureMessage.InSystemCall)
    }

    @Test
    fun genericVideo_isUsbCameraWaitingPermission_false() = runTest {
        val video = mockk<Input.Video>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(video))
        every { video.state } returns MutableStateFlow(Input.State.Closed.AwaitingPermission)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(false, actual)
    }

    @Test
    fun inputStateIdle_isUsbCameraWaitingPermission_false() = runTest {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbMock))
        every { usbMock.state } returns MutableStateFlow(Input.State.Idle)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(false, actual)
    }

    @Test
    fun inputStateAwaitingPermission_isUsbCameraWaitingPermission_true() = runTest {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbMock))
        every { usbMock.state } returns MutableStateFlow(Input.State.Closed.AwaitingPermission)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(true, actual)
    }

    @Test
    fun inputStateActive_isUsbCameraWaitingPermission_false() = runTest {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbMock))
        every { usbMock.state } returns MutableStateFlow(Input.State.Active)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(false, actual)
    }

    @Test
    fun inputStateClosed_isUsbCameraWaitingPermission_false() = runTest {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbMock))
        every { usbMock.state } returns MutableStateFlow(Input.State.Closed)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(false, actual)
    }

    @Test
    fun inputStateClosedError_isUsbCameraWaitingPermission_false() = runTest {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbMock))
        every { usbMock.state } returns MutableStateFlow(Input.State.Closed.Error)
        val actual = callMock.isUsbCameraWaitingPermission().first()
        assertEquals(false, actual)
    }

    @Test
    fun cameraRestrictionEnabled_hasCameraUsageRestriction_true() = runTest {
        val usage = mockk<Contact.Restrictions.Restriction.Camera> {
            every { usage } returns true
        }
        every { participantMeMock.restrictions } returns mockk {
            every { camera } returns MutableStateFlow(usage)
        }

        val actual = callMock.hasCameraUsageRestriction().first()
        assertEquals(true, actual)
    }

    @Test
    fun cameraRestrictionNotEnabled_hasCameraUsageRestriction_false() = runTest {
        val usage = mockk<Contact.Restrictions.Restriction.Camera> {
            every { usage } returns false
        }
        every { participantMeMock.restrictions } returns mockk {
            every { camera } returns MutableStateFlow(usage)
        }

        val actual = callMock.hasCameraUsageRestriction().first()
        assertEquals(false, actual)
    }
}
