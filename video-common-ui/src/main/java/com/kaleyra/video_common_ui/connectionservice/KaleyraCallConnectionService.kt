package com.kaleyra.video_common_ui.connectionservice

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi
import com.bandyer.android_audiosession.sounds.CallSound
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.callservice.CallForegroundService
import com.kaleyra.video_common_ui.callservice.CallForegroundServiceWorker
import com.kaleyra.video_common_ui.connectionservice.ContactsController.createOrUpdateConnectionServiceContact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.hasAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasInternalCameraInput
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

@RequiresApi(Build.VERSION_CODES.O)
class KaleyraCallConnectionService : ConnectionService(), CallForegroundService, CallNotificationProducer.Listener, KaleyraCallConnection.Listener {

    companion object {

        var logger: PriorityLogger? = null

        private var connection: MutableStateFlow<KaleyraCallConnection?> = MutableStateFlow(null)

        private const val CONNECTION_TIMEOUT_MS = 1500L

        suspend fun answer(): Boolean = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            connection.filterNotNull().firstOrNull()?.onAnswer()
            true
        } ?: false

        suspend fun reject(): Boolean = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            connection.filterNotNull().firstOrNull()?.onReject()
            true
        } ?: false

        suspend fun hangUp(): Boolean = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            connection.filterNotNull().firstOrNull()?.onDisconnect()
            true
        } ?: false
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val callForegroundServiceWorker by lazy { CallForegroundServiceWorker(application, coroutineScope, this) }

    private var notificationJob: Job? = null

    private var call: CallUI? = null

    private val notificationFlow: MutableStateFlow<Pair<Notification, Int>?> = MutableStateFlow(null)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        callForegroundServiceWorker.dispose()
        coroutineScope.cancel()
        notificationJob?.cancel()
        call?.disableAudioRouting()
        notificationJob = null
        call = null
        connection.value?.also { conn ->
            application.unregisterActivityLifecycleCallbacks(conn)
            conn.address?.also { ContactsController.deleteConnectionServiceContact(this, it) }
            connection.value = null
        }
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val call = KaleyraVideo.conference.call.replayCache.first()
        val connection = createConnection(request, call).apply {
            setDialing()
        }
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val call = KaleyraVideo.conference.call.replayCache.first()
        val connection = createConnection(request, call).apply {
            setRinging()
        }
        return connection
    }

    private fun createConnection(request: ConnectionRequest, call: CallUI): KaleyraCallConnection {
        return KaleyraCallConnection.create(request = request, call = call).apply {
            connection.value = this
            addListener(this@KaleyraCallConnectionService)
            configureService(call, this)
            application.registerActivityLifecycleCallbacks(this)
        }
    }

    override fun onShowIncomingCallUi(connection: KaleyraCallConnection) {
        val call = KaleyraVideo.conference.call.replayCache.first()
        createOrUpdateConnectionContact(connection, call)
    }

    private fun configureService(call: CallUI, connection: KaleyraCallConnection) {
        this.call = call

        notificationJob = combine(
            call.hasInternalCameraInput(),
            call.hasAudioInput(),
            call.hasScreenSharingInput(),
            notificationFlow.filterNotNull()
        ) { hasCameraInput, hasMicInput, hasScreenSharingInput, notificationPair ->
            val notification = notificationPair.first
            val notificationId = notificationPair.second
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(notificationId, notification, getForegroundServiceType(hasCameraInput, hasMicInput, hasScreenSharingInput))
            else startForeground(notificationId, notification)
        }.launchIn(MainScope())
        // should be launched on main scope otherwise it will receive that the screen input is active
        // when it has been stopped causing app crash on android 14

        call.enableAudioRouting(
            connection = connection,
            currentDevice = connection.currentAudioDevice,
            availableDevices = connection.availableAudioDevices,
            logger = logger,
            coroutineScope
        )
        callForegroundServiceWorker.bind(this, call, connection)
    }

    private fun createOrUpdateConnectionContact(connection: KaleyraCallConnection, call: Call) {
        coroutineScope.launch {
            val participants = call.participants.value
            val callee = if (participants.others.size > 1) {
                resources.getString(R.string.kaleyra_notification_incoming_group_call)
            } else {
                val other = participants.others.firstOrNull()
                other?.combinedDisplayName?.filterNotNull()?.firstOrNull() ?: other?.userId ?: ""
            }
            createOrUpdateConnectionServiceContact(
                this@KaleyraCallConnectionService,
                connection.address,
                callee
            )
        }
    }

    override fun onConnectionStateChange(connection: KaleyraCallConnection) {
        if (connection.state != Connection.STATE_DISCONNECTED) return
        connection.removeListener(this@KaleyraCallConnectionService)
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

    override fun onNewNotification(call: Call, notification: Notification, id: Int) {
        coroutineScope.launch {
            this@KaleyraCallConnectionService.notificationFlow.emit(Pair(notification, id))
        }
    }


    override fun onSilence() {
        CallSound.stop(instantly = true)
    }

    override fun onClearNotification(id: Int) = stopForegroundService()

    private fun stopForegroundService() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }

}