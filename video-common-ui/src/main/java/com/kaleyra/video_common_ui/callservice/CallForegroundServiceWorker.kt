package com.kaleyra.video_common_ui.callservice

import android.app.Application
import android.app.Service
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUncaughtExceptionHandler
import com.kaleyra.video_common_ui.StreamsAudioManager
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CameraStreamManager
import com.kaleyra.video_common_ui.call.ParticipantManager
import com.kaleyra.video_common_ui.call.StreamsManager
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnection
import com.kaleyra.video_common_ui.connectionservice.ProximityService
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer
import com.kaleyra.video_common_ui.notification.signature.SignatureNotificationProducer
import com.kaleyra.video_common_ui.utils.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

// TODO revise naming for managers
internal class CallForegroundServiceWorker(
    private val application: Application,
    private val coroutineScope: CoroutineScope,
    private val callNotificationListener: CallNotificationProducer.Listener
) {

    private val callNotificationProducer by lazy { CallNotificationProducer(coroutineScope) }

    private val fileShareNotificationProducer by lazy { FileShareNotificationProducer(coroutineScope) }

    private val signatureNotificationProducer by lazy { SignatureNotificationProducer(coroutineScope) }

    private val cameraStreamManager by lazy { CameraStreamManager(coroutineScope) }

    private val streamsManager by lazy { StreamsManager(coroutineScope) }

    private val participantManager by lazy { ParticipantManager(coroutineScope) }

    private val streamsAudioManager by lazy { StreamsAudioManager(coroutineScope) }

    private var call: CallUI? = null
    private var connection: KaleyraCallConnection? = null

    fun bind(service: Service, call: CallUI) {
        this.call = call
       internalBind(service)
    }

    fun bind(service: Service, call: CallUI, connection: KaleyraCallConnection) {
        this.call = call
        this.connection = connection
        internalBind(service)
    }

    private fun internalBind(service: Service) {
        Thread.setDefaultUncaughtExceptionHandler(CallUncaughtExceptionHandler)
        cameraStreamManager.bind(call!!)
        streamsManager.bind(call!!)
        participantManager.bind(call!!)
        streamsAudioManager.bind(call!!)
        callNotificationProducer.bind(call!!, service)
        callNotificationProducer.listener = callNotificationListener

        call!!.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { service.stopSelf() }
            .launchIn(coroutineScope)

        if (!DeviceUtils.isSmartGlass) {
            ProximityService.start(connection)
            fileShareNotificationProducer.bind(call!!)
            signatureNotificationProducer.bind(call!!)
        }
    }

    fun dispose() {
        call?.end()
        call = null
        cameraStreamManager.stop()
        streamsManager.stop()
        participantManager.stop()
        streamsAudioManager.stop()
        callNotificationProducer.stop()

        if (DeviceUtils.isSmartGlass) return
        ProximityService.stop()
        fileShareNotificationProducer.stop()
        signatureNotificationProducer.stop()
    }
}