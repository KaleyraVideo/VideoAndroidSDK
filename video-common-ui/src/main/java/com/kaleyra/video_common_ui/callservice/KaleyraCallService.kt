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
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn

/**
 * The CallService
 */
class KaleyraCallService : LifecycleService(), CallForegroundService, CallNotificationProducer.Listener {

    companion object {

        var logger: PriorityLogger? = null

        fun start(logger: PriorityLogger? = null) {
            with(ContextRetainer.context) {
                KaleyraCallService.logger = Companion.logger
                val intent = Intent(this, KaleyraCallService::class.java)
                startService(intent)
            }
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, KaleyraCallService::class.java))
        }
    }

    private val callForegroundServiceWorker by lazy { CallForegroundServiceWorker(application, lifecycleScope, this) }

    private var foregroundJob: Job? = null

    private var call: Call? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val call = KaleyraVideo.conference.call.replayCache.first().apply {
            call = this
        }
        callForegroundServiceWorker.bind(this, call)
        call.enableAudioRouting(
            withCallSounds = true,
            logger = logger,
            coroutineScope = lifecycleScope,
            isLink = call.isLink
        )
//        if (KaleyraVideo.conference.withUI && call.shouldShowAsActivity()) {
//            call.showOnAppResumed(lifecycleScope)
//        }
        return START_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        callForegroundServiceWorker.dispose()
        call?.disableAudioRouting()
        foregroundJob?.cancel()
        foregroundJob = null
        logger = null
        call = null
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