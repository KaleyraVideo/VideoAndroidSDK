@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.ui.call.usermessage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StackedSnackbarHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    val userMessageFlow = MutableStateFlow<ImmutableList<UserMessage>>(ImmutableList())
    val mutableUiState = MutableStateFlow<StackedSnackbarUiState>(StackedSnackbarUiState())
    val viewModelMock = mockk<UserMessagesViewModel> {
        every { userMessage } returns userMessageFlow
        every { uiState } returns mutableUiState
    }

    @Before
    fun setup() {
        composeTestRule.setContent {
            StackedUserMessageComponent(viewModel = viewModelMock) {}
        }
    }

    @Test
    fun testMultipleUserMessagePosted() = runTest {
        userMessageFlow.emit(ImmutableList(listOf(RecordingMessage.Started, AudioConnectionFailureMessage.Generic)))

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val recordingStarted = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val audioConnectionFailure = composeTestRule.activity.getString(R.string.kaleyra_generic_audio_routing_error)
        composeTestRule.onNodeWithText(recordingStarted).assertIsDisplayed()
        composeTestRule.onNodeWithText(audioConnectionFailure).assertIsDisplayed()
    }

    @Test
    fun testRemoveMessage() = runTest {
        userMessageFlow.emit(ImmutableList(listOf(RecordingMessage.Started,AudioConnectionFailureMessage.Generic)))
        advanceUntilIdle()
        composeTestRule.waitForIdle()
        userMessageFlow.emit(ImmutableList(listOf(AudioConnectionFailureMessage.Generic)))

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val recordingStarted = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(recordingStarted).assertIsNotDisplayed()
    }

    @Test
    fun testMultipleAlertMessagePosted() = runTest {
        mutableUiState.emit(StackedSnackbarUiState(alertMessages = ImmutableList(listOf(AlertMessage.AutomaticRecordingMessage, AlertMessage.WaitingForOtherParticipantsMessage))))

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val automaticRecording = composeTestRule.activity.getString(R.string.kaleyra_automatic_recording_disclaimer)
        val waitingForOtherParticipants = composeTestRule.activity.getString(R.string.kaleyra_strings_info_call_waiting_for_other_participants)
        composeTestRule.onNodeWithText(automaticRecording).assertIsDisplayed()
        composeTestRule.onNodeWithText(waitingForOtherParticipants).assertIsDisplayed()
    }
}