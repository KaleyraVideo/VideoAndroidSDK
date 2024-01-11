package com.kaleyra.video_common_ui.connectionservice

import android.app.Notification
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUncaughtExceptionHandler
import com.kaleyra.video_common_ui.ICallService
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationDelegate
import com.kaleyra.video_common_ui.call.CameraStreamInputsDelegate
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.call.StreamsOpeningDelegate
import com.kaleyra.video_common_ui.call.StreamsVideoViewDelegate
import com.kaleyra.video_common_ui.connectionservice.ContactsController.createOrUpdateConnectionServiceContact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
 class PhoneConnectionService : ConnectionService(), CallConnection.IncomingCallListener,
    CallConnection.ConnectionStateListener,
    ICallService {

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

//    private var call: CallUI? = null

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override val cameraStreamPublisher by lazy { CameraStreamPublisher(ioScope) }

    override val cameraStreamInputDelegate by lazy { CameraStreamInputsDelegate(ioScope) }

    override val streamOpeningDelegate by lazy { StreamsOpeningDelegate(ioScope) }

    override val streamVideoViewDelegate by lazy { StreamsVideoViewDelegate(ioScope) }

    override val fileShareNotificationDelegate by lazy { FileShareNotificationDelegate(ioScope) }

    override var screenShareOverlayDelegate: ScreenShareOverlayDelegate? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread.setDefaultUncaughtExceptionHandler(CallUncaughtExceptionHandler)
        screenShareOverlayDelegate = ScreenShareOverlayDelegate(application, ioScope)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearNotification()
        ProximityService.stop()
//        call?.end()
        screenShareOverlayDelegate?.dispose()
        ioScope.cancel()
//        call = null
    }

    /**
     * Set up the call streams and notifications
     */
    private fun setUpCall(connection: CallConnection) {
        setUpCall(this, ioScope) { stopSelf() }
        KaleyraVideo.onCallReady(ioScope) { call ->
            if (call.shouldShowAsActivity()) {
                call.showOnAppResumed(MainScope())
            }

            if (connection.isIncoming) {
                MainScope().launch {
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

    override fun startForegroundService(notification: Notification) {
        KaleyraVideo.onCallReady(ioScope) { call ->
            flowOf(call)
                .hasScreenSharingInput()
                .onEach { hasScreenSharingPermission ->
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(CallNotificationDelegate.CALL_NOTIFICATION_ID, notification, getForegroundServiceType(hasScreenSharingPermission))
                        else startForeground(CallNotificationDelegate.CALL_NOTIFICATION_ID, notification)
                    }
                }
                .launchIn(MainScope())
        }
    }

    override fun stopForegroundService() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    }

}