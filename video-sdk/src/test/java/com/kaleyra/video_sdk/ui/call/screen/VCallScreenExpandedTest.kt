package com.kaleyra.video_sdk.ui.call.screen

import android.net.Uri
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAncestors
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.brandlogo.model.BrandLogoState
import com.kaleyra.video_sdk.call.brandlogo.model.Logo
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.screen.LargeScreenMaxActions
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheetTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.PanelTestTag
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import io.mockk.every
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Test for the VCallScreen using a tablet (both in portrait and landscape)
@Config(qualifiers = "w960dp-h480dp")
@RunWith(RobolectricTestRunner::class)
internal class VCallScreenExpandedTest: VCallScreenBaseTest() {

    @Test
    fun testRinging_sheetDragActionsAreDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen()

        composeTestRule.onNodeWithTag(InputMessageDragHandleTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSheetActions_moreShowPanel() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertIsDisplayed()
    }

    @Test
    fun testSheetActions_moreHidePanel() {
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(moreText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertIsDisplayed()

        val hideText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(hideText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText(PanelTestTag).assertDoesNotExist()
    }

    @Test
    fun testNoMoreRingingState_sheetActionsIsCollapsed() {
        val sheetState = CallSheetState(CallSheetValue.Expanded)
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen(
            sheetState = sheetState,
        )

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)

        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList(),
            isRinging = false
        )
        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun testSheetPanelActions_flipCamera() {
        val actions = allActions.filterNot { it is FlipCameraAction }.take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FlipCameraAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val flipText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_flip_camera)
        composeTestRule
            .onAllNodesWithText(flipText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.switchCamera() }
    }

