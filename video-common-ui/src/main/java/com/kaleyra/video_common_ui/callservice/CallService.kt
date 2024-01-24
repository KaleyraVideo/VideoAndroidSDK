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
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CallNotificationProducer.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn

/**
 * The CallService
 */
internal class CallService: LifecycleService(), CallForegroundService, CallNotificationProducer.Listener {

    companion object {
        fun start() = with(ContextRetainer.context) {
            stop()
            val intent = Intent(this, CallService::class.java)
            startService(intent)
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, CallService::class.java))
        }
    }

    private val callForegroundServiceWorker by lazy { CallForegroundServiceWorker(application, lifecycleScope, this) }

    private var foregroundJob: Job? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        callForegroundServiceWorker.bind(this)
        return START_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        callForegroundServiceWorker.dispose()
        foregroundJob?.cancel()
        foregroundJob = null
    }

    override fun onNewNotification(call: Call, notification: Notification, id: Int) {
        if (foregroundJob != null) return
        // Every time the app goes in foreground, try to promote the service in foreground.
        // The runCatching is needed because the startForeground may fails when the app is in background but
        // the isInForeground flag is still true. This happens because the onStop of the application lifecycle is
        // dispatched 700ms after the last activity's onStop
        foregroundJob = combine(AppLifecycle.isInForeground, flowOf(call).hasScreenSharingInput()) { isInForeground, hasScreenSharingPermission ->
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