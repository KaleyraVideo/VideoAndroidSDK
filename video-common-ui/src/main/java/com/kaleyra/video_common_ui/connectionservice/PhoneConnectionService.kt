package com.kaleyra.video_common_ui.connectionservice

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUncaughtExceptionHandler
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationDelegate
import com.kaleyra.video_common_ui.call.CameraStreamInputsDelegate
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.call.StreamsOpeningDelegate
import com.kaleyra.video_common_ui.call.StreamsVideoViewDelegate
import com.kaleyra.video_common_ui.connectionservice.ContactsController.createOrUpdateConnectionServiceContact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.proximity.CallProximityDelegate
import com.kaleyra.video_common_ui.proximity.ProximityCallActivity
import com.kaleyra.video_common_ui.texttospeech.TextToSpeechNotifier
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.DeviceUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

@RequiresApi(Build.VERSION_CODES.O)
class PhoneConnectionService : ConnectionService(), CallConnection.IncomingCallListener,
    CallConnection.ConnectionStateListener,
    CameraStreamPublisher, CameraStreamInputsDelegate,
    StreamsOpeningDelegate, StreamsVideoViewDelegate, CallNotificationDelegate,
    FileShareNotificationDelegate, ScreenShareOverlayDelegate,
    Application.ActivityLifecycleCallbacks {

    companion object {
        private var connection: CallConnection? = null

        fun answer() {
            connection?.onAnswer()
        }

        fun reject() {
            connection?.onReject()
        }

        fun end() {
            connection?.onDisconnect()
        }
    }

    private var foregroundJob: Job? = null

    private var proximityDelegate: CallProximityDelegate<LifecycleService>? = null

    private var proximityCallActivity: ProximityCallActivity? = null

    private var call: CallUI? = null

    private var onCallNewActivity: ((Context) -> Unit)? = null

    private var recordingTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var awaitingParticipantsTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var mutedTextToSpeechNotifier: TextToSpeechNotifier? = null

    private val mainScope = MainScope()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread.setDefaultUncaughtExceptionHandler(CallUncaughtExceptionHandler)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        application.unregisterActivityLifecycleCallbacks(this)
        recordingTextToSpeechNotifier?.dispose()
        mutedTextToSpeechNotifier?.dispose()
        awaitingParticipantsTextToSpeechNotifier?.dispose()
        proximityDelegate?.destroy()
        foregroundJob?.cancel()
        mainScope.cancel()
        call?.end()
        awaitingParticipantsTextToSpeechNotifier = null
        recordingTextToSpeechNotifier = null
        mutedTextToSpeechNotifier = null
        proximityCallActivity = null
        proximityDelegate = null
        onCallNewActivity = null
        foregroundJob = null
        call = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity::class.java != call?.activityClazz) return
        onCallNewActivity?.invoke(activity)
        if (activity !is ProximityCallActivity) return
        proximityCallActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (activity::class.java != call?.activityClazz || activity !is ProximityCallActivity) return
        proximityCallActivity = null
    }

    /**
     * @suppress
     */
    override fun showNotification(notification: Notification) {
        super.showNotification(notification)
        startForeground(notification)
    }

    /**
     * @suppress
     */
    @Suppress("DEPRECATION")
    override fun clearNotification() {
        super.clearNotification()
        stopForegroundService()
    }

    /**
     * Set up the call streams and notifications
     *
     */
    private fun setUpCall(connection: CallConnection) {
        KaleyraVideo.onCallReady(mainScope) { call ->
            application.registerActivityLifecycleCallbacks(this)
            this@PhoneConnectionService.call = call

            addCameraStream(call, mainScope)
            handleCameraStreamAudio(call, mainScope)
            handleCameraStreamVideo(call, mainScope)
            openParticipantsStreams(call.participants, mainScope)
            setStreamsVideoView(this@PhoneConnectionService, call.participants, mainScope)
            syncCallNotification(call, call.activityClazz, mainScope)

            call.participants
                .onEach { participants ->
                    val userIds = participants.list.map { it.userId }.toTypedArray()
                    ContactDetailsManager.refreshContactDetails(*userIds)
                }
                .launchIn(mainScope)

            var screenShareScope: CoroutineScope? = null
            if (!DeviceUtils.isSmartGlass) {
                handleProximity(call)
                syncFileShareNotification(this, call, call.activityClazz, mainScope)
                onCallNewActivity = { activityContext ->
                    screenShareScope?.cancel()
                    screenShareScope = newChildScope(
                        coroutineScope = mainScope,
                        dispatcher = Dispatchers.Main
                    )
                    syncScreenShareOverlay(activityContext, call, screenShareScope!!)
                }
            }

            call.state
                .takeWhile { it !is Call.State.Disconnected.Ended }
                .onCompletion {
                    stopSelf()
                    screenShareScope = null
                }
                .launchIn(mainScope)

            if (call.shouldShowAsActivity()) {
                call.showOnAppResumed(mainScope)
            }

            if (connection.isIncoming) {
                mainScope.launch {
                    val participants = call.participants.value
                    val callee = participants.others.map {
                        it.combinedDisplayName.filterNotNull().firstOrNull() ?: Uri.EMPTY
                    }.joinToString()
                    createOrUpdateConnectionServiceContact(
                        this@PhoneConnectionService,
                        connection.address,
                        callee
                    )
                }
            }
        }
    }

    private fun handleProximity(call: CallUI) {
        combine(
            call.state,
            call.participants
        ) { state, participants -> state is Call.State.Disconnected && participants.let { it.creator() != it.me && it.creator() != null } }
            .onEach {
                // if the call is incoming, don't immediately bind the proximity
                if (it) return@onEach
//                proximityDelegate = CallProximityDelegate(
//                    lifecycleContext = this@PhoneConnectionService,
//                    call = call,
//                    disableProximity = { proximityCallActivity?.disableProximity ?: false },
//                    disableWindowTouch = { disableWindowTouch ->
//                        if (disableWindowTouch) proximityCallActivity?.disableWindowTouch()
//                        else proximityCallActivity?.enableWindowTouch()
//                    }
//                ).apply { bind() }
//                recordingTextToSpeechNotifier = CallRecordingTextToSpeechNotifier(
//                    call,
//                    proximityDelegate!!.sensor!!
//                ).apply { start(mainScope) }
//                mutedTextToSpeechNotifier = CallParticipantMutedTextToSpeechNotifier(
//                    call,
//                    proximityDelegate!!.sensor!!
//                ).apply { start(mainScope) }
//                awaitingParticipantsTextToSpeechNotifier = AwaitingParticipantsTextToSpeechNotifier(
//                    call,
//                    proximityDelegate!!.sensor!!
//                ).apply { start(mainScope) }
            }
            .takeWhile { it }
            .launchIn(mainScope)
    }

    private fun newChildScope(coroutineScope: CoroutineScope, dispatcher: CoroutineDispatcher) =
        CoroutineScope(SupervisorJob(coroutineScope.coroutineContext[Job])) + dispatcher

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val connection = createConnection(request, isIncoming = false).apply {
            setDialing()
        }
        setUpCall(connection)
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection = createConnection(request, isIncoming = true)

    private fun createConnection(
        request: ConnectionRequest,
        isIncoming: Boolean
    ): CallConnection {
        return CallConnection.create(request = request, isIncoming).apply {
            connection = this
            addIncomingCallListener(this@PhoneConnectionService)
            addConnectionStateListener(this@PhoneConnectionService)
//            addAudioOutputListener(this@PhoneConnectionService)
        }
    }