    @Test
    fun testSheetPanelActions_audioComponentIsDisplayed() {
        val actions = allActions.filterNot { it is AudioAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + AudioAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        composeTestRule
            .onAllNodesWithText(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Audio, component)
    }

    @Test
    fun testSheetPanelActions_chatComponentIsDisplayed() {
        val actions = (allActions - ChatAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onSidePanelComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule
            .onAllNodesWithText(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun testSheetPanelActions_screenShareToggleOff() {
        val actions = (allActions - ScreenShareAction.UserChoice()).take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice(isToggled = true)).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_stop_screen_share)
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { callViewModel.tryStopScreenShare() }
    }

    @Test
    fun testSheetPanelActions_screenShareToggleOn() {
        val actions = (allActions - ScreenShareAction.UserChoice()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ScreenShareAction.UserChoice()).toImmutableList(),
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule
            .onAllNodesWithText(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.ScreenShare, component)
    }

    @Test
    fun testSheetPanelActions_fileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onSidePanelComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + FileShareAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule
            .onAllNodesWithText(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetPanelActions_whiteboardComponentIsDisplayed() {
        val actions = (allActions - WhiteboardAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onSidePanelComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onAllNodesWithText(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun testSheetPanelActions_virtualBackgroundComponentIsDisplayed() {
        val actions = allActions.filterNot { it is VirtualBackgroundAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onModalSheetComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + VirtualBackgroundAction()).toImmutableList()
        )

        val moreText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule
            .onNodeWithContentDescription(moreText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        val virtualBgText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        composeTestRule
            .onAllNodesWithText(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.VirtualBackground, component)
    }


    @Test
    fun testSheetDragActions_sidePanelChatComponentIsDisplayed() {
        val actions = allActions.filterNot { it is ChatAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList(),
            isRinging = true
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onSidePanelComponentRequest = { component = it }
        )

        val chatText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(chatText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun testSheetActions_sidePanelChatComponentIsDisplayed() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            onSidePanelComponentRequest = { component = it}
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ChatAction()).toImmutableList()
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule
            .onNodeWithContentDescription(text, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        assertEquals(ModularComponent.Chat, component)
    }

    @Test
    fun testSheetActions_onSidePanelComponentChangeToFileShare() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onSidePanelComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetActions_onSidePanelComponentChangeToWhiteboard() {
        var component: ModularComponent? = null
        composeTestRule.setUpVCallScreen(
            modalSheetComponent = null,
            onSidePanelComponentRequest = { component = it }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun testSheetDragActions_sidePanelFileShareComponentIsDisplayed() {
        val actions = (allActions - FileShareAction()).take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            isRinging = true,
            actionList = (actions + FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onSidePanelComponentRequest = { component = it }
        )

        val fileShareText =
            composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.FileShare, component)
    }

    @Test
    fun testSheetDragActions_sidePanelWhiteboardComponentIsDisplayed() {
        val actions = allActions.filterNot { it is WhiteboardAction }.take(LargeScreenMaxActions)
        var component: ModularComponent? = null
        callActionsUiState.value = CallActionsUiState(
            isRinging = true,
            actionList = (actions + WhiteboardAction()).toImmutableList()
        )
        composeTestRule.setUpVCallScreen(
            sheetState = CallSheetState(CallSheetValue.Expanded),
            onSidePanelComponentRequest = { component = it }
        )

        val whiteboardText =
            composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithText(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        assertEquals(ModularComponent.Whiteboard, component)
    }

    @Test
    fun largeScreen_dragHandleIsNotDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )
        composeTestRule.setUpVCallScreen()

        composeTestRule.onNodeWithTag(InputMessageDragHandleTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userMessageIsNotDisplayedOnSidePanelFileShareComponent() {
        every { userMessagesViewModel.userMessage } returns flowOf(
            ImmutableList(listOf(
                RecordingMessage.Started))
        )
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.FileShare
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .onAncestors()[2]
            .onChildAt(2)
            .onChildren()
            .assertAll(hasText(fileShareText).not())
    }

    @Test
    fun userMessageIsNotDisplayedOnSidePanelWhiteboardComponent() {
        every { userMessagesViewModel.userMessage } returns flowOf(
            ImmutableList(listOf(
                RecordingMessage.Started))
        )
        composeTestRule.setUpVCallScreen(
            sidePanelComponent = ModularComponent.Whiteboard
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule
            .onNodeWithText(text, useUnmergedTree = true)
            .onAncestors()[2]
            .onChildAt(0)
            .onChildren()
            .assertAll(hasText(whiteboardText).not())
    }

    @Test
    fun userClicksOnSheetPanelItem_sheetPanelIsDismissed() {
        val actions = allActions.filterNot { it is ChatAction }.take(LargeScreenMaxActions)
        composeTestRule.setUpVCallScreen()
        callActionsUiState.value = CallActionsUiState(
            actionList = (actions + ChatAction()).toImmutableList()
        )

        val moreText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(moreText, useUnmergedTree = true).performClick()

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_chat)
        composeTestRule.onAllNodesWithText(audioText, useUnmergedTree = true)[0].performClick()

        composeTestRule.onNodeWithTag(PanelTestTag).assertDoesNotExist()
    }

    @Test
    fun largeDevice_companyLogoSet_callDisconnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Companion, logo = companyLogo))
        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callEndedNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnected.Ended, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callDisconnectingNeverConnected_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connecting, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Disconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callConnected_brandLogoDisplayed() = runTest {
        val logo = Logo(light = Uri.parse("https://www.example.com/light.png"), dark = Uri.parse("https://www.example.com/dark.png"))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = logo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoSet_callReconnecting_brandLogoDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Connected, logo = companyLogo))
        brandLogoUiState.emit(BrandLogoState(callStateUi = CallStateUi.Reconnecting, logo = companyLogo))

        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun largeDevice_companyLogoNotSet_brandLogoNotDisplayed() = runTest {
        brandLogoUiState.emit(BrandLogoState())
        composeTestRule.setUpVCallScreen()
        composeTestRule.waitForIdle()

        val companyLogo = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        composeTestRule.onNodeWithContentDescription(companyLogo).assertDoesNotExist()
    }
}