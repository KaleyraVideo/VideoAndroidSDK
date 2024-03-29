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

package com.kaleyra.video_sdk.common.usermessages.model

import java.util.UUID

/**
 * Recording message
 * @property id String recording message identifier
 * @constructor
 */
sealed class RecordingMessage(override val id: String) : UserMessage {

    /**
     * Recording Started
     */
    data object Started : RecordingMessage(UUID.randomUUID().toString())

    /**
     * Recording Stopped
     */
    data object Stopped : RecordingMessage(UUID.randomUUID().toString())

    /**
     * Recording Failed
     */
    data object Failed : RecordingMessage(UUID.randomUUID().toString())
}
