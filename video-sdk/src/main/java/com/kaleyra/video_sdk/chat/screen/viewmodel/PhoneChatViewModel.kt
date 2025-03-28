/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.chat.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conversation.Message
import com.kaleyra.video_common_ui.ChatViewModel
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.ConversationState
import com.kaleyra.video_sdk.chat.mapper.CallStateMapper.hasActiveCall
import com.kaleyra.video_sdk.chat.mapper.ChatButtonsMapper.mapToChatActions
import com.kaleyra.video_sdk.chat.mapper.ConversationStateMapper.toConnectionState
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.video_sdk.chat.mapper.ParticipantsMapper.toOtherParticipantsState
import com.kaleyra.video_sdk.chat.mapper.ParticipantsMapper.toParticipantsDetails
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class PhoneChatViewModelState(
    val isGroupChat: Boolean = false,
    val recipientDetails: ChatParticipantDetails = ChatParticipantDetails(),
    val chatName: String = "",
    val chatImage: ImmutableUri = ImmutableUri(),
    val actions: ImmutableSet<ChatAction> = ImmutableSet(),
    val connectionState: ConnectionState = ConnectionState.Unknown,
    val participantsDetails: ImmutableMap<String, ChatParticipantDetails> = ImmutableMap(),
    val participantsState: ChatParticipantsState = ChatParticipantsState(),
    val conversationState: ConversationState = ConversationState(),
    val isInCall: Boolean = false,
    val isUserConnected: Boolean = true,
    val isDeleted: Boolean = false,
    val hasFailedCreation: Boolean = false
) {

    fun toUiState(): ChatUiState {
        return if (isGroupChat) {
            ChatUiState.Group(
                name = chatName,
                image = chatImage,
                participantsDetails = participantsDetails,
                participantsState = participantsState,
                actions = actions,
                connectionState = connectionState,
                conversationState = conversationState,
                isInCall = isInCall,
                isUserConnected = isUserConnected,
                isDeleted = isDeleted,
                hasFailedCreation = hasFailedCreation
            )
        } else {
            ChatUiState.OneToOne(
                recipientDetails = recipientDetails,
                actions = actions,
                connectionState = connectionState,
                conversationState = conversationState,
                isInCall = isInCall,
                isUserConnected = isUserConnected,
                isDeleted = isDeleted,
                hasFailedCreation = hasFailedCreation
            )
        }
    }
}

internal class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure), UserMessageViewModel {

    private val firstUnreadMessageId = MutableStateFlow<String?>(null)

    private val viewModelState = MutableStateFlow(PhoneChatViewModelState())

    val uiState = viewModelState
        .map(PhoneChatViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())

    val theme: StateFlow<Theme> = company
        .flatMapLatest { it.combinedTheme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme())

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        viewModelScope.launch {
            val chat = chat.first()
            val isGroupChat = chat.isGroup && chat.participants.value.others.size > 1
            viewModelState.update {
                it.copy(
                    isGroupChat = isGroupChat,
                    chatName = chat.name ?: ""
                )
            }

            if (isGroupChat) {
                // TODO bind the chat name and chat image when the mtm chats will be available
                participants.first()
                    .toOtherParticipantsState()
                    .onEach { participantsState -> viewModelState.update { it.copy(participantsState = participantsState) } }
                    .launchIn(this)

                participants
                    .map { it.toParticipantsDetails() }
                    .onEach { participantsDetails ->
                        viewModelState.update {
                            it.copy(
                                participantsDetails = participantsDetails
                            )
                        }
                    }
                    .launchIn(this)
            } else {
                participants
                    .map { it.toParticipantsDetails() }
                    .onEach { participantsDetails ->
                        val others = participants.getValue()?.others
                        val recipientUserId = others?.firstOrNull()?.userId
                        val recipientDetails = recipientUserId?.let { participantsDetails[it] }
                        if (recipientDetails != null) viewModelState.update {
                            it.copy(
                                recipientDetails = recipientDetails
                            )
                        }
                    }.launchIn(this)
            }

            chat.name?.let { chatName ->
                viewModelState.update {
                    it.copy(chatName = chatName)
                }
            }

            findFirstUnreadMessageId(chat.messages.first(), chat::fetch).also {
                firstUnreadMessageId.value = it
            }

            messages
                .onEach { messagesUI ->
                    val newItems = messagesUI.list.mapToConversationItems(firstUnreadMessageId = firstUnreadMessageId.value)
                    updateConversationItems(newItems)
                }
                .launchIn(this)
        }

