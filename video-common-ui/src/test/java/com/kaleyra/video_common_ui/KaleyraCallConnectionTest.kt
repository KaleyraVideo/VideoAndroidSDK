package com.kaleyra.video_common_ui

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.CallAudioState
import android.telecom.CallAudioState.ROUTE_BLUETOOTH
import android.telecom.CallAudioState.ROUTE_EARPIECE
import android.telecom.CallAudioState.ROUTE_WIRED_HEADSET
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapCurrentRouteToAudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.CallAudioStateExtensions.mapToAvailableAudioOutputDevices
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnection
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class KaleyraCallConnectionTest {

    private val callMock = mockk<CallUI>(relaxed = true)

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
        mockkConstructor(KaleyraCallConnection::class)
        every { anyConstructed<KaleyraCallConnection>().setInitializing() } returns Unit
        every {
            anyConstructed<KaleyraCallConnection>().setAddress(
                requestMock.address,
                any()
            )
        } returns Unit
        every { anyConstructed<KaleyraCallConnection>().connectionProperties = any() } returns Unit
        every { anyConstructed<KaleyraCallConnection>().audioModeIsVoip = any() } returns Unit
        every { anyConstructed<KaleyraCallConnection>().connectionCapabilities = any() } returns Unit
        every { anyConstructed<KaleyraCallConnection>().extras = requestMock.extras } returns Unit

        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
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
        unmockkConstructor(KaleyraCallConnection::class)
    }

    @Test
    fun testOnAnswer() = runTest {
        val call = mockk<CallUI>(relaxed = true)
        val connection = KaleyraCallConnection.create(requestMock, call, backgroundScope)
        connection.onAnswer()
        verify(exactly = 1) { call.connect() }
        // on some devices the onAnswer is called multiple times, check the code is only executed once
        connection.onAnswer()
        verify(exactly = 1) { call.connect() }
    }

    @Test
    fun testOnAnswerWithVideoState() = runTest {
        val call = mockk<CallUI>(relaxed = true)
        val connection = KaleyraCallConnection.create(requestMock, call, backgroundScope)
        connection.onAnswer(0)
        verify(exactly = 1) { call.connect() }
        // on some devices the onAnswer is called multiple times, check the code is only executed once
        connection.onAnswer(0)
        verify(exactly = 1) { call.connect() }
    }

    @Test
    fun testOnStateChanged() = runTest {
        var result: KaleyraCallConnection? = null
        val listener = object : KaleyraCallConnection.Listener {
            override fun onConnectionStateChange(connection: KaleyraCallConnection) {
                result = connection
            }
        }
        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onStateChanged(Connection.STATE_ACTIVE)
        assertEquals(connection, result)
    }

    @Test
    fun testOnHold() = runTest {
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        val call = mockk<CallUI>(relaxed = true)
        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
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
        var result: KaleyraCallConnection? = null
        val listener = object : KaleyraCallConnection.Listener {
            override fun onShowIncomingCallUi(connection: KaleyraCallConnection) {
                result = connection
            }
        }
        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onShowIncomingCallUi()
        assertEquals(connection, result)
    }

    @Test
    fun callStateConnected_connectionSetToActive() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Connected)
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) { connection.setActive() }
    }

    @Test
    fun callStateAnsweredOnAnotherDevice_connectionSetToAnsweredElsewhere() =
        runTest(UnconfinedTestDispatcher()) {
            every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
            val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
            val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
        connection.syncStateWithCall()
        verify(exactly = 1) {
            connection.setDisconnected(withArg {
                assertEquals(it.code, DisconnectCause.BUSY)
            })
        }
    }

    @Test
    fun callStateCurrentUserInAnotherCall_connectionSetToBusy() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.CurrentUserInAnotherCall)
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
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
        val listener = object : KaleyraCallConnection.Listener {
            override fun onSilence() { silenced = true }
        }
        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
        connection.addListener(listener)
        connection.onSilence()
        assertEquals(true, silenced)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun testOnCallAudioStateChanged() = runTest {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_WIRED_HEADSET)
        val audioOutput = AudioOutputDevice.Earpiece()
        val availableAudioOutputs = listOf(AudioOutputDevice.None(), AudioOutputDevice.Loudspeaker())
        every { callAudioState.mapCurrentRouteToAudioOutputDevice() } returns audioOutput
        every { callAudioState.mapToAvailableAudioOutputDevices() } returns availableAudioOutputs
        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
        connection.onCallAudioStateChanged(callAudioState)
        assertEquals(audioOutput, connection.currentAudioDevice.first())
        assertEquals(availableAudioOutputs, connection.availableAudioDevices.first())
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun testMutedApi34() = runTest {
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
        val initialRoute = ROUTE_EARPIECE
        val mockkCallAudioState = mockk<CallAudioState>(relaxed = true) {
            every { isMuted } returns false
            every { route } returns initialRoute
        }
        every { connection.callAudioState } returns mockkCallAudioState
        connection.onMuteStateChanged(true)
        advanceUntilIdle()
        Assert.assertEquals(true, connection.currentAudioDevice.first() is AudioOutputDevice.None)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
    fun testUnMuteApi34() = runTest {
        val connection = spyk(KaleyraCallConnection.create(requestMock, callMock, backgroundScope))
        val unMutedRoute = ROUTE_EARPIECE
        val mockkCallAudioState = mockk<CallAudioState>(relaxed = true) {
            every { isMuted } returns false
            every { route } returns unMutedRoute
        }
        every { connection.callAudioState } returns mockkCallAudioState
        connection.onMuteStateChanged(true)
        advanceUntilIdle()
        connection.onMuteStateChanged(false)
        advanceUntilIdle()
        val currentAudioDevice = connection.currentAudioDevice.first { it is AudioOutputDevice.Earpiece }
        Assert.assertEquals(true, currentAudioDevice is AudioOutputDevice.Earpiece)
    }

    @Test
    fun testAudioDevicesUpdatedOnActivityResumedCallback() = runTest {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_BLUETOOTH)
        val availableAudioOutputs = listOf(AudioOutputDevice.None(), AudioOutputDevice.Bluetooth())
        val call = mockk<CallUI>(relaxed = true)
        val activity = mockk<Activity>()
        every { call.activityClazz } returns activity::class.java
        every { callAudioState.mapToAvailableAudioOutputDevices() } returns availableAudioOutputs

        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
        every { connection.callAudioState } returns callAudioState
        connection.onActivityResumed(activity)

        assertEquals(availableAudioOutputs, connection.availableAudioDevices.first())
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    fun testAudioDevicesUpdatedOnlyOnCallActivityResumed() = runTest {
        mockkObject(CallAudioStateExtensions)
        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_BLUETOOTH)
        val availableAudioOutputs = listOf(AudioOutputDevice.None(), AudioOutputDevice.Bluetooth())
        val call = mockk<CallUI>(relaxed = true)
        every { call.activityClazz } returns this@KaleyraCallConnectionTest::class.java
        every { callAudioState.mapToAvailableAudioOutputDevices() } returns availableAudioOutputs

        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
        every { connection.callAudioState } returns callAudioState
        connection.onActivityResumed(mockk())

        val result = withTimeoutOrNull(100) {
            connection.availableAudioDevices.first()
        }
        assertEquals(null, result)
        unmockkObject(CallAudioStateExtensions)
    }

    @Test
    fun testAudioDevicesDoNotCrashWhenCallAudioStateIsNull() = runTest {
        mockkObject(CallAudioStateExtensions)
        val call = mockk<CallUI>(relaxed = true)
        val activity = mockk<Activity>()
        every { call.activityClazz } returns activity::class.java

        val connection = spyk(KaleyraCallConnection.create(requestMock, call, backgroundScope))
        every { connection.callAudioState } returns null

        connection.onActivityResumed(activity)

        unmockkObject(CallAudioStateExtensions)
    }

