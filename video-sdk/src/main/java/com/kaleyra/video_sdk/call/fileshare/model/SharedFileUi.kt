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

package com.kaleyra.video_sdk.call.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

/**
 * Shared File Ui representation
 * @property id String
 * @property name String
 * @property uri ImmutableUri
 * @property size Long?
 * @property sender String
 * @property time Long
 * @property state State
 * @property isMine Boolean
 * @constructor
 */
@Immutable
data class SharedFileUi(
    val id: String,
    val name: String,
    val uri: ImmutableUri,
    val size: Long?,
    val sender: String,
    val time: Long,
    val state: State,
    val isMine: Boolean
) {

    /**
     * Shared File Ui State
     */
    @Immutable
    sealed class State {

        /**
         * Available Shared File Ui State
         */
        data object Available : State()

        /**
         * Pending Shared File Ui State
         */
        data object Pending : State()

        /**
         * In Progress Shared File Ui State
         * @property progress Float progress value
         * @constructor
         */
        data class InProgress(val progress: Float) : State()

        /**
         * Success Shared File Ui State
         * @property uri Uri uri of the shared file
         * @constructor
         */
        data class Success(val uri: ImmutableUri) : State()

        /**
         * Error Shared File Ui State
         */
        data object Error : State()

        /**
         * Cancelled Shared File Ui State
         */
        data object Cancelled : State()
    }
}
