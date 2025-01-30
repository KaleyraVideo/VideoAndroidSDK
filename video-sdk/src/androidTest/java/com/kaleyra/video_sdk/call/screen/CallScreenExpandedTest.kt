package com.kaleyra.video_sdk.call.screen

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
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.feedback.viewmodel.FeedbackViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.kicked.viewmodel.KickedMessageViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screen.view.CallScreenModalSheetTag
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.SidePanelTag
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass

internal class CallScreenExpandedTest: CallScreenBaseTest() {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun userClicksFileShareAction_fileShareSidePanelIsDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun userClicksFileShareAction_fileShareSidePanelIsDisplayed_feedbackDisplayed_fileShareSidePanelNotDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()

        feedbackUiState.value = FeedbackUiState.Display()

        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun userClicksFileShareAction_fileShareSidePanelIsDisplayed_kickedDisplayed_fileShareSidePanelNotDisplayed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        composeTestRule
            .onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()

        kickedUiState.value = KickedMessageUiState.Display()

        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun userClicksTwiceFileShareAction_fileShareSidePanelIsClosed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(FileShareAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val fileShareText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        val fileShareButton = composeTestRule.onAllNodesWithContentDescription(fileShareText, useUnmergedTree = true)[0]
        fileShareButton.performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()

        fileShareButton.performClick()
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksWhiteboardAction_whiteboardSidePanelIsDisplayed() {
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
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun userClicksTwiceWhiteboardAction_whiteboardSidePanelIsClosed() {
        callActionsUiState.value = CallActionsUiState(
            actionList = listOf(WhiteboardAction()).toImmutableList()
        )
        composeTestRule.setUpCallScreen()

        val whiteboardText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        // Check the button contained in the draggable part of the bottom sheet is displayed
        // The first of the list is the button contained in the fixed part of the bottom sheet, but not rendered by the internal adaptive layout.
        val whiteboardButton = composeTestRule.onAllNodesWithContentDescription(whiteboardText, useUnmergedTree = true)[0]
        whiteboardButton.performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()

        whiteboardButton.performClick()
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksParticipantsButton_participantsSidePanelIsDisplayed() {
        composeTestRule.setUpCallScreen()

        val participantsText = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule
            .onNodeWithContentDescription(participantsText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallScreenModalSheetTag).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun userClicksTwiceParticipantsButton_participantsSidePanelIsClosed() {
        composeTestRule.setUpCallScreen()

        val participantsText = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        val participantsButton = composeTestRule.onNodeWithContentDescription(participantsText, useUnmergedTree = true)
        participantsButton.performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()


        participantsButton.performClick()
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun userClicksMoreParticipantsStream_participantsSidePanelDisplayed() {
        val streamItem = StreamItem.HiddenStreams(
            users = listOf(
                HiddenStreamUserPreview("1", "user1", null),
                HiddenStreamUserPreview("2", "user2", null)
            )
        )
        streamUiState.value = StreamUiState(streamItems = listOf(streamItem).toImmutableList())

        composeTestRule.setUpCallScreen()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule
            .onNodeWithText(otherText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun shouldShowFileShareComponentTrue_fileShareSidePanelDisplayed() {
        val shouldShowFileShareComponent = mutableStateOf(false)
        composeTestRule.setUpCallScreen(shouldShowFileShareComponent = shouldShowFileShareComponent)

        val fileShareTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(fileShareTitle).assertDoesNotExist()

        shouldShowFileShareComponent.value = true

        composeTestRule.onNodeWithText(fileShareTitle, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun fileShareSidePanelComponentIsDisplayed_onFileShareVisibilityInvoked() {
        var isFileShareDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(FileShareAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(onFileShareVisibility = { isFileShareDisplayed = it })

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_file_share)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isFileShareDisplayed)
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun whiteboardSidePanelDisplayed_onWhiteboardVisibilityInvoked() {
        var isWhiteboardDisplayed = false
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        composeTestRule.setUpCallScreen(onWhiteboardVisibility = { isWhiteboardDisplayed = it })

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(true, isWhiteboardDisplayed)
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun showWhiteboardRequestReceived_whiteboardSidePanelDisplayed() {
        val whiteboardRequest = mutableStateOf(WhiteboardRequest.Show("username"))
        composeTestRule.setUpCallScreen(whiteboardRequest = whiteboardRequest)

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun hideWhiteboardRequestReceived_whiteboardNotDisplayed() {
        callActionsUiState.update {
            it.copy(actionList = listOf(WhiteboardAction()).toImmutableList())
        }
        val whiteboardRequest = mutableStateOf<WhiteboardRequest?>(null)
        composeTestRule.setUpCallScreen(whiteboardRequest = whiteboardRequest)

        val buttonText = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_whiteboard)
        composeTestRule
            .onNodeWithContentDescription(buttonText, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        val text = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertIsDisplayed()

        whiteboardRequest.value = WhiteboardRequest.Hide("username")

        composeTestRule.onNodeWithText(text, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SidePanelTag, useUnmergedTree = true).assertDoesNotExist()
    }
}