//    @Test
//    @Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
//    fun testOnCallAudioStateChangedApi34() = runTest {
//        mockkObject(CallAudioStateExtensions)
//        val callAudioState = CallAudioState(true, ROUTE_EARPIECE, ROUTE_EARPIECE or ROUTE_WIRED_HEADSET)
//        val audioOutput = AudioOutputDevice.Earpiece()
//        val availableAudioOutputs = listOf(AudioOutputDevice.None(), AudioOutputDevice.Loudspeaker())
//        every { callAudioState.mapCurrentRouteToAudioOutputDevice() } returns audioOutput
//        every { callAudioState.mapToAvailableAudioOutputDevices() } returns availableAudioOutputs
//        val connection = KaleyraCallConnection.create(requestMock, callMock, backgroundScope)
//        connection.onCallAudioStateChanged(callAudioState)
//        assertEquals(null, connection.currentAudioDevice.first())
//        assertEquals(listOf<AudioOutputDevice>(), connection.availableAudioDevices.first())
//        unmockkObject(CallAudioStateExtensions)
//    }

//    @Test
//    fun testOnAvailableCallEndpointsChange() = runTest {
//        mockkObject(CallEndpointExtensions)
//        val speakerEndpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
//        val earpieceEndpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
//        val speakerAudioOutput = AudioOutputDevice.Loudspeaker()
//        val earpieceAudioOutput = AudioOutputDevice.Earpiece()
//        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
//        every { speakerEndpoint.mapToAudioOutputDevice() } returns speakerAudioOutput
//        every { earpieceEndpoint.mapToAudioOutputDevice() } returns earpieceAudioOutput
//        connection.onAvailableCallEndpointsChanged(listOf(speakerEndpoint, earpieceEndpoint))
//        val expected = listOf(speakerAudioOutput, earpieceAudioOutput, AudioOutputDevice.None())
//        assertEquals(expected, connection.availableAudioDevices.first())
//        unmockkObject(CallEndpointExtensions)
//    }

//    @Test
//    fun testOnCallEndpointChanged() = runTest {
//        mockkObject(CallEndpointExtensions)
//        val endpoint = spyk(CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID())))
//        val audioOutput = AudioOutputDevice.Loudspeaker()
//        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
//        every { endpoint.mapToAudioOutputDevice() } returns audioOutput
//        connection.onCallEndpointChanged(endpoint)
//        assertEquals(audioOutput, connection.currentAudioDevice.first())
//        unmockkObject(CallEndpointExtensions)
//    }

//    @Test
//    fun isMutedTrue_onMuteStateChanged_audioStateMuted() = runTest {
//        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
//        connection.onMuteStateChanged(true)
//        assertEquals(AudioOutputDevice.None(), connection.currentAudioDevice.first())
//    }

//    @Test
//    fun isMutedFalse_onMuteStateChanged_audioStateNotUpdated() = runTest {
//        val connection = CallConnection.create(requestMock, callMock, backgroundScope)
//        connection.onMuteStateChanged(false)
//        assertEquals(null, connection.currentAudioDevice.first())
//    }

}
