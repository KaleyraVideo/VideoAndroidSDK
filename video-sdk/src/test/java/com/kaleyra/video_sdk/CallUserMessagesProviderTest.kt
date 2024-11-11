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

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.mapper.InputMapper
import com.kaleyra.video_sdk.call.mapper.RecordingMapper
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUserMessagesProviderTest {

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(InputMapper)
        mockkObject(RecordingMapper)
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
}