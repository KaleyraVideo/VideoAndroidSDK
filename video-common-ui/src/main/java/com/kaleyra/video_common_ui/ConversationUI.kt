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

import android.content.Context
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conversation.Conversation
import com.kaleyra.video.conversation.Message
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * The conversation UI
 *
 * @property conversation The Conversation delegate
 * @property chatActivityClazz The chat activity Class<*>
 * @property chatCustomNotificationActivityClazz The custom chat notification activity Class<*>
 * @constructor
 */
class ConversationUI(
    private val conversation: Conversation,
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null
) : Conversation by conversation {

    private var chatScope = CoroutineScope(Dispatchers.IO)
    private var unreadMessagesScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private var lastMessagePerChat: HashMap<String, String> = hashMapOf()

    private var mappedChats: List<ChatUI> = listOf()

    /**
     * @suppress
     */
    override val chats: SharedFlow<List<ChatUI>> = conversation.chats.map {
        it.map { chat -> getOrCreateChatUI(chat) }
    }.shareIn(chatScope, SharingStarted.Eagerly, 1)

    /**
     * WithUI flag, set to true to show the chat notifications, false otherwise
     */
    var withUI: Boolean = true

    /**
     * The chat actions that will be set on every chat
     */
    var chatActions: Set<ChatUI.Action> = ChatUI.Action.default


    init {
        listenToMessages()
    }

    internal fun dispose() {
        chatScope.cancel()
        unreadMessagesScope.cancel()
    }

    /**
     * Show the chat ui
     * @param context context to bind the chat ui
     * @param loggedUserId String optional logged user identification if already connected or connecting
     * @param chat The chat object that should be shown.
     */
    fun show(context: Context, loggedUserId: String?, chat: ChatUI) {
        chatScope.launch {
            val userIds = chat.participants.value.list.map { it.userId }
            ContactDetailsManager.refreshContactDetails(*userIds.toTypedArray())
        }
        KaleyraUIProvider.startChatActivity(
            context,
            chatActivityClazz,
            loggedUserId,
            chat.id
        )
    }

    /**
     * @suppress
     */
    override fun create(userId: String): Result<ChatUI> = conversation.create(userId).map {
        getOrCreateChatUI(it)
    }

    /**
     * @suppress
     */
    override fun create(userIds: List<String>, friendlyName: String?) = conversation.create(userIds, friendlyName).map {
        getOrCreateChatUI(it)
    }

    /**
     * Given a user, open a chat ui.
     * @param context launching context of the chat ui
     * @param userId the user id of the user to chat with
     */
    fun chat(context: Context, userId: String): Result<ChatUI> = create(userId).onSuccess {
        show(context, KaleyraVideo.connectedUser.value?.userId, it)
    }

    /**
     * Given a list of users, open a chat ui.
     * @param context launching context of the chat ui
     * @param userIds the user ids of the users to chat with
     * @param friendlyName the chat friendly name
     */
    fun chat(context: Context, userIds: List<String>, friendlyName: String? = null): Result<ChatUI> = create(userIds, friendlyName).onSuccess {
        show(context, KaleyraVideo.connectedUser.value?.userId, it)
    }

    override fun find(chatId: String): Deferred<Result<ChatUI>> = CompletableDeferred<Result<ChatUI>>().apply {
        chatScope.launch {
            runCatching {
                this@apply.complete(Result.success(getOrCreateChatUI(conversation.find(chatId).await().getOrNull()!!)))
            }.onFailure {
                this@apply.completeExceptionally(CancellationException(it.message))
            }
        }
    }

    private fun listenToMessages() {
        val listenedChats = mutableListOf<String>()
        chats.onEach { chats ->
            chats.forEach { chat ->
                if (listenedChats.contains(chat.id)) return@forEach
                listenedChats.add(chat.id)
                val chatParticipants = chat.participants.value
                ContactDetailsManager.refreshContactDetails(*chatParticipants.list.map { it.userId }.toTypedArray())
                chat.messages.onEach messagesUI@{
                    if (!withUI) return@messagesUI
                    val lastMessage = it.other.firstOrNull { it.state.value is Message.State.Received }
                    if (lastMessage == null || lastMessagePerChat[chat.id] == lastMessage.id) return@messagesUI
                    lastMessagePerChat[chat.id] = lastMessage.id
                    it.showUnreadMsgs(chat)
                }.onCompletion {
                    listenedChats.remove(chat.id)
                }.launchIn(unreadMessagesScope)
            }
        }.launchIn(unreadMessagesScope)
    }

    fun show(context: Context, chat: Chat) {
        val loggedUserId = KaleyraVideo.connectedUser.value?.userId ?: return
        show(context, loggedUserId, getOrCreateChatUI(chat))
    }

    private fun getOrCreateChatUI(chat: Chat): ChatUI = synchronized(this) {
        mappedChats.firstOrNull { it.id == chat.id } ?: createChatUI(chat)
    }

    private fun createChatUI(chat: Chat): ChatUI {
        return ChatUI(chat = chat, actions = MutableStateFlow(chatActions), chatActivityClazz = chatActivityClazz, chatCustomNotificationActivityClazz = chatCustomNotificationActivityClazz).apply { mappedChats = mappedChats + this }
    }
}