        buttons
            // mapNotNull instead of map because sometimes is received a null value causing crash
            .mapNotNull {
                it.mapToChatActions(call = { callType, maxDuration, recordingType ->
                    call(callType, maxDuration, recordingType)
                })
            }
            .onEach { actions -> viewModelState.update { it.copy(actions = ImmutableSet(actions)) } }
            .launchIn(viewModelScope)

        call
            .hasActiveCall()
            .onEach { hasActiveCall -> viewModelState.update { it.copy(isInCall = hasActiveCall) } }
            .launchIn(viewModelScope)

        conversation
            .toConnectionState()
            .onEach { connectionState -> viewModelState.update { it.copy(connectionState = connectionState) } }
            .launchIn(viewModelScope)

        chat
            .flatMapLatest { it.unreadMessagesCount }
            .onEach { count -> updateUnreadMessagesCount(count) }
            .launchIn(viewModelScope)

        chat
            .flatMapLatest { it.state }
            .onEach { state ->
                when (state) {
                    Chat.State.Closed.Companion -> viewModelState.update { it.copy(isDeleted = true) }
                    Chat.State.Closed.Error.InvalidParticipants -> viewModelState.update { it.copy(hasFailedCreation = true, isDeleted = true) }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)

        connectedUser
            .onEach { user -> viewModelState.update { it.copy(isUserConnected = user != null) } }
            .launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val chat = chat.getValue() ?: return
        chat.add(Message.Content.Text(text))
        firstUnreadMessageId.value = null
    }

    fun typing() {
        val chat = chat.getValue() ?: return
        chat.participants.value.me?.typing()
    }

    fun fetchMessages() {
        viewModelScope.launch {
            updateFetchingState(isFetching = true)
            chat.first().fetch(FETCH_COUNT)
            updateFetchingState(isFetching = false)
        }
    }

    fun onMessageScrolled(message: ConversationItem.Message) {
        val messages = messages.getValue()?.other ?: return
        messages.firstOrNull { it.id == message.id }?.markAsRead()
    }

    fun onAllMessagesScrolled() {
        val messages = messages.getValue()?.other ?: return
        messages.firstOrNull()?.markAsRead()
    }

    fun showCall() {
        val call = call.getValue() ?: return
        call.show()
    }

    fun getLoggedUserId(): String? = connectedUser.getValue()?.userId

    private fun updateUnreadMessagesCount(count: Int) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(unreadMessagesCount = count)
            it.copy(conversationState = conversationState)
        }
    }

    private fun updateConversationItems(newItems: List<ConversationItem>) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(conversationItems = ImmutableList(newItems))
            it.copy(conversationState = conversationState)
        }
    }

    private fun updateFetchingState(isFetching: Boolean) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(isFetching = isFetching)
            it.copy(conversationState = conversationState)
        }
    }

    private fun call(callType: Call.Type, maxDuration: Long? = null, recordingType: Call.Recording.Type? = null) {
        val conference = conference.getValue() ?: return
        if (conference.state.value !is State.Connected) return
        val chat = chat.getValue() ?: return
        val userId = chat.participants.value.others.map { it.userId }
        conference.call(
            userIDs = userId,
            options = {
                this.callType = callType
                this.maxDuration = maxDuration
                this.recordingType = recordingType ?: Call.Recording.Type.Never
            },
            chatId = chat.id)
    }

    companion object {
        private const val FETCH_COUNT = 50

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PhoneChatViewModel(configure) as T
                }
            }
    }
}
