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

package com.kaleyra.video_sdk.call.stream.model.core

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo

/**
 * Stream Ui representation of a Stream on the Ui
 * @property id String stream ui identifier
 * @property userInfo information about the users
 * @property isMine Boolean true if it's local user's stream, false otherwise
 * @property audio AudioUi? optional audio component
 * @property video VideoUi? optional video component
 * @property createdAt Long optional stream creation time
 * @constructor
 */
@Immutable
data class StreamUi(
    val id: String,
    val userInfo: UserInfo? = null,
    val isMine: Boolean = false,
    val audio: AudioUi? = null,
    val video: VideoUi? = null,
    val createdAt: Long = 0L
)
