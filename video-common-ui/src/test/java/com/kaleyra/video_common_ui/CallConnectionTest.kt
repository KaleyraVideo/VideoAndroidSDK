package com.kaleyra.video_common_ui

import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.telecom.CallAudioState
import android.telecom.CallAudioState.ROUTE_EARPIECE
import android.telecom.CallAudioState.ROUTE_WIRED_HEADSET
import android.telecom.CallEndpoint
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.CallAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputs
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToCurrentAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallConnection
import com.kaleyra.video_common_ui.connectionservice.CallEndpointExtensions
import com.kaleyra.video_common_ui.connectionservice.CallEndpointExtensions.toAudioOutput
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallConnectionTest {

    private val callMock = mockk<Call>(relaxed = true)

    private val requestMock = mockk<ConnectionRequest>(relaxed = true)

    private val uriMock = Uri.fromParts(PhoneAccount.SCHEME_SIP, "+0000000000", null)

    private val extrasMock = Bundle()

    @Before
    fun setUp() {
        with(requestMock) {
            every { address } returns uriMock
            every { extras } returns extrasMock
        }
    }

    @Test
    fun testCreate() = runTest {
        mockkConstructor(CallConnection::class)
        every { anyConstructed<CallConnection>().setInitializing() } returns Unit
        every {
            anyConstructed<CallConnection>().setAddress(
                requestMock.address,
                any()
            )
        } returns Unit
        every { anyConstructed<CallConnection>().connectionProperties = any() } returns Unit
        every { anyConstructed<CallConnection>().audioModeIsVoip = any() } returns Unit
        every { anyConstructed<CallConnection>().connectionCapabilities = any() } returns Unit
        every { anyConstructed<CallConnection>().extras = requestMock.extras } returns Unit

        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        verify(exactly = 1) { connection.setInitializing() }
        verify(exactly = 1) {
            connection.setAddress(
                requestMock.address,
                TelecomManager.PRESENTATION_ALLOWED
            )
        }
        verify(exactly = 1) { connection.connectionProperties = Connection.PROPERTY_SELF_MANAGED }
        verify(exactly = 1) { connection.audioModeIsVoip = true }
        verify(exactly = 1) {
            connection.connectionCapabilities =
                Connection.CAPABILITY_MUTE or Connection.CAPABILITY_HOLD or Connection.CAPABILITY_SUPPORT_HOLD
        }
        verify(exactly = 1) { connection.extras = requestMock.extras }
        unmockkConstructor(CallConnection::class)
    }

    @Test
    fun testOnAnswer() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = CallConnection.create(requestMock, call, backgroundScope)
        connection.onAnswer()
        verify(exactly = 1) { call.connect() }
        // on some devices the onAnswer is called multiple times, check the code is only executed once
        connection.onAnswer()
        verify(exactly = 1) { call.connect() }
    }

    @Test
    fun testOnAnswerWithVideoState() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = CallConnection.create(requestMock, call, backgroundScope)
        connection.onAnswer(0)
        verify(exactly = 1) { call.connect() }
        // on some devices the onAnswer is called multiple times, check the code is only executed once
        connection.onAnswer(0)
        verify(exactly = 1) { call.connect() }
    }

    @Test
    fun testOnStateChanged() = runTest {
        var result: CallConnection? = null
        val listener = object : CallConnection.Listener {
            override fun onConnectionStateChange(connection: CallConnection) {
                result = connection
            }
        }
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onStateChanged(Connection.STATE_ACTIVE)
        assertEquals(connection, result)
    }

    @Test
    fun testOnHold() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onHold()
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.LOCAL)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnAbort() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onAbort()
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.OTHER)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnReject() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onReject()
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.REJECTED)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnRejectReason() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onReject(0)
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.REJECTED)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnRejectReply() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onReject("")
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.REJECTED)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnDisconnect() = runTest {
        val call = mockk<Call>(relaxed = true)
        val connection = spyk(CallConnection.create(requestMock, call, backgroundScope))
        connection.onDisconnect()
        verify(exactly = 1) { call.end() }
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.LOCAL)
            })
        }
        verify(exactly = 1) { connection.destroy() }
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testOnShowIncomingCall() = runTest {
        var result: CallConnection? = null
        val listener = object : CallConnection.Listener {
            override fun onShowIncomingCallUi(connection: CallConnection) {
                result = connection
            }
        }
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onShowIncomingCallUi()
        assertEquals(connection, result)
    }

    @Test
    fun callStateConnected_connectionSetToActive() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Connected)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) { connection.setActive() }
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_connectionSetToAnsweredElsewhere() =
        runTest(UnconfinedTestDispatcher()) {
            every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
            val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
            connection.syncStateWithCall()
            verify(exactly = 1) {
                connection.setDisconnected(withArg {
                    assertEquals(it.code, DisconnectCause.ANSWERED_ELSEWHERE)
                })
            }
        }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun callStateAnsweredOnAnotherDeviceApi24_connectionSetToOther() =
        runTest(UnconfinedTestDispatcher()) {
            every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
            val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
            connection.syncStateWithCall()
            verify(exactly = 1) {
                connection.setDisconnected(withArg {
                    assertEquals(it.code, DisconnectCause.OTHER)
                })
            }
        }

    @Test
    fun callStateLineBusy_connectionSetToBusy() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.LineBusy)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.BUSY)
            })
        }
    }

    @Test
    fun callStateDeclined_connectionSetToRemote() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.Declined)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.REMOTE)
            })
        }
    }

    @Test
    fun callStateHungUp_connectionSetToRemote() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.HungUp(""))
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.REMOTE)
            })
        }
    }

    @Test
    fun callStateError_connectionSetToError() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.Error)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.ERROR)
            })
        }
    }

    @Test
    fun callStateTimeout_connectionSetToOther() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.Timeout)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.OTHER)
            })
        }
    }

    @Test
    fun callStateKicked_connectionSetToOther() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.Kicked(""))
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.OTHER)
            })
        }
    }

    @Test
    fun callStateEnded_connectionSetToOther() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended)
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.OTHER)
            })
        }
    }

    @Test
    fun testOnSilence() = runTest {
        var silenced = false
        val listener = object : CallConnection.Listener {
            override fun onSilence() { silenced = true }
        }
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onSilence()
        assertEquals(true, silenced)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun testOnCallAudioStateChanged() = runTest {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_WIRED_HEADSET)
        val audioOutput = CallAudioOutput.Earpiece
        val availableAudioOutputs = listOf(CallAudioOutput.Muted, CallAudioOutput.Speaker)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        every { callAudioState.mapToCurrentAudioOutput() } returns audioOutput
        every { callAudioState.mapToAvailableAudioOutputs() } returns availableAudioOutputs
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onCallAudioStateChanged(callAudioState)
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState(
            currentOutput = audioOutput,
            availableOutputs = availableAudioOutputs
        )
        assertEquals(expected, audioOutputState)
        assertEquals(expected, connection.audioState)
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun testOnCallAudioStateChangedApi34() = runTest {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_WIRED_HEADSET)
        val audioOutput = CallAudioOutput.Earpiece
        val availableAudioOutputs = listOf(CallAudioOutput.Muted, CallAudioOutput.Speaker)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        every { callAudioState.mapToCurrentAudioOutput() } returns audioOutput
        every { callAudioState.mapToAvailableAudioOutputs() } returns availableAudioOutputs
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onCallAudioStateChanged(callAudioState)
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState()
        assertEquals(null, audioOutputState)
        assertEquals(expected, connection.audioState)
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    fun testOnAvailableCallEndpointsChange() = runTest {
        mockkObject(CallEndpointExtensions)
        val speakerEndpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
        val earpieceEndpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
        val speakerAudioOutput = CallAudioOutput.Speaker
        val earpieceAudioOutput = CallAudioOutput.Earpiece
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        every { speakerEndpoint.toAudioOutput() } returns speakerAudioOutput
        every { earpieceEndpoint.toAudioOutput() } returns earpieceAudioOutput
        connection.addListener(listener)
        connection.onAvailableCallEndpointsChanged(listOf(speakerEndpoint, earpieceEndpoint))
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState(availableOutputs = listOf(speakerAudioOutput, earpieceAudioOutput))
        assertEquals(expected, connection.audioState)
        assertEquals(expected, audioOutputState)
        unmockkObject(CallEndpointExtensions)
    }

    @Test
    fun testOnCallEndpointChanged() = runTest {
        mockkObject(CallEndpointExtensions)
        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
        val audioOutput = CallAudioOutput.Speaker
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        every { endpoint.toAudioOutput() } returns audioOutput
        connection.addListener(listener)
        connection.onCallEndpointChanged(endpoint)
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState(currentOutput = audioOutput)
        assertEquals(expected, connection.audioState)
        assertEquals(expected, audioOutputState)
        unmockkObject(CallEndpointExtensions)
    }

    @Test
    fun isMutedTrue_onMuteStateChanged_audioStateMuted() = runTest {
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        connection.addListener(listener)
        connection.onMuteStateChanged(true)
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState(currentOutput = CallAudioOutput.Muted)
        assertEquals(expected, connection.audioState)
        assertEquals(expected, audioOutputState)
    }

    @Test
    fun isMutedFalse_onMuteStateChanged_audioStateNotUpdated() = runTest {
        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
        var audioOutputState: com.kaleyra.video_common_ui.connectionservice.CallAudioState? = null
        val listener = object : CallConnection.Listener {
            override fun onAudioOutputStateChanged(connectionAudioState: com.kaleyra.video_common_ui.connectionservice.CallAudioState) {
                audioOutputState = connectionAudioState
            }
        }
        connection.addListener(listener)
        connection.onMuteStateChanged(false)
        val expected = com.kaleyra.video_common_ui.connectionservice.CallAudioState()
        assertEquals(expected, connection.audioState)
        assertEquals(null, audioOutputState)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun api34_setSpeakerAudioOutput_speakerEndpointSet() = runTest {
        mockkObject(CallEndpointExtensions)
        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
        val audioOutput = CallAudioOutput.Speaker
        every { endpoint.toAudioOutput() } returns audioOutput
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.onAvailableCallEndpointsChanged(listOf(endpoint))
        connection.setAudioOutput(CallAudioOutput.Speaker)
        verify(exactly = 1) { connection.requestCallEndpointChange(endpoint, any(), any()) }
        unmockkObject(CallEndpointExtensions)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun api34_setWiredHeadsetOutput_wiredHeadsetEndpointSet() = runTest {
        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_WIRED_HEADSET, ParcelUuid(UUID.randomUUID())))
        val audioOutput = CallAudioOutput.WiredHeadset
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.onAvailableCallEndpointsChanged(listOf(endpoint))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.requestCallEndpointChange(endpoint, any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun api34_setEarpieceOutput_earpieceEndpointSet() = runTest {
        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_EARPIECE, ParcelUuid(UUID.randomUUID())))
        val audioOutput = CallAudioOutput.Earpiece
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.onAvailableCallEndpointsChanged(listOf(endpoint))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.requestCallEndpointChange(endpoint, any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun api34_setBluetoothOutput_bluetoothEndpointSet() = runTest {
        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_BLUETOOTH, ParcelUuid(UUID.randomUUID())))
        val audioOutput = CallAudioOutput.Bluetooth(endpoint.identifier.toString(), endpoint.endpointName.toString())
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.onAvailableCallEndpointsChanged(listOf(endpoint))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.requestCallEndpointChange(endpoint, any(), any()) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun api34_setMutedOutput_muteStateUpdated() = runTest {
        val audioOutput = CallAudioOutput.Muted
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.onMuteStateChanged(true) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun setSpeakerAudioOutput_speakerAudioRouteSet() = runTest {
        val audioOutput = CallAudioOutput.Speaker
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun setWiredHeadsetOutput_wiredHeadsetAudioRouteSet() = runTest {
        val audioOutput = CallAudioOutput.WiredHeadset
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_WIRED_HEADSET) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun setEarpieceOutput_earpieceAudioRouteSet() = runTest {
        val audioOutput = CallAudioOutput.Earpiece
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun setBluetoothOutputApi27_bluetoothAudioRouteSet() = runTest {
        val audioOutput = CallAudioOutput.Bluetooth("id", "name")
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_BLUETOOTH) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun setBluetoothOutput_bluetoothAudioRouteSet() = runTest {
        val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
        val bluetoothDevice2 = mockk<BluetoothDevice>(relaxed = true)
        every { bluetoothDevice.address } returns "address"
        every { bluetoothDevice2.address } returns "address2"
        val audioOutput = CallAudioOutput.Bluetooth("address", "name")
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        every { connection.callAudioState.supportedBluetoothDevices } returns listOf(bluetoothDevice2, bluetoothDevice)
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.requestBluetoothAudio(bluetoothDevice) }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun setMutedOutput_muteStateUpdated() = runTest {
        val audioOutput = CallAudioOutput.Muted
        val connection = spyk(CallConnection.create(requestMock, callMock, backgroundScope))
        val currentRoute = CallAudioState.ROUTE_SPEAKER
        val supportedMask = CallAudioState.ROUTE_SPEAKER or CallAudioState.ROUTE_EARPIECE
        val callAudioState = mockk<CallAudioState>(relaxed = true) {
            every { route } returns currentRoute
            every { supportedRouteMask } returns supportedMask
        }
        every { connection.callAudioState } returns callAudioState
        connection.setAudioOutput(audioOutput)
        verify(exactly = 1) { connection.onCallAudioStateChanged(CallAudioState(true, currentRoute, supportedMask)) }
    }
}
