package com.kaleyra.video_sdk.common.snackbarm3.model

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
    private val _alertMessagesFlow = MutableStateFlow<List<AlertMessage>>(listOf())
    private val _userMessagesFlow = MutableSharedFlow<List<UserMessage>>(replay = 0)
    val userMessages: SharedFlow<List<UserMessage>> = _userMessagesFlow
    val alertMessages: StateFlow<List<AlertMessage>> = _alertMessagesFlow

    fun addAlertMessages(messages: List<AlertMessage>) = scope.launch {
        _alertMessages?.filter { alertMessage -> !messages.contains(alertMessage) }?.forEach { removedAlertMessage ->
            internalRemoveUserMessage(removedAlertMessage)
        }

        if (messages.isEmpty()) return@launch

        _alertMessages = _alertMessages ?: mutableListOf()

        messages.map { alertMessage ->
            if (!_alertMessages!!.contains(alertMessage)) _alertMessages!!.add(alertMessage)
        }
        updateUserMessages()
    }

    fun addUserMessage(userMessage: UserMessage, autoDismiss: Boolean = false) = scope.launch { internalAddUserMessage(userMessage, autoDismiss) }

    private fun internalAddUserMessage(userMessage: UserMessage, autoDismiss: Boolean = false) {
        with(_userMessages) {
            when (userMessage) {
                is RecordingMessage -> this.removeIf { it is RecordingMessage }
                is AudioConnectionFailureMessage -> this.removeIf { it is AudioConnectionFailureMessage }
                is UsbCameraMessage -> this.removeIf { it is UsbCameraMessage }
                else -> Unit
            }

            if (contains(userMessage)) return

            add(0, userMessage)
        }
        updateUserMessages()

        if (!autoDismiss) return

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

    private fun updateUserMessages() = scope.launch {
        _userMessagesFlow.emit(mutableListOf(*_userMessages.toTypedArray()))
        _alertMessages?.let { alertMessages -> _alertMessagesFlow.emit(listOf(*alertMessages.toTypedArray())) }
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