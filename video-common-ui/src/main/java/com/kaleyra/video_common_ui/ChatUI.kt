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

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video_common_ui.chat.ChatUIButtonsProvider
import com.kaleyra.video_common_ui.chat.DefaultChatUIButtonsProvider
import com.kaleyra.video_common_ui.common.UIButton
import com.kaleyra.video_common_ui.utils.extensions.mapToSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * The chat UI
 *
 * @property actions The MutableStateFlow containing the set of actions
 * @property chatActivityClazz The chat activity Class<*>
 * @property chatCustomNotificationActivityClazz The custom chat notification activity Class<*>
 * @constructor
 */
class ChatUI(
    chat: Chat,
    @Deprecated(
        message = "Action mutable state flow is deprecated and it will be removed in a further release.\nPlease update ChatUI Buttons via buttonsProvider callback.",
        replaceWith = ReplaceWith("buttonsProvider = { chatButtons ->\nchatButtons\n}"))
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(Action.default),
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    chatUIButtonsProvider: ChatUIButtonsProvider = DefaultChatUIButtonsProvider(actions, scope),
) : Chat by chat,
    ChatUIButtonsProvider by chatUIButtonsProvider {

    private var chatScope = CoroutineScope(Dispatchers.IO)

    /**
     * @suppress
     */
    override val messages: SharedFlow<MessagesUI> = chat.messages.mapToSharedFlow(chatScope) { MessagesUI(it, chatActivityClazz, chatCustomNotificationActivityClazz) }

    /**
     * The chat button sealed class
     */
    sealed class Button : UIButton {
        /**
         * @suppress
         */
        companion object Collections {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    Participants,
                    Call(preferredType = com.kaleyra.video.conference.Call.PreferredType.audioOnly()),
                    Call(preferredType = com.kaleyra.video.conference.Call.PreferredType.audioUpgradable()),
                    Call(preferredType = com.kaleyra.video.conference.Call.PreferredType.audioVideo())
                )
            }

            val default by lazy {
                setOf(
                    Participants,
                    Call(preferredType = com.kaleyra.video.conference.Call.PreferredType.audioUpgradable()),
                    Call(preferredType = com.kaleyra.video.conference.Call.PreferredType.audioVideo())
                )
            }
        }

        /**
         * The create call button
         *
         * @property preferredType The call type
         * @property maxDuration Optional max duration of the call in seconds
         * @property recordingType Optional call recording type, default recording type is set to Call.Recording.Type.Never
         * @constructor
         */
        data class Call(
            val preferredType: com.kaleyra.video.conference.Call.PreferredType = com.kaleyra.video.conference.Call.PreferredType.audioVideo(),
            val maxDuration: Long? = 0,
            val recordingType: com.kaleyra.video.conference.Call.Recording.Type? = com.kaleyra.video.conference.Call.Recording.Type.Never
        ) : Button()

        /**
         * Show participants button
         */
        data object Participants : Button()
    }

    /**
     * The chat action sealed class
     */
    sealed class Action {
        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    ShowParticipants,
                    CreateCall(preferredType = Call.PreferredType.audioOnly()),
                    CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            }

            val default by lazy {
                setOf(
                    ShowParticipants,
                    CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            }
        }

        /**
         * The create call action
         *
         * @property preferredType The call PreferredType
         * @property maxDuration Optional max duration of the call in seconds
         * @property recordingType Optional call recording type, default recording type is set to Call.Recording.Type.Never
         * @constructor
         */
        data class CreateCall(
            val preferredType: Call.PreferredType = Call.PreferredType.audioVideo(),
            val maxDuration: Long? = 0,
            val recordingType: Call.Recording.Type? = Call.Recording.Type.Never
        ) : Action()

        /**
         * Show participants action
         */
        data object ShowParticipants : Action()
    }
}