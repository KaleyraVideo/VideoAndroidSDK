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

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.isFileSharingSupported
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.toCallActions
import com.kaleyra.video_sdk.call.mapper.InputMapper
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasAudio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.hasVirtualBackground
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CallActionsMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(VirtualBackgroundMapper)
        mockkObject(InputMapper)
        mockkObject(ParticipantMapper)
        every { callMock.hasVirtualBackground() } returns flowOf(false)
        every { callMock.hasAudio() } returns flowOf(true)
        every { callMock.isAudioOnly() } returns flowOf(false)
        every { callMock.isGroupCall(any()) } returns flowOf(false)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun fileShareActionExists_isFileSharingSupported_true() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.FileShare))
        val result = callMock.isFileSharingSupported()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun fileShareActionDoesNotExists_isFileSharingSupported_true() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ScreenShare))
        val result = callMock.isFileSharingSupported()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(emptySet())
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf<CallActionUI>()
        assertEquals(expected, actual)
    }

    @Test
    fun allCallActions_toCallActions_mappedCallActions() = runTest {
        every { callMock.actions } returns MutableStateFlow(
            setOf(
                CallUI.Action.ToggleMicrophone,
                CallUI.Action.ToggleCamera,
                CallUI.Action.SwitchCamera,
                CallUI.Action.HangUp,
                CallUI.Action.OpenChat.Full,
                CallUI.Action.OpenWhiteboard.Full,
                CallUI.Action.Audio,
                CallUI.Action.FileShare,
                CallUI.Action.ScreenShare
            )
        )
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(
            MicAction(),
            CameraAction(),
            FlipCameraAction(),
            HangUpAction(),
            ChatAction(),
            WhiteboardAction(),
            AudioAction(),
            FileShareAction(),
            ScreenShareAction()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun hasVirtualBackgroundTrue_toCallActions_actionsListHasVirtualBackground() = runTest {
        every { callMock.actions } returns MutableStateFlow(emptySet())
        every { callMock.hasVirtualBackground() } returns flowOf(true)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(VirtualBackgroundAction())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleCameraActionAndCallHasVideo_toCallActions_actionsListHasCamera() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { callMock.isAudioOnly() } returns flowOf(false)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(CameraAction())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleCameraActionAndCallHasNoVideo_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { callMock.isAudioOnly() } returns flowOf(true)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(), actual)
    }

    @Test
    fun chatActionAndItIsNotGroupCall_toCallActions_actionsListHasChatAction() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenChat.Full))
        every { callMock.isGroupCall(any()) } returns flowOf(false)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        assertEquals(listOf(ChatAction()), actual)
    }

    @Test
    fun chatActionAndItIsGroupCall_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenChat.Full))
        every { callMock.isGroupCall(any()) } returns flowOf(true)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(), actual)
    }

    @Test
    fun switchCameraActionAndCallHasVideo_toCallActions_actionsListHasSwitchCamera() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { callMock.isAudioOnly() } returns flowOf(false)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(FlipCameraAction())
        assertEquals(expected, actual)
    }

    @Test
    fun switchCameraActionAndCallHasNoVideo_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { callMock.isAudioOnly() } returns flowOf(true)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(), actual)
    }

    @Test
    fun toggleMicActionAndCallHasAudio_toCallActions_actionsListHasMicrophone() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { callMock.hasAudio() } returns flowOf(true)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(MicAction())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleMicActionAndCallHasNoAudio_toCallActions_emptyList() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { callMock.hasAudio() } returns flowOf(false)
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(), actual)
    }

    @Test
    fun moreThan3Actions_toCallActions_hangUpIsIn4thPosition() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone, CallUI.Action.SwitchCamera, CallUI.Action.ToggleCamera, CallUI.Action.HangUp, CallUI.Action.ScreenShare))
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(
            MicAction(),
            CameraAction(),
            FlipCameraAction(),
            HangUpAction(),
            ScreenShareAction()
        )
        assertEquals(expected, actual)
    }

    @Test
    fun lessOf4Actions_toCallActions_hangUpIsLastPosition() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone, CallUI.Action.SwitchCamera, CallUI.Action.HangUp))
        val result = callMock.toCallActions(flowOf("companyId"))
        val actual = result.first()
        val expected = listOf(
            MicAction(),
            FlipCameraAction(),
            HangUpAction()
        )
        assertEquals(expected, actual)
    }
}