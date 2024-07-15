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
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.mapper.InputMapper.hasAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasInternalCameraInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * The CallService
 */
class KaleyraCallService : LifecycleService(), CallForegroundService, CallNotificationProducer.Listener {

    companion object {

        var logger: PriorityLogger? = null

        fun start(logger: PriorityLogger? = null) {
            with(ContextRetainer.context) {
                KaleyraCallService.logger = logger
                val intent = Intent(this, KaleyraCallService::class.java)
                startService(intent)
            }
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, KaleyraCallService::class.java))
        }
    }

    private val callForegroundServiceWorker by lazy { CallForegroundServiceWorker(application, lifecycleScope, this) }

    private var notificationJob: Job? = null

    private var call: CallUI? = null

    private val notificationFlow: MutableStateFlow<Pair<Notification, Int>?> = MutableStateFlow(null)

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val call = KaleyraVideo.conference.call.replayCache.firstOrNull()!!
        this@KaleyraCallService.call = call

        callForegroundServiceWorker.bind(this@KaleyraCallService, call)

        notificationJob =
            combine(
                AppLifecycle.isInForeground,
                call.hasInternalCameraInput(),
                call.hasAudioInput(),
                call.hasScreenSharingInput(),
                notificationFlow.filterNotNull()
            ) { isInForeground, hasCameraInput, hasMicInput, hasScreenSharingInput, notificationPair ->
                if (!isInForeground) return@combine
                val notification = notificationPair.first
                val notificationId = notificationPair.second
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(notificationId, notification, getForegroundServiceType(hasCameraInput, hasMicInput, hasScreenSharingInput))
                else startForeground(notificationId, notification)
                this@KaleyraCallService.call ?: return@combine
            }.launchIn(lifecycleScope)

        call.enableAudioRouting(
            logger = logger,
            coroutineScope = lifecycleScope,
            isLink = call.isLink
        )

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
        lifecycleScope.launch {
            this@KaleyraCallService.notificationFlow.emit(Pair(notification, id))
        }
    }

    @Suppress("DEPRECATION")
    override fun onClearNotification(id: Int) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
            else stopForeground(true)
        }
    }
}
