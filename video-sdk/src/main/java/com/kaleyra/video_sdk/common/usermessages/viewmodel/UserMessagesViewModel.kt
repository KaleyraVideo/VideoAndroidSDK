package com.kaleyra.video_sdk.common.usermessages.viewmodel

import androidx.compose.ui.platform.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.snackbar.model.StackedSnackbarHostMessagesHandler
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class UserMessagesViewModel(val accessibilityManager: AccessibilityManager?, configure: suspend () -> Configuration) : BaseViewModel<StackedSnackbarUiState>(configure) {

    private val stackedSnackbarHostMessagesHandler = StackedSnackbarHostMessagesHandler(accessibilityManager = accessibilityManager)
    override fun initialState() = StackedSnackbarUiState()

    private val userMessageChannel = Channel<List<UserMessage>>(Channel.BUFFERED)

    val userMessage: Flow<ImmutableList<UserMessage>> = userMessageChannel.receiveAsFlow().map { ImmutableList(it) }

    init {

        println("INIT USER MESSAGES VIEW MODEL: $this")

        call.onEach {
            with(CallUserMessagesProvider) {

                start(call.getValue()!!)

                userMessage
                    .onEach {
                        stackedSnackbarHostMessagesHandler.addUserMessage(it, true)
                    }
                    .launchIn(viewModelScope)

                alertMessages
                    .onEach {
                        stackedSnackbarHostMessagesHandler.addAlertMessages(it)
                    }
                    .launchIn(viewModelScope)
            }

            with(stackedSnackbarHostMessagesHandler) {
                userMessages
                    .onEach { newUserMessages ->
                        runCatching {
                            userMessageChannel.send(newUserMessages)
                        }.onFailure {
                            println(it)
                        }
                    }.launchIn(viewModelScope)
                alertMessages
                    .onEach { newAlertMessages ->
                        _uiState.update { it.copy(alertMessages = ImmutableList(newAlertMessages.toList())) }
                    }.launchIn(viewModelScope)
            }
        }
            .onCompletion {
                println("completion of viewmodel scope job on $this@UserMessagesViewModels")
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        println("user messages view model on cleared: $this")
    }

    fun dismiss(userMessage: UserMessage) {
        stackedSnackbarHostMessagesHandler.removeUserMessage(userMessage)
    }

    fun dismissMessages() {
        stackedSnackbarHostMessagesHandler.removeUserMessages()
    }

    companion object {
        fun provideFactory(accessibilityManager: AccessibilityManager?, configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return  UserMessagesViewModel(accessibilityManager, configure) as T
                }
            }
    }
}