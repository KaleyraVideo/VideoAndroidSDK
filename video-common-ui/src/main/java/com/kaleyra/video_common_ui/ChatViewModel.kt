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

package com.kaleyra.video_common_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.video.State
import com.kaleyra.video.conversation.Chat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.lastOrNull

/**
 * ChatViewModel representation of the chat view model
 * @constructor
 */
open class ChatViewModel(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    /**
     * Current chat flow
     */
    val chat = _chat.asSharedFlow()

    /**
     * Current call flow
     */
    val call = conference.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    /**
     * Messages flow
     */
    val messages = chat.flatMapLatest { it.messages }.shareInEagerly(viewModelScope)

    /**
     * Chat UI Button flow
     */
    val buttons = chat.flatMapLatest { it.buttons }.shareInEagerly(viewModelScope)

    /**
     * Chat participants flow
     */
    val participants = chat.flatMapLatest { it.participants }.shareInEagerly(viewModelScope)

    /**
     * Set the current one-to-one chat by passing the other participant's userId
     * @param loggedUserId String optional logged user identification if the user has already connected or is connecting
     * @param chatId String Chat id associated with the chat to be set
     * @return ChatUI? the retrieved ChatUI if available
     */
    suspend fun setChat(loggedUserId: String?, chatId: String): ChatUI? {
        val conversation = conversation.first()
        if (!KaleyraVideo.isConfigured) {
            val hasConfigured = requestConfiguration()
            if (!hasConfigured) return null
        }
        if (KaleyraVideo.conversation.state.value is State.Disconnected) {
            requestConnect(loggedUserId)
        }

        val getChatById: (ChatUI) -> Boolean = {
            it.id == chatId
        }
        val getChatByServerId: suspend (ChatUI) -> Boolean = {
            it.serverId.first() == chatId
        }

        var chat: ChatUI? = null

        kotlin.runCatching {
            conversation.state.first { it is State.Connected }
            var foundByServerId = false
            val foundById = conversation.chats.getValue()?.any { getChatById(it) } ?: false
            if (!foundById) {
                val chatFoundByServerId = conversation.find(chatId).await()
                foundByServerId = chatFoundByServerId.isSuccess
            }

            if (foundById || foundByServerId) {
                conversation.chats.first {
                    it.lastOrNull { getChatById(it) || getChatByServerId(it) }?.let {
                        _chat.emit(it)
                        chat = it
                    } != null
                }
            }
        }

        return chat
    }
}
