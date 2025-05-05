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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kaleyra.video_common_ui.R

/**
 * FileShareNotification
 */
internal class FileShareNotification {

    /**
     * Builder
     *
     * @property context The context used to construct the notification
     * @property channelId The notification channel id
     * @property channelName The notification channel name showed to the users
     * @property contentText The notification content text
     * @property contentTitle The notification title
     * @property contentIntent The pending intent to be executed when the user tap on the notification
     * @property downloadIntent The pending intent to be executed when the user tap on the download button
     * @property notificationPriority The priority associated with this notification
     * @constructor
     */
    data class Builder(
        val context: Context,
        val channelId: String,
        val channelName: String,
        private var contentText: String = "",
        private var contentTitle: String = "",
        private var contentIntent: PendingIntent? = null,
        private var downloadIntent: PendingIntent? = null,
        private var notificationPriority: Int = NotificationCompat.PRIORITY_HIGH
    ) {

        /**
         * The text to be shown inside the notification
         *
         * @param text String
         * @return Builder
         */
        fun contentText(text: String) = apply { this.contentText = text }

        /**
         * Set notification title
         *
         * @param text The title
         * @return Builder
         */
        fun contentTitle(text: String) = apply { this.contentTitle = text }

        /**
         * Set notification priority
         *
         * @param priority Notification priority to be assigned to the builder
         * @return Builder
         */
        fun setPriority(priority: Int) = apply { this.notificationPriority = priority }

        /**
         * The pending intent to be executed when the user tap on the notification
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun contentIntent(pendingIntent: PendingIntent) = apply { this.contentIntent = pendingIntent }

        /**
         * The pending intent to be executed when the user taps the download file button
         *
         * @param pendingIntent PendingIntent
         * @return Builder
         */
        fun downloadIntent(pendingIntent: PendingIntent) = apply { this.downloadIntent = pendingIntent }


        /**
         * Build the chat notification
         *
         * @return Notification
         */
        fun build(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createNotificationChannel(context, channelId, channelName, notificationPriority)

            val builder =
                NotificationCompat.Builder(context.applicationContext, channelId)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setVibrate(arrayOf(0L).toLongArray())
                    .setWhen(System.currentTimeMillis())
                    .setGroupSummary(false)
                    .setSmallIcon(R.drawable.ic_kaleyra_file_share)
                    .setPriority(notificationPriority)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentInfo("")

            contentIntent?.also { builder.setContentIntent(it) }
            downloadIntent?.also {
                val downloadAction = NotificationCompat.Action(0, context.getString(R.string.kaleyra_strings_action_download), it)
                builder.addAction(downloadAction)
            }

            return builder.build()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String,
            notificationPriority: Int
        ) {
            val isLowPriority = notificationPriority == NotificationCompat.PRIORITY_LOW

            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                if (isLowPriority) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}