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

package com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.screen.view.ModalSheetComponent
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScreenPanelContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<CallActionUI>()))

    private var callAction: CallActionUI? = null

    @Before
    fun setUp() {
        items = ImmutableList()
        callAction = null
    }

    private val callActionsUiState = MutableStateFlow(CallActionsUiState())

    private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    @Test
    fun userClicksChat_showChatInvoked() {
        mockkObject(ActivityExtensions)
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ChatAction())),
                onModalSheetComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.showChat(any()) }
        verify(exactly = 1) { composeTestRule.activity.unlockDevice(any()) }
        unmockkObject(ActivityExtensions)
    }

    @Test
    fun userClicksFlipCamera_switchCameraInvoked() {
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FlipCameraAction())),
                onModalSheetComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.switchCamera() }
    }

    @Test
    fun userClicksAudio_onModalSheetComponentRequestAudio() {
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(AudioAction())),
                onModalSheetComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Audio, component)
    }

    @Test
    fun userClicksFileShare_onModalSheetComponentRequestFileShare() {
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(FileShareAction())),
                onModalSheetComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.FileShare, component)
    }

    @Test
    fun userClicksWhiteboard_onModalSheetComponentRequestWhiteboard() {
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(WhiteboardAction())),
                onModalSheetComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.Whiteboard, component)
    }

    @Test
    fun userClicksVirtualBackground_onModalSheetComponentRequestVirtualBackground() {
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(VirtualBackgroundAction())),
                onModalSheetComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.VirtualBackground, component)
    }

    @Test
    fun userClicksScreenShareWhenEnabled_tryStopScreenShareInvoked() {
        every { callActionsViewModel.tryStopScreenShare() } returns true
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ScreenShareAction())),
                onModalSheetComponentRequest = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callActionsViewModel.tryStopScreenShare() }
    }

    @Test
    fun userClicksScreenShareWhenNotEnabled_onModalSheetComponentRequestScreenShare() {
        every { callActionsViewModel.tryStopScreenShare() } returns false
        var component: ModalSheetComponent? = null
        composeTestRule.setContent {
            SheetPanelContent(
                viewModel = callActionsViewModel,
                callActions = ImmutableList(listOf(ScreenShareAction())),
                onModalSheetComponentRequest = { component = it }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_screen_share)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModalSheetComponent.ScreenShare, component)
    }

    @Test
    fun flipCameraAction_flipCameraItemIsDisplayed() {
        items = ImmutableList(listOf(FlipCameraAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun audioAction_audioItemIsDisplayed() {
        items = ImmutableList(listOf(AudioAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun chatAction_chatItemIsDisplayed() {
        items = ImmutableList(listOf(ChatAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fileShareAction_fileShareItemIsDisplayed() {
        items = ImmutableList(listOf(FileShareAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun whiteboardAction_whiteboardItemIsDisplayed() {
        items = ImmutableList(listOf(WhiteboardAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun virtualBackgroundAction_virtualBackgroundItemIsDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundAction()))
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        val action = ChatAction()
        items = ImmutableList(
            listOf(
                FlipCameraAction(),
                AudioAction(),
                action,
                FileShareAction(),
                WhiteboardAction(),
                VirtualBackgroundAction()
            )
        )
        composeTestRule.setContent {
            SheetPanelContent(
                callActions = items,
                onItemClick = { callAction = it }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_chat)
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(action, callAction)
    }
}