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

package com.kaleyra.video_sdk.chat.conversation.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

/**
 * Message representation
 * @property id String message identifier
 * @property content String message content
 * @property time String message date
 */
@Stable
sealed interface Message {

    val id: String

    val content: String

    val time: String

    /**
     * Other Message representing message from another participant
     * @property id String message identifier
     * @property content String message content
     * @property time String message date
     * @property userId String message author identifier
     * @constructor
     */
    data class OtherMessage(
        override val id: String,
        override val content: String,
        override val time: String,
        val userId: String
    ) : Message

    /**
     * My message representation of a message sent by logged user
     * @property id String message identifier
     * @property content String message content
     * @property time String message date
     * @property state Flow<State> flow of message states
     * @constructor
     */
    data class MyMessage(
        override val id: String,
        override val content: String,
        override val time: String,
        val state: Flow<State>
    ) : Message

    /**
     * Message State
     */
    @Immutable
    sealed class State {

        /**
         * Created Message State
         */
        data object Created : State()

        /**
         * Received Message State
         */
        data object Received : State()

        /**
         * Sending Message State
         */
        data object Sending : State()

        /**
         * Sent Message State
         */
        data object Sent : State()

        /**
         * Read Message State
         */
        data object Read : State()
    }
}
