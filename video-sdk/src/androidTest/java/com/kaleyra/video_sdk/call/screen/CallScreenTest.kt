package com.kaleyra.video_sdk.call.screen

import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.pip.view.DefaultPipSize
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheetTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.SidePanelTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.StreamMenuContentTestTag
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.findBackButton
import com.kaleyra.video_sdk.pressBack
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

internal class CallScreenTest: CallScreenBaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun callBottomSheetExpanded_userPerformsBack_sheetIsCollapsed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = allActions.toImmutableList()
        )
        val sheetState = CallSheetState(CallSheetValue.Expanded)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun streamMenuDisplayed_userPerformsBack_streamMenuIsDismissed() {
        val streams = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri()))
        )

        streamUiState.value = StreamUiState(
            streamItems = listOf(streams).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertDoesNotExist()
    }

    @Test
    fun callBottomSheetCollapsed_userPerformsBack_activityIsFinishing() {
        val sheetState = CallSheetState(CallSheetValue.Collapsed)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(true, composeTestRule.activity.isFinishing)
    }

    @Test
    fun callStateEnded_userPerformsBack_onCallEndedBackInvoked() {
        var isInvoked = false
        composeTestRule.setUpCallScreen(
            uiState = MainUiState(isCallEnded = true),
            onCallEndedBack = { isInvoked = true }
        )

        composeTestRule.pressBack()
        composeTestRule.waitForIdle()

        assertEquals(true, isInvoked)
    }

    @Test
    fun userClicksFileShareActionOnSmallScreen_fileShareModalSheetIsDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksFileShareActionOnSmallScreen_fileShareModalSheetIsDisplayed_feedbackDisplayed_fileShareModalSheetHidden() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()

        feedbackUiState.value = FeedbackUiState.Display()

        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksFileShareActionOnSmallScreen_fileShareModalSheetIsDisplayed_kickedDisplayed_fileShareModalSheetHidden() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()

        kickedUiState.value = KickedMessageUiState.Display()

        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksAudioAction_audioComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(AudioAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val audioText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_audio)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(audioText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_audio_route_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksScreenShareAction_screenShareComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction.UserChoice()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(screenShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksWhiteboardActionOnSmallScreen_whiteboardModalSheetIsDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(whiteboardText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksVirtualBackgroundAction_virtualBackgroundComponentDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(VirtualBackgroundAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val virtualBgText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_virtual_background)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(virtualBgText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksParticipantsButtonOnSmallScreen_participantsModalSheetIsDisplayed() {
        composeTestRule.setUpCallScreen()

        val participantsText = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule
            .onNodeWithContentDescription(participantsText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksMoreParticipantsStreamOnSmallScreen_participantsModalSheetDisplayed() {
        val streamItem = StreamItem.MoreStreams(
            userInfos = listOf(
                UserInfo("1", "user1", ImmutableUri()),
                UserInfo("2", "user2", ImmutableUri()),
            ).toImmutableList()
        )
        streamUiState.value = StreamUiState(streamItems = listOf(streamItem).toImmutableList())

        composeTestRule.setUpCallScreen()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule
            .onNodeWithText(otherText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
    }

    @Test
    fun pipModeEnabled_pipCallScreenIsDisplayed() {
        composeTestRule.setUpCallScreen(isPipMode = true)
        composeTestRule.onNodeWithTag(PipScreenTestTag).assertIsDisplayed()
    }

    @Test
    fun selectedStream_streamMenuIsDisplayed() {
        val streamItem = StreamItem.Stream(
            id = "streamId",
            stream = StreamUi(id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri())),
        )
        streamUiState.value = StreamUiState(
            streamItems = listOf(streamItem).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        composeTestRule
            .onNodeWithText("username", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(StreamMenuContentTestTag).assertIsDisplayed()
    }

    @Test
    fun collapsedCallSheet_userClicksMoreButton_sheetIsExpanded() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val sheetState = CallSheetState()
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_more_actions)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun expandedCallSheet_userClicksMoreButton_sheetIsCollapsed() {
        callActionsUiState.update {
            it.copy(actionList = allActions.toImmutableList())
        }
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setUpCallScreen(callSheetState = sheetState)

        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_description_hide_actions)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        composeTestRule.waitForIdle()

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun shouldShowFileShareComponentTrueOnSmallScreen_fileShareModalSheetDisplayed() {
        val shouldShowFileShareComponent = mutableStateOf(false)
        composeTestRule.setUpCallScreen(
            shouldShowFileShareComponent = shouldShowFileShareComponent
        )

        val fileShareTitle = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule.onNodeWithText(fileShareTitle).assertDoesNotExist()

        shouldShowFileShareComponent.value = true

        composeTestRule.onNodeWithText(fileShareTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
    }

    @Test
    fun fileShareModalSheetComponentIsDisplayed_onFileShareVisibilityInvoked() {
        var isFileShareDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(FileShareAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(
            onFileShareVisibility = { isFileShareDisplayed = it }
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_files)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isFileShareDisplayed)
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
    }

    @Test
    fun whiteboardModalsSheetDisplayed_onWhiteboardVisibilityInvoked() {
        var isWhiteboardDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(
            onWhiteboardVisibility = { isWhiteboardDisplayed = it }
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isWhiteboardDisplayed)
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        var isBackPressed = false
        composeTestRule.setUpCallScreen(
            onBackPressed = { isBackPressed = true }
        )

        composeTestRule.findBackButton().performClick()
        assertEquals(true, isBackPressed)
    }

    @Test
    fun showWhiteboardRequestReceivedOnSmallScreen_whiteboardModalSheetDisplayed() {
        val whiteboardRequest = mutableStateOf(WhiteboardRequest.Show("username"))
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        val whiteboardRequest = mutableStateOf(WhiteboardRequest.Show("username"))
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )
        composeTestRule.waitForIdle()
        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardRequestMessage.WhiteboardShowRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

    @Test
    fun hideWhiteboardRequestReceivedOnSmallScreen_whiteboardNotDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        val whiteboardRequest = mutableStateOf<WhiteboardRequest?>(null)
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertIsDisplayed()

        whiteboardRequest.value = WhiteboardRequest.Hide("username")

        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardUserMessageDisplayed() = mockkObject(CallUserMessagesProvider) {
        every { CallUserMessagesProvider.sendUserMessage(any()) } returns Unit

        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        val whiteboardRequest = mutableStateOf<WhiteboardRequest?>(null)
        composeTestRule.setUpCallScreen(
            whiteboardRequest = whiteboardRequest
        )

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()

        whiteboardRequest.value = WhiteboardRequest.Hide("username")

        composeTestRule.waitForIdle()

        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg<WhiteboardRequestMessage.WhiteboardHideRequestMessage> {
                assertEquals("username", it.username)
            })
        }
    }

    @Test
    fun testUserClicksDeviceScreenShare_onAskInputPermissionsInvoked() {
        var arePermissionAsked = false
        composeTestRule.setUpCallScreen(
            onAskInputPermissions = { arePermissionAsked = true }
        )
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(ScreenShareAction.UserChoice()).toImmutableList()
        )

        val screenShareText = composeTestRule.activity.getString(R.string.kaleyra_strings_action_start_screen_share)
        composeTestRule.onNodeWithContentDescription(screenShareText, useUnmergedTree = true).performClick()

        composeTestRule.waitForIdle()

        val deviceText = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(deviceText).performClick()

        TestCase.assertEquals(true, arePermissionAsked)
    }

    @Test
    fun testOnPipAspectRatioInvoked() {
        var aspectRatio: Rational? = null
        composeTestRule.setUpCallScreen(
            onPipAspectRatio = { aspectRatio = it },
            isPipMode = true
        )
        composeTestRule.waitForIdle()

        assertEquals(Rational(DefaultPipSize.width, DefaultPipSize.height), aspectRatio)
    }
}
