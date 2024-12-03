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
     * Chat UI Actions flow
     */
    val actions = chat.flatMapLatest { it.actions }.shareInEagerly(viewModelScope)

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

        val getChatById: suspend (ChatUI) -> Boolean = {
            it.id == chatId || it.serverId.first() == chatId
        }

        var chat: ChatUI? = null

        kotlin.runCatching {
            if (conversation.chats.getValue()?.firstOrNull { getChatById(it) } == null)
                conversation.find(chatId).await()

            conversation.chats.first {
                it.firstOrNull { getChatById(it) }?.let {
                    _chat.emit(it)
                    chat = it
                } != null
            }
        }

        return chat
    }
}
