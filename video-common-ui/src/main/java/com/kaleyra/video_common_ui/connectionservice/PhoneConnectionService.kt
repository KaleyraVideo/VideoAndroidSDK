package com.kaleyra.video_common_ui.connectionservice

import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallForegroundService
import com.kaleyra.video_common_ui.CallForegroundServiceWorker
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUncaughtExceptionHandler
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CallNotificationProducer.Companion.CALL_NOTIFICATION_ID
import com.kaleyra.video_common_ui.call.CameraStreamManager
import com.kaleyra.video_common_ui.call.ParticipantManager
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.call.StreamsManager
import com.kaleyra.video_common_ui.connectionservice.ContactsController.createOrUpdateConnectionServiceContact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class PhoneConnectionService : ConnectionService(), CallForegroundService, CallNotificationProducer.Listener, CallConnection.IncomingCallListener, CallConnection.ConnectionStateListener {

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

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val callForegroundServiceWorker = CallForegroundServiceWorker(coroutineScope, this)

    private var foregroundJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        callForegroundServiceWorker.dispose()
        coroutineScope.cancel()
        foregroundJob?.cancel()
        foregroundJob = null
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val connection = createConnection(request).apply {
            setDialing()
        }
        bindCallForegroundServiceWorker()
        return connection
    }

    private fun bindCallForegroundServiceWorker(block: ((CallUI) -> Unit)? = null) {
        callForegroundServiceWorker.bind(this) { call ->
            if (call.shouldShowAsActivity()) {
                call.showOnAppResumed(coroutineScope)
            }
            block?.invoke(call)
        }
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection = createConnection(request)

    private fun createConnection(request: ConnectionRequest): CallConnection {
        return CallConnection.create(request = request).apply {
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
        bindCallForegroundServiceWorker { call ->
            coroutineScope.launch {
                val participants = call.participants.value
                val callee = participants.others.map { it.combinedDisplayName.filterNotNull().firstOrNull() ?: Uri.EMPTY }.joinToString()
                createOrUpdateConnectionServiceContact(
                    this@PhoneConnectionService,
                    connection.address,
                    callee
                )
            }
        }
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

    override fun onNewNotification(call: Call, notification: Notification, id: Int) {
        if (foregroundJob != null) return
        foregroundJob = flowOf(call)
            .hasScreenSharingInput()
            .onEach { hasScreenSharingPermission ->
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(CALL_NOTIFICATION_ID, notification, getForegroundServiceType(hasScreenSharingPermission))
                    else startForeground(CALL_NOTIFICATION_ID, notification)
                }
            }
            .launchIn(coroutineScope)
    }

    override fun onClearNotification(id: Int) = stopForegroundService()

    private fun stopForegroundService() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }

}