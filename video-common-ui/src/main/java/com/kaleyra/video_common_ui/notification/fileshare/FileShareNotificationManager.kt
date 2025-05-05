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

package com.kaleyra.video_common_ui.notification.fileshare

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions

object FileShareNotificationExtra {
    const val NOTIFICATION_ACTION_EXTRA = "notificationAction"
}

internal interface FileShareNotificationManager {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "com.kaleyra.video_common_ui.fileshare_notification_channel_default"
        private const val DEFAULT_CHANNEL_ID_LOW_PRIORITY = "com.kaleyra.video_common_ui.fileshare_notification_channel_low_priority"

        private const val CONTENT_REQUEST_CODE = 121
        private const val DOWNLOAD_REQUEST_CODE = 232
    }

    fun buildIncomingFileNotification(
        context: Context,
        username: String,
        downloadId: String,
        notificationPriority: Int,
        activityClazz: Class<*>
    ): Notification {
        val resources = context.resources

        val isLowPriority = notificationPriority == NotificationCompat.PRIORITY_LOW
        val channelName = resources.getString(R.string.kaleyra_strings_system_android_file_share_notification_channel)
        val builder = FileShareNotification
            .Builder(
                context = context,
                channelId = if (isLowPriority) DEFAULT_CHANNEL_ID_LOW_PRIORITY else  DEFAULT_CHANNEL_ID,
                channelName = if (isLowPriority) "$channelName low priority" else channelName
            )
            .contentTitle(resources.getString(
                    R.string.kaleyra_string_info_user_file_uploaded,
                    username
                ))
            .contentText(resources.getString(R.string.kaleyra_strings_info_download_file_title))
            .setPriority(notificationPriority)
            .contentIntent(downloadContentPendingIntent(context, activityClazz, downloadId))
            .downloadIntent(downloadPendingIntent(context, activityClazz, downloadId))

        return builder.build()
    }

    private fun downloadContentPendingIntent(context: Context, activityClazz: Class<*>, downloadId: String) =
        createCallActivityPendingIntent(context, CONTENT_REQUEST_CODE + downloadId.hashCode(), activityClazz, null)

    private fun downloadPendingIntent(
        context: Context,
        activityClazz: Class<*>,
        downloadId: String
    ) = createCallActivityPendingIntent(
            context,
            DOWNLOAD_REQUEST_CODE + downloadId.hashCode(),
            activityClazz,
            Intent().putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        )

    private fun <T> createCallActivityPendingIntent(
        context: Context,
        requestCode: Int,
        activityClazz: Class<T>,
        intentExtras: Intent?
    ): PendingIntent {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, activityClazz).apply {
            // Setting main action and category launcher allows to open activity
            // from notification if there is already an instance, instead of a creating a new one
            this.action = Intent.ACTION_MAIN
            this.addCategory(Intent.CATEGORY_LAUNCHER)
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.putExtra(FileShareNotificationExtra.NOTIFICATION_ACTION_EXTRA, FileShareNotificationActionReceiver.ACTION_DOWNLOAD)
            intentExtras?.let { this.putExtras(it) }
        }
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntentExtensions.updateFlags
        )
    }
}

