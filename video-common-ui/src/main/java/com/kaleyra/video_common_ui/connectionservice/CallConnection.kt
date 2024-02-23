package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapCurrentRouteToAudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputDevices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

@RequiresApi(Build.VERSION_CODES.M)
class CallConnection private constructor(val call: Call, val coroutineScope: CoroutineScope) : Connection() {

    interface Listener {
        fun onConnectionStateChange(connection: CallConnection) = Unit

        fun onShowIncomingCallUi(connection: CallConnection) = Unit

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

    private val _currentAudioDevice: MutableStateFlow<AudioOutputDevice?> = MutableStateFlow(null)

    private val _availableAudioDevices: MutableStateFlow<List<AudioOutputDevice>> = MutableStateFlow(listOf())

    private val listeners: MutableList<Listener> = mutableListOf()

    private var wasAnswered = false

    private var wasRejected = false

    val currentAudioDevice: StateFlow<AudioOutputDevice?> = _currentAudioDevice

    val availableAudioDevices: StateFlow<List<AudioOutputDevice>> = _availableAudioDevices

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
        _currentAudioDevice.value = state.mapCurrentRouteToAudioOutputDevice()
        _availableAudioDevices.value = state.mapToAvailableAudioOutputDevices()
    }

    ///// Audio API 34 /////

//    Try to use the new API when the endpoint issue will be solved: https://issuetracker.google.com/issues/302436283
//
//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint>) {
//        _availableAudioDevices.value = availableEndpoints.mapNotNull { it.mapToAudioOutputDevice() } + AudioOutputDevice.None()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
//        _currentAudioDevice.value = callEndpoint.mapToAudioOutputDevice()
//    }
//
//    override fun onMuteStateChanged(isMuted: Boolean) {
//        if (!isMuted) return
//        _currentAudioDevice.value = AudioOutputDevice.None()
//    }

}