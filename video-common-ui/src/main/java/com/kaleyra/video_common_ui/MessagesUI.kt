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

import android.net.Uri
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conversation.ChatParticipants
import com.kaleyra.video.conversation.Message
import com.kaleyra.video.conversation.Messages
import com.kaleyra.video.conversation.OtherMessage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.notification.ChatNotificationMessage
import com.kaleyra.video_common_ui.notification.CustomChatNotificationManager
import com.kaleyra.video_common_ui.notification.DisplayedChatActivity
import com.kaleyra.video_common_ui.notification.DisplayedChatActivity.Companion.chatId
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.AppLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

/**
 * The messages UI
 *
 * @property chatActivityClazz Class<*>
 * @property chatCustomNotificationActivityClazz Class<*>?
 * @constructor
 */
class MessagesUI(
    messages: Messages,
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null
) : Messages by messages {

    /**
     * Shows the notification of the unread messages
     *
     * @param chatUI The Chat to be shown as unread messages
     */
    suspend fun showUnreadMsgs(chatUI: ChatUI) {
        if (DisplayedChatActivity.chatId.value == chatUI.id) return
        chatCustomNotificationActivityClazz?.let {
            showCustomInAppNotification(chatUI, it)
        } ?: showNotification(chatUI)
    }

    private suspend fun showCustomInAppNotification(
        chatUI: ChatUI,
        chatCustomNotificationActivity: Class<*>
    ) {
        if (AppLifecycle.isInForeground.value) CustomChatNotificationManager.notify(chatUI.id, chatCustomNotificationActivity)
        else showNotification(chatUI, chatCustomNotificationActivity)
    }

    private suspend fun showNotification(
        chatUI: ChatUI,
        chatCustomNotificationActivity: Class<*>? = null
    ) {
        ContactDetailsManager.refreshContactDetails(*chatUI.participants.value.list.map { it.userId }.toTypedArray())

        val messages = other
            .filter { it.state.value is Message.State.Received }
            .map { it.toChatNotificationMessage(chatUI.participants.value) }
            .sortedBy { it.timestamp }
        val me = chatUI.participants.value.me ?: return

        val notification = NotificationManager.buildChatNotification(
            me.userId,
            me.combinedDisplayName.filterNotNull().firstOrNull() ?: me.userId,
            me.combinedDisplayImage.filterNotNull().firstOrNull() ?: Uri.EMPTY,
            // Set the chatId not null if it is a one to one chat
            chatUI.id,
            chatUI.isGroup,
            if (chatUI.isGroup) {
                when {
                    chatUI.name.isNullOrBlank() ->
                        chatUI.participants.value.others.map { it.combinedDisplayName.filterNotNull().firstOrNull() }.toSet().joinToString(", ")
                    else -> chatUI.name
                }
            } else "",
            messages,
            chatActivityClazz,
            chatCustomNotificationActivity
        )
        if (DisplayedChatActivity.chatId.value == chatUI.id) return
        NotificationManager.notify(chatUI.id.hashCode(), notification)
    }

    private suspend fun OtherMessage.toChatNotificationMessage(chatParticipants: ChatParticipants): ChatNotificationMessage {
        val otherParticipant = chatParticipants.others.find { it.userId == creator.userId }
        return ChatNotificationMessage(
            creator.userId,
            otherParticipant?.combinedDisplayName?.filterNotNull()?.firstOrNull() ?: otherParticipant?.userId ?: " ",
            otherParticipant?.combinedDisplayImage?.filterNotNull()?.firstOrNull() ?: Uri.EMPTY,
            (content as? Message.Content.Text)?.message ?: "",
            creationDate.time
        )
    }
}
