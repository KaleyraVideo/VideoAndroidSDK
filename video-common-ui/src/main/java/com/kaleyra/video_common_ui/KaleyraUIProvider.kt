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
import android.content.Intent
import com.kaleyra.video_common_ui.utils.DeviceUtils.isSmartGlass
import com.kaleyra.video_utils.ContextRetainer

/**
 * Provider for the display operation of conversation and conference flows
 */
object KaleyraUIProvider {

    const val ENABLE_TILT_EXTRA = "enableTilt"

    /**
     * Starts requested call activity
     * @param activityClazz Class<*> the requested call activity's Class
     */
    fun startCallActivity(activityClazz: Class<*>) =
        with(ContextRetainer.context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(ENABLE_TILT_EXTRA, isSmartGlass)
            }
            startActivity(intent)
        }

    /**
     * Starts requested chat activity
     * @param activityClazz Class<*> the requested chat activity's Class
     */
    fun startChatActivity(context: Context, activityClazz: Class<*>, userIds: List<String>, chatId: String? = null) = with(context) {
        val intent = Intent(this, activityClazz).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(ENABLE_TILT_EXTRA, isSmartGlass)
            putExtra("chatId", chatId)
            putExtra("userIds", userIds.toTypedArray())
        }
        startActivity(intent)
    }
}