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

package com.kaleyra.video_common_ui.notification

import android.net.Uri

/**
 * The chat notification message
 *
 * @property userId The logged user id
 * @property displayName The user display name
 * @property displayImage The logger user avatar
 * @property text The content text
 * @property timestamp The message timestamp
 * @constructor
 */
internal data class ChatNotificationMessage(
    val userId: String,
    val displayName: String,
    val displayImage: Uri,
    val text: String,
    val timestamp: Long
)