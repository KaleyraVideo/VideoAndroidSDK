package com.kaleyra.video_sdk.call.usermessage

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.snackbarm3.model.StackedSnackbarHostState
import com.kaleyra.video_sdk.common.snackbarm3.view.StackedSnackbarHost
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StackedSnackbarHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    lateinit var stackedSnackbarHostState: StackedSnackbarHostState

    @Test
    fun testMultipleUserMessagePosted() = runTest {
        composeTestRule.setContent {
            stackedSnackbarHostState = StackedSnackbarHostState(LocalAccessibilityManager.current!!, this)
            LocalAccessibilityManager.current
            StackedSnackbarHost(stackedSnackbarHostState, onActionClick = {})
        }

        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostState.addUserMessage(AudioConnectionFailureMessage.Generic, false)

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val recordingStarted = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        val audioConnectionFailure = composeTestRule.activity.getString(R.string.kaleyra_generic_audio_routing_error)
        composeTestRule.onNodeWithText(recordingStarted).assertIsDisplayed()
        composeTestRule.onNodeWithText(audioConnectionFailure).assertIsDisplayed()
    }

    @Test
    fun testRemoveMessage() = runTest {
        composeTestRule.setContent {
            stackedSnackbarHostState = StackedSnackbarHostState(LocalAccessibilityManager.current!!, this)
            LocalAccessibilityManager.current
            StackedSnackbarHost(stackedSnackbarHostState, onActionClick = {})
        }

        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostState.addUserMessage(AudioConnectionFailureMessage.Generic, false)
        advanceUntilIdle()
        composeTestRule.waitForIdle()
        stackedSnackbarHostState.removeUserMessage(RecordingMessage.Started)

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val recordingStarted = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(recordingStarted).assertIsNotDisplayed()
    }

    @Test
    fun testMultipleAlertMessagePosted() = runTest {
        composeTestRule.setContent {
            stackedSnackbarHostState = StackedSnackbarHostState(LocalAccessibilityManager.current!!, this)
            LocalAccessibilityManager.current
            StackedSnackbarHost(stackedSnackbarHostState, onActionClick = {})
        }

        stackedSnackbarHostState.addAlertMessages(listOf(AlertMessage.AutomaticRecordingMessage, AlertMessage.WaitingForOtherParticipantsMessage))

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        val automaticRecording = composeTestRule.activity.getString(R.string.kaleyra_automatic_recording_disclaimer)
        val waitingForOtherParticipants = composeTestRule.activity.getString(R.string.kaleyra_waiting_for_other_participants)
        composeTestRule.onNodeWithText(automaticRecording).assertIsDisplayed()
        composeTestRule.onNodeWithText(waitingForOtherParticipants).assertIsDisplayed()
    }

    @Test
    fun testAutoDismissUserMessage() = runTest {
        composeTestRule.setContent {
            stackedSnackbarHostState = StackedSnackbarHostState(LocalAccessibilityManager.current!!, this)
            LocalAccessibilityManager.current
            StackedSnackbarHost(stackedSnackbarHostState, onActionClick = {})
        }

        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, true)

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        advanceTimeBy(15000L)
        val recordingStarted = composeTestRule.activity.getString(R.string.kaleyra_recording_started_message)
        composeTestRule.onNodeWithText(recordingStarted).assertIsNotDisplayed()
    }
}