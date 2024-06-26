@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.ui.call.usermessage.model

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.AccessibilityManager
import com.kaleyra.video_sdk.common.snackbarm3.model.StackedSnackbarHostState
import com.kaleyra.video_sdk.common.snackbarm3.model.toMillis
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class StackedStackbarHostStateTest {

    @Test
    fun testUserMessagePosted() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started)
        stackedSnackbarHostState.userMessages.first { it.contains(RecordingMessage.Started) }
    }

    @Test
    fun testUserMessagePostedAndAutoDismissed() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, true)
        stackedSnackbarHostState.userMessages.first { it.contains(RecordingMessage.Started) }
        stackedSnackbarHostState.userMessages.first { it.size == 0 }
    }

    @Test
    fun testMultipleUserMessagePosted() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostState.addUserMessage(AudioConnectionFailureMessage.Generic, false)
        stackedSnackbarHostState.userMessages.first { it.size == 2 && it.contains(RecordingMessage.Started) && it.contains(AudioConnectionFailureMessage.Generic) }
    }

    @Test
    fun testRecordingUserMessageReplaced() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Stopped, false)
        stackedSnackbarHostState.addUserMessage(RecordingMessage.Failed, false)
        stackedSnackbarHostState.userMessages.first { it.size == 1 && it.contains(RecordingMessage.Failed) }
    }

    @Test
    fun testAudioErrorUserMessageReplaced() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(AudioConnectionFailureMessage.Generic, false)
        stackedSnackbarHostState.addUserMessage(AudioConnectionFailureMessage.InSystemCall, false)
        stackedSnackbarHostState.userMessages.first { it.size == 1 && it.contains(AudioConnectionFailureMessage.InSystemCall) }
    }

    @Test
    fun testUsbCameraUserMessageReplaced() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addUserMessage(UsbCameraMessage.Connected("usb"), false)
        stackedSnackbarHostState.addUserMessage(UsbCameraMessage.Disconnected, false)
        stackedSnackbarHostState.addUserMessage(UsbCameraMessage.NotSupported, false)
        stackedSnackbarHostState.userMessages.first { it.size == 1 && it.contains(UsbCameraMessage.NotSupported) }
    }

    @Test
    fun testAlertMessagesAdded() = runTest {
        val stackedSnackbarHostState = StackedSnackbarHostState(scope = backgroundScope)
        stackedSnackbarHostState.addAlertMessages(listOf(AlertMessage.WaitingForOtherParticipantsMessage, AlertMessage.AutomaticRecordingMessage))
        stackedSnackbarHostState.alertMessages.first {
            it.size == 2
                && it.contains(AlertMessage.WaitingForOtherParticipantsMessage)
                && it.contains(AlertMessage.AutomaticRecordingMessage)
        }
    }
}
