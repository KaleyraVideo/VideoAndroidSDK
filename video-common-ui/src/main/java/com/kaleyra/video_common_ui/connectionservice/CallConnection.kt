package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.kaleyra.video.conference.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

@RequiresApi(Build.VERSION_CODES.M)
class CallConnection private constructor(val call: Call, val coroutineScope: CoroutineScope) : Connection() {

    interface Listener {
        fun onConnectionStateChange(connection: CallConnection) = Unit
        fun onShowIncomingCallUi(connection: CallConnection) = Unit

//        fun onAudioOutputStateChanged(audioOutputState: AudioOutputState) = Unit

//        fun onSilent() = Unit
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun create(request: ConnectionRequest, call: Call, coroutineScope: CoroutineScope = MainScope()): CallConnection {
            return CallConnection(call, coroutineScope).apply {
                setInitializing()
                setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
                connectionProperties = PROPERTY_SELF_MANAGED
                audioModeIsVoip = true
                connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD
                extras = request.extras
            }
        }
    }

    private val listeners: MutableList<Listener> = mutableListOf()

    private var wasAnswered = false

    private var wasRejected = false

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

    override fun onSilence() {
        super.onSilence()
//        listeners.forEach { it.onSilent() }
//        CallSound.stop()
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
        listeners.forEach { it.onShowIncomingCallUi(this) }
    }

    private fun setConnectionDisconnected(cause: Int) {
        setDisconnected(DisconnectCause(cause))
        destroy()
        listeners.clear()
    }

//    var audioOutputState: AudioOutputState = AudioOutputState()
//        private set

//    private var availableEndpoints: List<CallEndpoint> = listOf()

    ///////////// NEW AUDIO API //////////////
//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint>) {
//        audioOutputState = audioOutputState.copy(availableOutputs = availableEndpoints.mapNotNull { it.toAudioOutput() })
//        this.availableEndpoints = availableEndpoints
//        audioOutputListeners.forEach { it.onAudioOutputStateChanged(audioOutputState) }
//    }

//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
//        audioOutputState = audioOutputState.copy(currentOutput = callEndpoint.toAudioOutput())
//        audioOutputListeners.forEach { it.onAudioOutputStateChanged(audioOutputState) }
//    }

//    override fun onMuteStateChanged(isMuted: Boolean) {
//        if (!isMuted) return
//        audioOutputState = audioOutputState.copy(currentOutput = AudioOutput.Muted)
//        audioOutputListeners.forEach { it.onAudioOutputStateChanged(audioOutputState) }
//    }

//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    private fun CallEndpoint.toAudioOutput(): AudioOutput? {
//        return when (endpointType) {
//            CallEndpoint.TYPE_EARPIECE -> AudioOutput.Earpiece
//            CallEndpoint.TYPE_SPEAKER -> AudioOutput.Speaker
//            CallEndpoint.TYPE_WIRED_HEADSET -> AudioOutput.WiredHeadset
//            CallEndpoint.TYPE_BLUETOOTH -> {
//                AudioOutput.Bluetooth(id = identifier, name = endpointName.toString())
//            }
//
//            else -> null
//        }
//    }

    //////////////////////////////////////////

    ///////////// OLD AUDIO API //////////////
//    @Deprecated("Deprecated in Java")
//    override fun onCallAudioStateChanged(state: CallAudioState) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
//        val currentAudioOutput = state.toCurrentAudioOutput(context) ?: return
//        audioOutputState = AudioOutputState(
//            currentOutput = currentAudioOutput,
//            availableOutputs = state.toAvailableAudioOutput(context)
//        )
//        audioOutputListeners.forEach { it.onAudioOutputStateChanged(audioOutputState) }
//    }
    //////////////////////////////////////////

//    @RequiresApi(Build.VERSION_CODES.P)
//    fun setAudio(output: AudioOutput) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) setCallEndpoint(output)
//        else setAudioRoute(output)
//    }

//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    private fun setCallEndpoint(output: AudioOutput) {
//        val endpoint = when (output) {
//            AudioOutput.Speaker -> availableEndpoints.firstOrNull { it.endpointType == CallEndpoint.TYPE_SPEAKER }
//            AudioOutput.WiredHeadset -> availableEndpoints.firstOrNull { it.endpointType == CallEndpoint.TYPE_WIRED_HEADSET }
//            AudioOutput.Earpiece -> availableEndpoints.firstOrNull { it.endpointType == CallEndpoint.TYPE_EARPIECE }
//            is AudioOutput.Bluetooth -> availableEndpoints.firstOrNull { it.identifier == output.id }
//            AudioOutput.Muted -> null
//        } ?: return
//
//        requestCallEndpointChange(
//            endpoint,
//            mainExecutor!!,
//            object : OutcomeReceiver<Void, CallEndpointException> {
//                override fun onResult(result: Void?) = Unit
//                override fun onError(error: CallEndpointException) = Unit
//            }
//        )
//    }

//    @RequiresApi(Build.VERSION_CODES.P)
//    @Suppress("DEPRECATION")
//    private fun setAudioRoute(output: AudioOutput) {
//        when (output) {
//            is AudioOutput.Speaker -> setAudioRoute(CallAudioState.ROUTE_SPEAKER)
//            is AudioOutput.WiredHeadset -> setAudioRoute(CallAudioState.ROUTE_WIRED_HEADSET)
//            is AudioOutput.Earpiece -> setAudioRoute(CallAudioState.ROUTE_EARPIECE)
//            is AudioOutput.Bluetooth -> {
//                val device = callAudioState.supportedBluetoothDevices.firstOrNull { it.address == output.address } ?: return
//                requestBluetoothAudio(device)
//            }
//
//            is AudioOutput.Muted -> onCallAudioStateChanged(
//                CallAudioState(
//                    true,
//                    callAudioState.route,
//                    callAudioState.supportedRouteMask
//                )
//            )
//        }
//    }

}

//data class AudioOutputState(
//    val currentOutput: AudioOutput? = null,
//    val availableOutputs: List<AudioOutput> = listOf()
//)

//sealed class AudioOutput {
//    data object Muted : AudioOutput()
//    data object Earpiece : AudioOutput()
//    data object Speaker : AudioOutput()
//    data object WiredHeadset : AudioOutput()
//    data class Bluetooth(
//        val id: ParcelUuid? = null,
//        val address: String? = null,
//        val name: String? = null,
//        val uuids: List<ParcelUuid> = listOf()
//    ) : AudioOutput()
//}