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

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CameraStreamManager
import com.kaleyra.video_common_ui.call.ParticipantManager
import com.kaleyra.video_common_ui.call.StreamsManager
import com.kaleyra.video_common_ui.connectionservice.ProximityService
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

interface NewCallForegroundService {

    fun startForegroundService(notification: Notification, id: Int)

    fun stopForegroundService()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getForegroundServiceType(hasScreenSharingInput: Boolean): Int {
        val inputsFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        val screenSharingFlag = if (hasScreenSharingInput) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or inputsFlag or screenSharingFlag
    }
}

/**
 * The CallService
 */
internal class NewCallService: LifecycleService(), NewCallForegroundService, CallNotificationProducer.Listener  {

    companion object {
        fun start() = with(ContextRetainer.context) {
            stop()
            val intent = Intent(this, NewCallService::class.java)
            startService(intent)
        }

        fun stop() = with(ContextRetainer.context) {
            stopService(Intent(this, NewCallService::class.java))
        }
    }

    private val callNotificationProducer by lazy { CallNotificationProducer(lifecycleScope) }

    private val fileShareNotificationProducer by lazy { FileShareNotificationProducer(lifecycleScope) }

    private val cameraStreamManager by lazy { CameraStreamManager(lifecycleScope) }

    private val streamsManager by lazy { StreamsManager(lifecycleScope) }

    private val participantManager by lazy { ParticipantManager(lifecycleScope) }

    private var foregroundJob: Job? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Thread.setDefaultUncaughtExceptionHandler(CallUncaughtExceptionHandler)
        lifecycleScope.launch {
            val call = KaleyraVideo.conference.call.first()
            cameraStreamManager.bind(call)
            streamsManager.bind(call)
            participantManager.bind(call)
            callNotificationProducer.bind(call)
            callNotificationProducer.listener = this@NewCallService

            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion { stopSelf() }
                .launchIn(lifecycleScope)

            if (DeviceUtils.isSmartGlass) return@launch
            ProximityService.start()
            fileShareNotificationProducer.bind(call)

        }
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        foregroundJob?.cancel()
        foregroundJob = null
        ProximityService.stop()
        cameraStreamManager.stop()
        streamsManager.stop()
        participantManager.stop()
        callNotificationProducer.stop()
        fileShareNotificationProducer.stop()
    }

    override fun onNewNotification(notification: Notification, id: Int) = startForegroundService(notification, id)

    override fun onClearNotification(id: Int) = stopForegroundService()

    override fun startForegroundService(notification: Notification, id: Int) {
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(id, notification, getForegroundServiceType(hasScreenSharingInput = false))
            else startForeground(id, notification)
        }
    }

    @Suppress("DEPRECATION")
    override fun stopForegroundService() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
            else stopForeground(true)
        }
    }
}

internal class CallService : LifecycleService() {

//    private var call: CallUI? = null

    override fun onDestroy() {
        super.onDestroy()
//        call?.end()
//        call = null
    }

    override fun startForegroundService(notification: Notification) {
        if (foregroundJob != null) return
        // Every time the app goes in foreground, try to promote the service in foreground.
        // The runCatching is needed because the startForeground may fails when the app is in background but
        // the isInForeground flag is still true. This happens because the onStop of the application lifecycle is
        // dispatched 700ms after the last activity's onStop
        KaleyraVideo.onCallReady(lifecycleScope) { call ->
            foregroundJob = combine(AppLifecycle.isInForeground, flowOf(call).hasScreenSharingInput()) { isInForeground, hasScreenSharingPermission ->
                if (!isInForeground) return@combine
                kotlin.runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(CALL_NOTIFICATION_ID, notification, getForegroundServiceType(hasScreenSharingPermission))
                    else startForeground(CALL_NOTIFICATION_ID, notification)
                }
            }.launchIn(lifecycleScope)
        }
    }

}