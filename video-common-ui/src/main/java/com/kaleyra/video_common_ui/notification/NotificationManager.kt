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

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationManager
import com.kaleyra.video_common_ui.notification.signature.SignatureNotificationManager
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.MainScope

/**
 * NotificationManager
 */
internal object NotificationManager : CallNotificationManager, ChatNotificationManager, FileShareNotificationManager, SignatureNotificationManager {

    private val notificationManager by lazy { ContextRetainer.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    init {
        cancelChatNotificationOnShow(MainScope())
    }

    /**
     * Notify the system to add a notification
     *
     * @param notificationId Int
     * @param notification Notification
     */
    fun notify(notificationId: Int, notification: Notification) = notificationManager.notify(notificationId, notification)

    /**
     * Cancel a notification
     *
     * @param notificationId Int
     */
    fun cancel(notificationId: Int) = notificationManager.cancel(notificationId)

    /**
     * Cancel all notifications
     */
    fun cancelAll() = notificationManager.cancelAll()
}
