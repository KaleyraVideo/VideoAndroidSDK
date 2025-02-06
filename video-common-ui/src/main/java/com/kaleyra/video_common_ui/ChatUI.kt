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

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Conference
import com.kaleyra.video_common_ui.utils.extensions.CallTypeExtensions.toCallType
import com.kaleyra.video_common_ui.utils.extensions.mapToSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.parcelize.Parcelize

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
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(Action.default),
    private val chatActivityClazz: Class<*>,
    private val chatCustomNotificationActivityClazz: Class<*>? = null
) : Chat by chat {

    private var chatScope = CoroutineScope(Dispatchers.IO)

    /**
     * @suppress
     */
    override val messages: SharedFlow<MessagesUI> = chat.messages.mapToSharedFlow(chatScope) { MessagesUI(it, chatActivityClazz, chatCustomNotificationActivityClazz) }

    /**
     * The chat action sealed class
     */
    @Keep
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
                    CreateCall(callType = Call.Type.audioOnly()),
                    CreateCall(callType = Call.Type.audioUpgradable()),
                    CreateCall(callType = Call.Type.audioVideo())
                )
            }

            val default by lazy {
                setOf(
                    ShowParticipants,
                    CreateCall(callType = Call.Type.audioUpgradable()),
                    CreateCall(callType = Call.Type.audioVideo())
                )
            }
        }

        /**
         * The create call action
         *
         * @property callType The call type
         * @property maxDuration Optional max duration of the call in seconds
         * @property recordingType Optional call recording type, default recording type is set to Call.Recording.Type.Never
         * @constructor
         */
        class CreateCall(
            val callType: Call.Type = Call.Type.audioVideo(),
            val maxDuration: Long? = 0,
            val recordingType: Call.Recording.Type? = Call.Recording.Type.Never
            ) : Action() {

                @Deprecated(
                    message = "This constructor has been deprecated and it will be removed in a further release.",
                    replaceWith = ReplaceWith("CreateCall(callType, maxDuration, recordingType)")
                )
                constructor(preferredType: Call.PreferredType = Call.PreferredType.audioVideo(),
                            maxDuration: Long? = 0,
                            recordingType: Call.Recording.Type? = Call.Recording.Type.Never
                ) : this(preferredType.toCallType(), maxDuration, recordingType)
            }

        /**
         * Show participants action
         */
        data object ShowParticipants : Action()
    }
}