@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.ui.call.usermessage.model

import com.kaleyra.video_sdk.common.snackbar.model.StackedSnackbarHostMessagesHandler
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardRequestMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StackedSnackbarHandlerTest {

    @Test
    fun testUserMessagePosted() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Started)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.contains(RecordingMessage.Started) }
    }

    @Test
    fun testUserMessagePostedAndAutoDismissed() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Started, true)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.contains(RecordingMessage.Started) }
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 0 }
    }

    @Test
    fun testMultipleUserMessagePosted() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostMessagesHandler.addUserMessage(AudioConnectionFailureMessage.Generic, false)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 2 && it.contains(RecordingMessage.Started) && it.contains(AudioConnectionFailureMessage.Generic) }
    }

    @Test
    fun testRecordingUserMessageReplaced() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Started, false)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Stopped, false)
        stackedSnackbarHostMessagesHandler.addUserMessage(RecordingMessage.Failed, false)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 1 && it.contains(RecordingMessage.Failed) }
    }

    @Test
    fun testAudioErrorUserMessageReplaced() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(AudioConnectionFailureMessage.Generic, false)
        stackedSnackbarHostMessagesHandler.addUserMessage(AudioConnectionFailureMessage.InSystemCall, false)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 1 && it.contains(AudioConnectionFailureMessage.InSystemCall) }
    }

    @Test
    fun testUsbCameraUserMessageReplaced() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(UsbCameraMessage.Connected("usb"), false)
        stackedSnackbarHostMessagesHandler.addUserMessage(UsbCameraMessage.Disconnected, false)
        stackedSnackbarHostMessagesHandler.addUserMessage(UsbCameraMessage.NotSupported, false)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 1 && it.contains(UsbCameraMessage.NotSupported) }
    }

    @Test
    fun testWhiteboardRequestUserMessageReplaced() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addUserMessage(WhiteboardRequestMessage.WhiteboardShowRequestMessage("user"), false)
        stackedSnackbarHostMessagesHandler.addUserMessage(WhiteboardRequestMessage.WhiteboardHideRequestMessage("user"), false)
        stackedSnackbarHostMessagesHandler.userMessages.first { it.size == 1 && it.firstOrNull { it is WhiteboardRequestMessage.WhiteboardShowRequestMessage } == null }
    }

    @Test
    fun testAlertMessagesAdded() = runTest {
        val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(scope = backgroundScope)
        stackedSnackbarHostMessagesHandler.addAlertMessages(listOf(AlertMessage.WaitingForOtherParticipantsMessage, AlertMessage.AutomaticRecordingMessage))
        stackedSnackbarHostMessagesHandler.alertMessages.first {
            it.size == 2
                && it.contains(AlertMessage.WaitingForOtherParticipantsMessage)
                && it.contains(AlertMessage.AutomaticRecordingMessage)
        }
    }
}
