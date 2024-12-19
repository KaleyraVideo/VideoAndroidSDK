package com.kaleyra.video_sdk.common.snackbar.model

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.AccessibilityManager
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardRequestMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private val StackedSnackBarAutoDismissDefaultDuration = SnackbarDuration.Short

@Stable
class StackedSnackbarHostMessagesHandler(val accessibilityManager: AccessibilityManager? = null, val scope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())) : UiState {

    private var _alertMessages: MutableList<AlertMessage>? = null
    private val _userMessages = mutableListOf<UserMessage>()
    private val _alertMessagesFlow = MutableStateFlow<Set<AlertMessage>>(setOf())
    private val _userMessagesFlow = MutableSharedFlow<List<UserMessage>>(replay = 0)
    val userMessages: SharedFlow<List<UserMessage>> = _userMessagesFlow
    val alertMessages: StateFlow<Set<AlertMessage>> = _alertMessagesFlow

    fun addAlertMessages(messages: Set<AlertMessage>) = scope.launch {

        if (messages.any { it is AlertMessage.CustomMessage }) {
            _alertMessages?.removeIf { it is AlertMessage.CustomMessage }
            updateUserMessages()
        }

        _alertMessages?.filter { alertMessage -> !messages.contains(alertMessage) }?.forEach { removedAlertMessage ->
            internalRemoveUserMessage(removedAlertMessage)
        }

        _alertMessages = _alertMessages ?: mutableListOf()

        messages.map { alertMessage ->
            if (!_alertMessages!!.contains(alertMessage)) _alertMessages!!.add(alertMessage)
        }

        if (messages.isEmpty()) _alertMessages!!.clear()

        updateUserMessages()
    }

    fun addUserMessage(userMessage: UserMessage, autoDismiss: Boolean = false) = scope.launch { internalAddUserMessage(userMessage, autoDismiss) }

    private fun internalAddUserMessage(userMessage: UserMessage, autoDismiss: Boolean = false) = scope.launch {
        with(_userMessages) {
            when (userMessage) {
                is RecordingMessage -> this.removeIf { it is RecordingMessage }
                is AudioConnectionFailureMessage -> this.removeIf { it is AudioConnectionFailureMessage }
                is UsbCameraMessage -> this.removeIf { it is UsbCameraMessage }
                is WhiteboardRequestMessage -> this.removeIf { it is WhiteboardRequestMessage }
                is AlertMessage.CustomMessage -> this.removeIf { it.id == userMessage.id }
                else -> Unit
            }

            if (contains(userMessage)) return@launch

            add(0, userMessage)
        }

        updateUserMessages()

        if (!autoDismiss) return@launch

        autoDismissUserMessage(userMessage)
    }

    private fun autoDismissUserMessage(userMessage: UserMessage) = scope.launch {
        delay(StackedSnackBarAutoDismissDefaultDuration.toMillis(userMessage is PinScreenshareMessage, accessibilityManager))
        removeUserMessage(userMessage)
    }

    fun removeUserMessage(userMessage: UserMessage) = scope.launch {
        internalRemoveUserMessage(userMessage)
        updateUserMessages()
    }

    private fun internalRemoveUserMessage(userMessage: UserMessage) {
        if (!_userMessages.remove(userMessage)) _alertMessages?.remove(userMessage)
    }

    private suspend fun updateUserMessages() {
        _userMessagesFlow.emit(mutableListOf(*_userMessages.toTypedArray()))
        _alertMessages?.let { alertMessages ->
            _alertMessagesFlow.emit(setOf(*alertMessages.toTypedArray()))
        }
    }
}

internal fun SnackbarDuration.toMillis(
    hasAction: Boolean,
    accessibilityManager: AccessibilityManager?
): Long {
    val duration = when (this) {
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
        SnackbarDuration.Long -> 10000L
        SnackbarDuration.Short -> 4000L
    }
    return accessibilityManager?.calculateRecommendedTimeoutMillis(
        duration,
        containsIcons = true,
        containsText = true,
        containsControls = hasAction
    ) ?: duration
}