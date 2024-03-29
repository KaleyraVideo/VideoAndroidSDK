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

package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.conversation.Message
import com.kaleyra.video.conversation.Messages
import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal object MessagesMapper {

    private const val UnreadMessageFetchCount = 5
//    private const val NewMessageChainDeltaMillis = 5 * 60 * 1000L

    internal fun Message.toUiMessage(): com.kaleyra.video_sdk.chat.conversation.model.Message {
        val text = (content as? Message.Content.Text)?.message ?: ""
        val time = TimestampUtils.parseTime(creationDate.time)

        return if (this is com.kaleyra.video.conversation.OtherMessage)
            com.kaleyra.video_sdk.chat.conversation.model.Message.OtherMessage(id, text, time, creator.userId)
        else
            com.kaleyra.video_sdk.chat.conversation.model.Message.MyMessage(id, text, time, state.mapToUiState())
    }

    internal fun Flow<Message.State>.mapToUiState(): Flow<com.kaleyra.video_sdk.chat.conversation.model.Message.State> =
        map { state ->
            when (state) {
                is Message.State.Sending -> com.kaleyra.video_sdk.chat.conversation.model.Message.State.Sending
                is Message.State.Sent -> com.kaleyra.video_sdk.chat.conversation.model.Message.State.Sent
                is Message.State.Received -> com.kaleyra.video_sdk.chat.conversation.model.Message.State.Received
                is Message.State.Created -> com.kaleyra.video_sdk.chat.conversation.model.Message.State.Created
                else -> com.kaleyra.video_sdk.chat.conversation.model.Message.State.Read
            }
        }

    internal fun List<Message>.mapToConversationItems(firstUnreadMessageId: String? = null): List<ConversationItem> {
        val items = mutableListOf<ConversationItem>()

        forEachIndexed { index, message ->
            val previousMessage = getOrNull(index + 1)
            val nextMessage = getOrNull(index - 1)

            val messageElement = ConversationItem.Message(
                message = message.toUiMessage(),
                isFirstChainMessage = if (previousMessage != null) message.isFirstChainMessage(previousMessage) else true,
                isLastChainMessage = if (nextMessage != null) message.isLastChainMessage(nextMessage) else true
            )
            items.add(messageElement)

            if (firstUnreadMessageId != null && message.id == firstUnreadMessageId) {
                items.add(ConversationItem.UnreadMessages)
            }

            if (previousMessage == null ||
                !TimestampUtils.isSameDay(message.creationDate.time, previousMessage.creationDate.time)
            ) {
                items.add(ConversationItem.Day(message.creationDate.time))
            }
        }

        return items
    }

    private fun Message.isFirstChainMessage(previousMessage: Message) =
        previousMessage.creator.userId != creator.userId
//                || areDateDifferenceGreaterThanMillis(creationDate, previousMessage.creationDate, NewMessageChainDeltaMillis)
                || !TimestampUtils.isSameDay(creationDate.time, previousMessage.creationDate.time)

    private fun Message.isLastChainMessage(nextMessage: Message) =
        nextMessage.creator.userId != creator.userId
//                || areDateDifferenceGreaterThanMillis(nextMessage.creationDate, creationDate, NewMessageChainDeltaMillis)
                || !TimestampUtils.isSameDay(creationDate.time, nextMessage.creationDate.time)

    internal suspend fun findFirstUnreadMessageId(
        messages: Messages,
        fetch: suspend (Int) -> Result<Messages>
    ): String? {
        val flow = MutableSharedFlow<String?>(replay = 1, extraBufferCapacity = 1)
        findFirstUnreadMessageInternal(messages, fetch) {
            flow.tryEmit(it)
        }
        return flow.first()
    }

    private suspend fun findFirstUnreadMessageInternal(
        messages: Messages,
        fetch: suspend (Int) -> Result<Messages>,
        continuation: (String?) -> Unit
    ) {
        val list = messages.other
        val message = list.lastOrNull { it.state.value is Message.State.Received } ?: kotlin.run {
            continuation(null)
            return
        }
        val index = list.indexOf(message)
        val size = list.size

        if (index == size - 1) {
            val result = fetch(UnreadMessageFetchCount).getOrNull()
            when {
                result == null -> continuation(null)
                result.other.isEmpty() -> continuation(message.id)
                else -> findFirstUnreadMessageInternal(result, fetch, continuation)
            }
        } else continuation(message.id)
    }
}
