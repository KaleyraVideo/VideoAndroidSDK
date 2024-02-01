package com.kaleyra.video_common_ui

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.CallConnection
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkConstructor
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

}