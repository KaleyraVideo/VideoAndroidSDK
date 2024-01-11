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
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationDelegate
import com.kaleyra.video_common_ui.call.CallNotificationDelegate.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.call.CameraStreamInputsDelegate
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.call.StreamsOpeningDelegate
import com.kaleyra.video_common_ui.call.StreamsVideoViewDelegate
import com.kaleyra.video_common_ui.connectionservice.ProximityService
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

 interface ICallService: CallNotificationDelegate {

    val cameraStreamPublisher: CameraStreamPublisher

    val cameraStreamInputDelegate: CameraStreamInputsDelegate

    val streamOpeningDelegate: StreamsOpeningDelegate

    val streamVideoViewDelegate: StreamsVideoViewDelegate

    val fileShareNotificationDelegate: FileShareNotificationDelegate

    val screenShareOverlayDelegate: ScreenShareOverlayDelegate?

    override fun showNotification(notification: Notification) {
        super.showNotification(notification)
        startForegroundService(notification)
    }

    override fun clearNotification() {
        super.clearNotification()
        stopForegroundService()
    }

    fun setUpCall(context: Context, coroutineScope: CoroutineScope, onEnded: () -> Unit) {
        KaleyraVideo.onCallReady(coroutineScope) { call ->
//            this@CallService.call = call
            cameraStreamPublisher.addCameraStream(call)
            cameraStreamInputDelegate.handleCameraStreamAudio(call)
            cameraStreamInputDelegate.handleCameraStreamVideo(call)
            streamOpeningDelegate.openParticipantsStreams(call)
            streamVideoViewDelegate.setStreamsVideoView(context, call)
            syncCallNotification(call, coroutineScope)
            refreshParticipantDetails(call, coroutineScope)
            stopOnCallEnded(call, coroutineScope, onEnded)
            if (DeviceUtils.isSmartGlass) return@onCallReady
            ProximityService.start()
            fileShareNotificationDelegate.syncFileShareNotification(context, call)
        }
    }

    fun startForegroundService(notification: Notification)

    fun stopForegroundService()

    fun refreshParticipantDetails(call: Call, coroutineScope: CoroutineScope) {
        call.participants
            .onEach { participants ->
                val userIds = participants.list.map { it.userId }.toTypedArray()
                ContactDetailsManager.refreshContactDetails(*userIds)
            }
            .launchIn(coroutineScope)
    }

    fun stopOnCallEnded(call: Call, coroutineScope: CoroutineScope, onEnded: () -> Unit) {
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { onEnded() }
            .launchIn(coroutineScope)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getForegroundServiceType(hasScreenSharingPermission: Boolean): Int {
        val inputsFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        val screenSharingFlag = if (hasScreenSharingPermission) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or inputsFlag or screenSharingFlag
    }

}

/**
 * The CallService
 */
internal class CallService : LifecycleService(), ICallService {

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

    private var foregroundJob: Job? = null

//    private var call: CallUI? = null

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override val cameraStreamPublisher by lazy { CameraStreamPublisher(ioScope) }

    override val cameraStreamInputDelegate by lazy { CameraStreamInputsDelegate(ioScope) }

    override val streamOpeningDelegate by lazy { StreamsOpeningDelegate(ioScope) }

    override val streamVideoViewDelegate by lazy { StreamsVideoViewDelegate(ioScope) }

    override val fileShareNotificationDelegate by lazy { FileShareNotificationDelegate(ioScope) }

    override var screenShareOverlayDelegate: ScreenShareOverlayDelegate? = null

    /**
     * @suppress
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Thread.setDefaultUncaughtExceptionHandler(CallUncaughtExceptionHandler)
        screenShareOverlayDelegate = ScreenShareOverlayDelegate(application, ioScope)
        setUpCall(this, ioScope) { stopSelf() }
        return START_NOT_STICKY
    }

    /**
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        foregroundJob?.cancel()
        ProximityService.stop()
//        call?.end()
        screenShareOverlayDelegate?.dispose()
        ioScope.cancel()
        foregroundJob = null
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

    @Suppress("DEPRECATION")
    override fun stopForegroundService() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
            else stopForeground(true)
        }
    }

}