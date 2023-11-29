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
     * @param userId String the other participant's userId
     * @return ChatUI? the retrieved ChatUI if available
     */
    suspend fun setChat(userId: String): ChatUI? {
        val conversation = conversation.first()
        val chat = conversation.create(userId).getOrNull() ?: return null
        _chat.tryEmit(chat)
        return chat
    }
}
