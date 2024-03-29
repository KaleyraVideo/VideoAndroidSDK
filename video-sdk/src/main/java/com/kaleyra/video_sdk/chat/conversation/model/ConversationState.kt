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
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Conversation State
 * @property conversationItems ImmutableList<ConversationItem>? list of conversation items
 * @property isFetching Boolean flag indicating if there is a fetch in progress, true if fetching, false otherwise
 * @property unreadMessagesCount Int unread messages count
 * @constructor
 */
@Immutable
data class ConversationState(
    val conversationItems: ImmutableList<ConversationItem>? = null,
    val isFetching: Boolean = false,
    val unreadMessagesCount: Int = 0
)
