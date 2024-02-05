package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import android.os.OutcomeReceiver
import android.os.ParcelUuid
import android.telecom.CallEndpoint
import android.telecom.CallEndpointException
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputs
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToCurrentAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallEndpointExtensions.toAudioOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.M)
class CallConnection private constructor(val call: Call, val coroutineScope: CoroutineScope) : Connection() {

    interface Listener {
        fun onConnectionStateChange(connection: CallConnection) = Unit

        fun onShowIncomingCallUi(connection: CallConnection) = Unit

        fun onAudioOutputStateChanged(connectionAudioState: CallAudioState) = Unit

        fun onSilence() = Unit
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun create(
            request: ConnectionRequest,
            call: Call,
            coroutineScope: CoroutineScope = MainScope()
        ): CallConnection {
            return CallConnection(call, coroutineScope).apply {
                setInitializing()
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                connectionProperties = PROPERTY_SELF_MANAGED
                audioModeIsVoip = true
                connectionCapabilities =
                    CAPABILITY_MUTE or CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD
                extras = request.extras
            }
        }
    }

    var audioState: CallAudioState = CallAudioState()

    private val listeners: MutableList<Listener> = mutableListOf()

    private val endpointExecutor by lazy { Executors.newSingleThreadExecutor() }

    private var wasAnswered = false

    private var wasRejected = false

    private var availableCallEndpoints: List<CallEndpoint> = listOf()


    init {
        syncStateWithCall()
    }

    fun addListener(listener: Listener) = listeners.add(listener)

    fun removeListener(listener: Listener) = listeners.remove(listener)

