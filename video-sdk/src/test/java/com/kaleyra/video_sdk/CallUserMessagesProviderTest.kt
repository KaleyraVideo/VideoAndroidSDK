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

package com.kaleyra.video_sdk

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Stream
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_common_ui.mapper.StreamMapper.doOthersHaveStreams
import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_common_ui.notification.signature.SignDocumentsVisibilityObserver
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper
import com.kaleyra.video_sdk.call.mapper.RecordingMapper
import com.kaleyra.video_sdk.call.pip.CallUiPipVisibilityObserver
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.DownloadFileMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.SignatureMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUserMessagesProviderTest {

    private val signDocumentsFlow = MutableStateFlow<Set<SignDocument>>(setOf())
    private val filesFlow = MutableStateFlow<Set<SharedFile>>(setOf())

    private val callMock = mockk<CallUI>(relaxed = true) {
        every { participants } returns MutableStateFlow(mockk {
            every { me } returns mockk(relaxed = true) {
                every { streams } returns MutableStateFlow(listOf(
                    mockk {
                        every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
                        every { state } returns MutableStateFlow(Stream.State.Live)
                        every { audio } returns mockk(relaxed = true)
                        every { video } returns mockk(relaxed = true)
                    }
                ))
                every { userId } returns "me"
            }
            every { others } returns listOf(mockk(relaxed = true) { every { userId } returns "other" })
        })
        every { sharedFolder } returns mockk {
            every { signDocuments } returns signDocumentsFlow
            every { files } returns filesFlow
        }
    }

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        mockkObject(InputMapper)
        mockkObject(RecordingMapper)
        mockkObject(KaleyraVideo)
        mockkObject(CallStateMapper)
        mockkObject(com.kaleyra.video_common_ui.mapper.StreamMapper)
        every { KaleyraVideo.conference } returns mockk(relaxed = true)
        signDocumentsFlow.value = setOf()
        filesFlow.value = setOf()
        mockkObject(FileShareVisibilityObserver.Companion)
        every { FileShareVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(SignDocumentsVisibilityObserver.Companion)
        every { FileShareVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallExtensions)
        with(CallExtensions) {
            coEvery { callMock.isCpuThrottling(any()) } returns MutableStateFlow(false)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
        CallUserMessagesProvider.dispose()
    }

    @Test
    fun testStart() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDoubleStart() = runTest {
        val scope = TestScope()
        CallUserMessagesProvider.start(callMock, scope)
        CallUserMessagesProvider.start(callMock, backgroundScope)
        assertEquals(false, scope.isActive)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDispose() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        CallUserMessagesProvider.dispose()
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testRecordingStartedUserMessage() = runTest {
        with(RecordingMapper) {
            every { callMock.toRecordingMessage() } returns flowOf(RecordingMessage.Started)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun recordingStateInitializedWithStopped_recordingStoppedUserMessageNotReceived() = runTest {
        with(RecordingMapper) {
            every { callMock.toRecordingMessage() } returns flowOf(RecordingMessage.Stopped)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val result = withTimeoutOrNull(100) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testRecordingStoppedUserMessage() = runTest {
        val messageFlow = MutableStateFlow<RecordingMessage>(RecordingMessage.Stopped)
        with(RecordingMapper) {
            every { callMock.toRecordingMessage() } returns messageFlow
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            val actual = CallUserMessagesProvider.userMessage.drop(1).first()
            assert(actual is RecordingMessage.Stopped)
        }
        messageFlow.value = RecordingMessage.Started
    }

    @Test
    fun testRecordingFailedUserMessage() = runTest {
        with(RecordingMapper) {
            every { callMock.toRecordingMessage() } returns flowOf(RecordingMessage.Failed)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is RecordingMessage.Failed)
    }

    @Test
    fun testMutedUserMessage() = runTest {
        with(InputMapper) {
            every { callMock.toMutedMessage() } returns flowOf(MutedMessage(null))
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        withTimeout(100) {
            CallUserMessagesProvider.userMessage.first()
        }
    }

    @Test
    fun testUsbConnectedUserMessage() = runTest {
        with(InputMapper) {
            every { callMock.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.Connected(""))
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is UsbCameraMessage.Connected)
    }

    @Test
    fun usbInitiallyDisconnected_usbDisconnectedUserMessageNotReceived() = runTest {
        with(InputMapper) {
            every { callMock.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.Disconnected)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val result = withTimeoutOrNull(100) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testUsbDisconnectedUserMessage() = runTest {
        val messageFlow = MutableStateFlow<UsbCameraMessage>(UsbCameraMessage.Disconnected)
        with(InputMapper) {
            every { callMock.toUsbCameraMessage() } returns messageFlow
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            val actual = CallUserMessagesProvider.userMessage.drop(1).first()
            assert(actual is UsbCameraMessage.Disconnected)
        }
        messageFlow.value = UsbCameraMessage.Connected("")
    }

    @Test
    fun testUsbNotSupportedUserMessage() = runTest {
        with(InputMapper) {
            every { callMock.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.NotSupported)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is UsbCameraMessage.NotSupported)
    }

    @Test
    fun testGenericAudioOutputFailureMessage() = runTest {
        with(InputMapper) {
            every { callMock.toAudioConnectionFailureMessage() } returns flowOf(
                AudioConnectionFailureMessage.Generic)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is AudioConnectionFailureMessage.Generic)
    }

    @Test
    fun testInSystemCallAudioOutputFailureMessage() = runTest {
        with(InputMapper) {
            every { callMock.toAudioConnectionFailureMessage() } returns flowOf(
                AudioConnectionFailureMessage.InSystemCall)
        }
        CallUserMessagesProvider.start(callMock, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is AudioConnectionFailureMessage.InSystemCall)
    }

    @Test
    fun testSendUserMessage() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        CallUserMessagesProvider.sendUserMessage(CameraRestrictionMessage())
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is CameraRestrictionMessage)
    }

    @Test
    fun testAutomaticRecordingAlertMessage() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connecting)
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callMock.recording } returns MutableStateFlow<Call.Recording>(
            object : Call.Recording {
                override val type: Call.Recording.Type = Call.Recording.Type.Automatic
                override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Stopped)
            }
        )

        CallUserMessagesProvider.start(callMock, backgroundScope)
        CallUserMessagesProvider.alertMessages.first { it.contains(AlertMessage.AutomaticRecordingMessage) }

        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.AutomaticRecordingMessage)
    }

    @Test
    fun testAmIAloneAlertMessage() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        val doOtherHaveStreams = MutableStateFlow<Boolean>(false)
        every { callMock.doOthersHaveStreams() } returns doOtherHaveStreams
        CallUserMessagesProvider.start(callMock, backgroundScope)

        advanceTimeBy(100)
        doOtherHaveStreams.emit(true)
        advanceTimeBy(100)
        doOtherHaveStreams.emit(false)
        CallUserMessagesProvider.alertMessages.first { it.contains(AlertMessage.LeftAloneMessage) }

        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.LeftAloneMessage)
    }

    @Test
    fun testAmIAloneAlertMessageClearedOnCallEnded() = runTest {
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { callMock.state } returns callState
        val callStateUi = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toCallStateUi() } returns callStateUi
        val doOtherHaveStreams = MutableStateFlow<Boolean>(false)
        every { callMock.doOthersHaveStreams() } returns doOtherHaveStreams
        CallUserMessagesProvider.start(callMock, backgroundScope)
        advanceTimeBy(100)
        doOtherHaveStreams.emit(true)
        advanceTimeBy(100)
        doOtherHaveStreams.emit(false)
        CallUserMessagesProvider.alertMessages.first { it.contains(AlertMessage.LeftAloneMessage) }
        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.LeftAloneMessage)

        callState.emit(Call.State.Disconnected.Ended.HungUp())

        CallUserMessagesProvider.alertMessages.first { it.isEmpty() }
        assert(CallUserMessagesProvider.alertMessages.first().isEmpty())
    }

    @Test
    fun testWaitingForOtherParticipantsAlertMessage() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val amIWaitingOthers = MutableStateFlow<Boolean>(true)
        every { callMock.amIWaitingOthers() } returns amIWaitingOthers
        CallUserMessagesProvider.start(callMock, backgroundScope)

        CallUserMessagesProvider.alertMessages.first { it.contains(AlertMessage.WaitingForOtherParticipantsMessage) }

        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.WaitingForOtherParticipantsMessage)
    }

    @Test
    fun testCustomAlertMessage() = runTest {
        val floatingMessage = FloatingMessage("body", FloatingMessage.Button("text", action = { }))
        every { callMock.floatingMessages } returns MutableStateFlow(floatingMessage)
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        CallUserMessagesProvider.start(callMock, backgroundScope)

        CallUserMessagesProvider.alertMessages.first { it.any { it is AlertMessage.CustomMessage } }

        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.CustomMessage)
    }

    @Test
    fun testCustomAlertMessageClearedOnCallEnd() = runTest {
        val callState: MutableStateFlow<Call.State> = MutableStateFlow(Call.State.Connected)
        val floatingMessage = FloatingMessage("body", FloatingMessage.Button("text", action = { }))
        every { callMock.floatingMessages } returns MutableStateFlow(floatingMessage)
        every { callMock.state } returns callState
        CallUserMessagesProvider.start(callMock, backgroundScope)
        CallUserMessagesProvider.alertMessages.first { it.any { it is AlertMessage.CustomMessage } }
        assert(CallUserMessagesProvider.alertMessages.first().first() is AlertMessage.CustomMessage)

        callState.emit(Call.State.Disconnected.Ended.HungUp())
        backgroundScope.cancel()

        CallUserMessagesProvider.alertMessages.first { it.none { it is AlertMessage.CustomMessage } }
        assert(CallUserMessagesProvider.alertMessages.first().isEmpty())
    }

    @Test
    fun testSignDocumentMessage() = runTest {
        mockkObject(SignDocumentsVisibilityObserver.Companion)
        every { SignDocumentsVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        CallUserMessagesProvider.start(callMock, backgroundScope)
        signDocumentsFlow.emit(
            setOf(
                mockk {
                    every { id } returns "signId"
                    every { creationTime } returns 123L
                    every { sender } returns mockk { every { userId } returns "other" }
                }
            )
        )
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is SignatureMessage)
    }

    @Test
    fun testSignDocumentMessageNotShowingIfSignDocumentsIsVisible() = runTest {
        mockkObject(SignDocumentsVisibilityObserver.Companion)
        every { SignDocumentsVisibilityObserver.isDisplayed } returns MutableStateFlow(true)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        signDocumentsFlow.value =
            setOf(
                mockk {
                    every { id } returns "signId"
                    every { creationTime } returns 123L
                    every { sender } returns mockk { every { userId } returns "other" }
                }
            )
        CallUserMessagesProvider.start(callMock, backgroundScope)

        val result = withTimeoutOrNull(1000) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testSignDocumentMessageNotShowingIfCallUIPipIsVisible() = runTest {
        mockkObject(SignDocumentsVisibilityObserver.Companion)
        every { SignDocumentsVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(true)
        signDocumentsFlow.value = setOf(
            mockk {
                every { id } returns "signId"
                every { creationTime } returns 123L
                every { sender } returns mockk { every { userId } returns "other" }
            }
        )
        CallUserMessagesProvider.start(callMock, backgroundScope)

        val result = withTimeoutOrNull(1000) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testDownloadFileMessage() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        mockkObject(FileShareVisibilityObserver.Companion)
        every { FileShareVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        filesFlow.value = setOf(
            mockk {
                every { id } returns "signId"
                every { creationTime } returns 123L
                every { sender } returns mockk {
                    every { combinedDisplayName } returns MutableStateFlow("displayName")
                    every { userId } returns "other"
                }
            }
        )

        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is DownloadFileMessage)
    }

    @Test
    fun testDownloadFileMessageNotShowingIfFileShareIsVisible() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        mockkObject(FileShareVisibilityObserver.Companion)
        every { FileShareVisibilityObserver.isDisplayed } returns MutableStateFlow(true)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        filesFlow.emit(
            setOf(
                mockk {
                    every { id } returns "signId"
                    every { creationTime } returns 123L
                    every { sender } returns mockk {
                        every { combinedDisplayName } returns MutableStateFlow("displayName")
                        every { userId } returns "other"
                    }
                }
            )
        )

        val result = withTimeoutOrNull(1000) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testDownloadFileMessageNotShowingIfCallUIPipIsVisible() = runTest {
        CallUserMessagesProvider.start(callMock, backgroundScope)
        mockkObject(FileShareVisibilityObserver.Companion)
        every { FileShareVisibilityObserver.isDisplayed } returns MutableStateFlow(false)
        mockkObject(CallUiPipVisibilityObserver.Companion)
        every { CallUiPipVisibilityObserver.isDisplayed } returns MutableStateFlow(true)
        filesFlow.value = setOf(
            mockk {
                every { id } returns "signId"
                every { creationTime } returns 123L
                every { sender } returns mockk {
                    every { combinedDisplayName } returns MutableStateFlow("displayName")
                    every { userId } returns "other"
                }
            }
        )

        val result = withTimeoutOrNull(1000) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }
}