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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
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
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.FileShare))
        val result = callMock.isFileSharingSupported()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun fileShareActionDoesNotExists_isFileSharingSupported_true() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.ScreenShare.UserChoice))
        val result = callMock.isFileSharingSupported()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { callMock.buttons } returns MutableStateFlow(emptySet())
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf<CallActionUI>()
        assertEquals(expected, actual)
    }

    @Test
    fun allCallActions_toCallActions_mappedCallActions() = runTest {
        every { callMock.hasVirtualBackground() } returns flowOf(true)
        every { callMock.buttons } returns MutableStateFlow(
            setOf(
                CallUI.Button.Microphone,
                CallUI.Button.Camera,
                CallUI.Button.FlipCamera,
                CallUI.Button.HangUp,
                CallUI.Button.CameraEffects,
                CallUI.Button.Chat,
                CallUI.Button.Whiteboard,
                CallUI.Button.AudioOutput,
                CallUI.Button.FileShare,
                CallUI.Button.ScreenShare.UserChoice,
                CallUI.Button.ScreenShare.App,
                CallUI.Button.ScreenShare.WholeDevice
            )
        )
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf(
            MicAction(),
            CameraAction(),
            FlipCameraAction(),
            HangUpAction(),
            VirtualBackgroundAction(),
            ChatAction(),
            WhiteboardAction(),
            AudioAction(),
            FileShareAction(),
            ScreenShareAction.UserChoice(),
            ScreenShareAction.App(),
            ScreenShareAction.WholeDevice(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun customAction_toCallActions_actionListHasCustomAction() = runTest {
        val customAction = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(
                icon = 20,
                text = "customText",
                action = { },
                badgeValue = 3,
                isEnabled = false,
                accessibilityLabel = "accessibilityText",
                appearance = CallUI.Button.Custom.Configuration.Appearance(14, 17)
            )
        )
        every { callMock.buttons } returns MutableStateFlow(setOf(customAction))
        val result = callMock.toCallActions().first()
        val expected = listOf(
            CustomAction(
                id = customAction.id,
                icon = customAction.config.icon,
                buttonTexts = CustomCallAction.ButtonTexts(customAction.config.text, customAction.config.accessibilityLabel),
                onClick = customAction.config.action,
                notificationCount = customAction.config.badgeValue,
                buttonColors = customAction.config.appearance?.let {
                    CustomCallAction.ButtonsColors(it.background, it.tint, Color(it.background).copy(alpha = 0.38f).toArgb(), Color(it.tint).copy(alpha = 0.38f).toArgb(),)
                },
                isEnabled = customAction.config.isEnabled
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun cameraEffectsAndHasVirtualBackgroundTrue_toCallActions_actionsListHasVirtualBackground() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.CameraEffects))
        every { callMock.hasVirtualBackground() } returns flowOf(true)
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf(VirtualBackgroundAction())
        assertEquals(expected, actual)
    }

    @Test
    fun cameraEffectsAndHasVirtualBackgroundFalse_toCallActions_hasDisabledVirtualBackgroundAction() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.CameraEffects))
        every { callMock.hasVirtualBackground() } returns flowOf(false)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(VirtualBackgroundAction(isEnabled = false)), actual)
    }

    @Test
    fun toggleCameraActionAndCallHasVideo_toCallActions_actionsListHasCamera() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Camera))
        every { callMock.isAudioOnly() } returns flowOf(false)
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf(CameraAction())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleCameraActionAndCallHasNoVideo_toCallActions_hasDisabledCameraAction() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Camera))
        every { callMock.isAudioOnly() } returns flowOf(true)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(CameraAction(isEnabled = false)), actual)
    }

    @Test
    fun chatActionAndItIsNotGroupCall_toCallActions_actionsListHasChatAction() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Chat))
        every { callMock.isGroupCall(any()) } returns flowOf(false)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf(ChatAction()), actual)
    }

    @Test
    fun chatActionAndItIsGroupCall_toCallActions_emptyList() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Chat))
        every { callMock.isGroupCall(any()) } returns flowOf(true)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(ChatAction()), actual)
    }

    @Test
    fun switchCameraActionAndCallHasVideo_toCallActions_actionsListHasSwitchCamera() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.FlipCamera))
        every { callMock.isAudioOnly() } returns flowOf(false)
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf(FlipCameraAction())
        assertEquals(expected, actual)
    }

    @Test
    fun switchCameraActionAndCallHasNoVideo_toCallActions_hasDisabledFlipCameraAction() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.FlipCamera))
        every { callMock.isAudioOnly() } returns flowOf(true)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(FlipCameraAction(isEnabled = false)), actual)
    }

    @Test
    fun toggleMicActionAndCallHasAudio_toCallActions_actionsListHasMicrophone() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Microphone))
        every { callMock.hasAudio() } returns flowOf(true)
        val result = callMock.toCallActions()
        val actual = result.first()
        val expected = listOf(MicAction())
        assertEquals(expected, actual)
    }

    @Test
    fun toggleMicActionAndCallHasNoAudio_toCallActions_hasDisabledMicAction() = runTest {
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.Microphone))
        every { callMock.hasAudio() } returns flowOf(false)
        val result = callMock.toCallActions()
        val actual = result.first()
        assertEquals(listOf<CallActionUI>(MicAction(isEnabled = false)), actual)
    }
}