    fun syncStateWithCall() {
        call.state
            .onEach {
                when (it) {
                    is Call.State.Connected -> setActive()
                    Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        setConnectionDisconnected(DisconnectCause.ANSWERED_ELSEWHERE)
                    } else {
                        setConnectionDisconnected(DisconnectCause.OTHER)
                    }

                    Call.State.Disconnected.Ended.LineBusy -> setConnectionDisconnected(DisconnectCause.BUSY)
                    Call.State.Disconnected.Ended.Declined, is Call.State.Disconnected.Ended.HungUp -> setConnectionDisconnected(DisconnectCause.REMOTE)
                    is Call.State.Disconnected.Ended.Error -> setConnectionDisconnected(DisconnectCause.ERROR)
                    Call.State.Disconnected.Ended, Call.State.Disconnected.Ended.Timeout, is Call.State.Disconnected.Ended.Kicked -> setConnectionDisconnected(DisconnectCause.OTHER)
                    else -> Unit
                }
            }
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .launchIn(coroutineScope)
    }

    override fun onAnswer(videoState: Int) {
        super.onAnswer(videoState)
        _onAnswer()
    }

    override fun onAnswer() {
        super.onAnswer()
        _onAnswer()
    }

    private fun _onAnswer() {
        // On some devices (like Huawei), both onAnswer() and onAnswer(int) are called
        if (wasAnswered) return
        wasAnswered = true
        call.connect()
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)
        listeners.forEach { it.onConnectionStateChange(this) }
    }

    override fun onHold() {
        super.onHold()
        coroutineScope.cancel()
        call.end()
        setConnectionDisconnected(DisconnectCause.LOCAL)
    }

    override fun onAbort() {
        super.onAbort()
        coroutineScope.cancel()
        call.end()
        setConnectionDisconnected(DisconnectCause.OTHER)
    }

    override fun onReject() {
        super.onReject()
        _onReject()
    }

    override fun onReject(rejectReason: Int) {
        super.onReject(rejectReason)
        _onReject()
    }

    override fun onReject(replyMessage: String?) {
        super.onReject(replyMessage)
        _onReject()
    }

    private fun _onReject() {
        if (wasRejected) return
        wasRejected = true
        coroutineScope.cancel()
        call.end()
        setConnectionDisconnected(DisconnectCause.REJECTED)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        coroutineScope.cancel()
        call.end()
        setConnectionDisconnected(DisconnectCause.LOCAL)
    }

    private fun setConnectionDisconnected(cause: Int) {
        setDisconnected(DisconnectCause(cause))
        destroy()
        listeners.clear()
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
        listeners.forEach { it.onShowIncomingCallUi(this) }
    }
    
    override fun onSilence() {
        super.onSilence()
        listeners.forEach { it.onSilence() }
    }

    ///// Audio legacy API /////

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(state: android.telecom.CallAudioState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val currentAudioOutput = state.mapToCurrentAudioOutput() ?: return
        audioState = CallAudioState(
            currentOutput = currentAudioOutput,
            availableOutputs = state.mapToAvailableAudioOutputs()
        )
        listeners.forEach { it.onAudioOutputStateChanged(audioState) }
    }

    ///// Audio API 34 /////

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint>) {
        this.availableCallEndpoints = availableEndpoints
        audioState = audioState.copy(availableOutputs = availableEndpoints.mapNotNull { it.toAudioOutput() })
        listeners.forEach { it.onAudioOutputStateChanged(audioState) }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
        audioState = audioState.copy(currentOutput = callEndpoint.toAudioOutput())
        listeners.forEach { it.onAudioOutputStateChanged(audioState) }
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        if (!isMuted) return
        audioState = audioState.copy(currentOutput = CallAudioOutput.Muted)
        listeners.forEach { it.onAudioOutputStateChanged(audioState) }
    }

    ///////////////////////////

    @RequiresApi(Build.VERSION_CODES.O)
    fun setAudioOutput(output: CallAudioOutput) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) setCallEndpoint(output)
        else setAudioRoute(output)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    private fun setAudioRoute(output: CallAudioOutput) =
        when (output) {
            is CallAudioOutput.Speaker -> setAudioRoute(android.telecom.CallAudioState.ROUTE_SPEAKER)
            is CallAudioOutput.WiredHeadset -> setAudioRoute(android.telecom.CallAudioState.ROUTE_WIRED_HEADSET)
            is CallAudioOutput.Earpiece -> setAudioRoute(android.telecom.CallAudioState.ROUTE_EARPIECE)
            is CallAudioOutput.Bluetooth -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val device = callAudioState.supportedBluetoothDevices.firstOrNull { it.address == output.id } ?: let {
                        return
                    }
                    requestBluetoothAudio(device)
                } else {
                    setAudioRoute(android.telecom.CallAudioState.ROUTE_BLUETOOTH)
                }
            }

            is CallAudioOutput.Muted -> onCallAudioStateChanged(
                android.telecom.CallAudioState(
                    true,
                    callAudioState.route,
                    callAudioState.supportedRouteMask
                )
            )
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun setCallEndpoint(output: CallAudioOutput) {
        if (output !is CallAudioOutput.Muted) {
            val endpoint = mapToCallEndpoint(output) ?: return
            requestCallEndpointChange(
                endpoint,
                endpointExecutor,
                object : OutcomeReceiver<Void, CallEndpointException> {
                    override fun onResult(result: Void?) = Unit
                    override fun onError(error: CallEndpointException) = Unit
                }
            )
        } else {
            onMuteStateChanged(true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun mapToCallEndpoint(output: CallAudioOutput): CallEndpoint? {
        return availableCallEndpoints.firstOrNull {
            when (output) {
                CallAudioOutput.Speaker -> it.endpointType == CallEndpoint.TYPE_SPEAKER
                CallAudioOutput.Earpiece -> it.endpointType == CallEndpoint.TYPE_EARPIECE
                CallAudioOutput.WiredHeadset -> it.endpointType == CallEndpoint.TYPE_WIRED_HEADSET
                is CallAudioOutput.Bluetooth -> {
                    it.endpointType == CallEndpoint.TYPE_BLUETOOTH && it.identifier == ParcelUuid.fromString(output.id)
                }

                CallAudioOutput.Muted -> false
            }
        }
    }

}