//    override fun onSilent() {
//
//    }

    override fun onShowIncomingCallUi(connection: CallConnection) {
        connection.setRinging()
        setUpCall(connection)
    }


    override fun onConnectionStateChange(connection: CallConnection) {
        if (connection.state != Connection.STATE_DISCONNECTED) return
        connection.removeIncomingCallListener(this@PhoneConnectionService)
        connection.removeConnectionStateListener(this@PhoneConnectionService)
        stopForegroundService()
        stopSelf()
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
        stopForegroundService()
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        stopForegroundService()
    }

    override fun onConnectionServiceFocusLost() {
        super.onConnectionServiceFocusLost()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        connectionServiceFocusReleased()
    }

    private fun startForeground(notification: Notification) {
        flowOf(call!!)
            .hasScreenSharingInput()
            .onEach { hasScreenSharingPermission ->
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(CallNotificationDelegate.CALL_NOTIFICATION_ID, notification, getForegroundServiceType(hasScreenSharingPermission))
                    else startForeground(CallNotificationDelegate.CALL_NOTIFICATION_ID, notification)
                }
            }
            .launchIn(mainScope)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getForegroundServiceType(hasScreenSharingPermission: Boolean): Int {
        val inputsFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        val screenSharingFlag = if (hasScreenSharingPermission) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or inputsFlag or screenSharingFlag
    }

    private fun stopForegroundService() = runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
}