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

package com.kaleyra.video_common_ui.callservice

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.Synchronization
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CallNotificationProducer.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * The CallService
 */
class KaleyraCallService : LifecycleService(), CallForegroundService, CallNotificationProducer.Listener {

    companion object {

        internal const val REQUEST_CONNECT_USER_ID = "REQUEST_CONNECT_USER_ID"

        var logger: PriorityLogger? = null

        fun start(logger: PriorityLogger? = null) {
            with(ContextRetainer.context) {
                KaleyraCallService.logger = logger
                val intent = Intent(this, KaleyraCallService::class.java)
                intent.putExtra(REQUEST_CONNECT_USER_ID, KaleyraVideo.connectedUser.value?.userId)
                startService(intent)
            }
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, KaleyraCallService::class.java))
        }
    }

    private val callForegroundServiceWorker by lazy { CallForegroundServiceWorker(application, lifecycleScope, this) }

    private var notificationJob: Job? = null

    private var call: Call? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.extras ?: stopSelf().also { return START_NOT_STICKY  }

        Log.e("SERVICE", "onstartcommand")

        MainScope().launch {
            if (!KaleyraVideo.isConfigured) {
                val requestConnectUserId = intent!!.extras!!.getString(REQUEST_CONNECT_USER_ID) ?: run {
                    return@launch
                }
                val hasConfigured = requestConfiguration()
                if (!hasConfigured) {
                    stopSelf()
                    if (!AppLifecycle.isInForeground.value) KaleyraVideo.disconnect()
                    Log.e("SERVICE", "has not configured")
                    com.kaleyra.video_common_ui.notification.NotificationManager.cancel(CALL_NOTIFICATION_ID)
                    return@launch
                }
                val hasConnected = requestConnect(requestConnectUserId)
                if (!hasConnected) {
                    stopSelf()
                    if (!AppLifecycle.isInForeground.value) KaleyraVideo.disconnect()
                    Log.e("SERVICE", "has not connected")
                    com.kaleyra.video_common_ui.notification.NotificationManager.cancel(CALL_NOTIFICATION_ID)
                    return@launch
                }
                KaleyraVideo.synchronization.first { it is Synchronization.Active }
                if (KaleyraVideo.conference.call.replayCache.firstOrNull() == null) {
                    stopSelf()
                    if (!AppLifecycle.isInForeground.value) KaleyraVideo.disconnect()
                    com.kaleyra.video_common_ui.notification.NotificationManager.cancel(CALL_NOTIFICATION_ID)
                    return@launch
                }
                Log.e("SERVICE", "has synchronized")
            }
            val call = KaleyraVideo.conference.call.replayCache.first().apply {
                call = this
            }
            callForegroundServiceWorker.bind(this@KaleyraCallService, call)
            call.enableAudioRouting(
                logger = logger,
                coroutineScope = lifecycleScope,
                isLink = call.isLink
            )
        }
        return START_REDELIVER_INTENT
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        callForegroundServiceWorker.dispose()
        call?.disableAudioRouting()
        notificationJob?.cancel()
        notificationJob = null
        logger = null
        call = null
    }

    override fun onNewNotification(call: Call, notification: Notification, id: Int) {
        // Every time the app goes in foreground, try to promote the service in foreground.
        // The runCatching is needed because the startForeground may fails when the app is in background but
        // the isInForeground flag is still true. This happens because the onStop of the application lifecycle is
        // dispatched 700ms after the last activity's onStop
        notificationJob?.cancel()
        notificationJob = combine(AppLifecycle.isInForeground, flowOf(call).hasScreenSharingInput()) { isInForeground, hasScreenSharingPermission ->
            if (!isInForeground) return@combine
            kotlin.runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(id, notification, getForegroundServiceType(hasScreenSharingPermission))
                else startForeground(id, notification)
            }
        }.launchIn(lifecycleScope)
    }

    @Suppress("DEPRECATION")
    override fun onClearNotification(id: Int) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
            else stopForeground(true)
        }
    }
}
