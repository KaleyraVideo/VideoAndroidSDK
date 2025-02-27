/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.StreamMapper
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.isConnected
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CallStateMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>()

    private val participantMeMock = mockk<CallParticipant.Me>(relaxed = true) {
        every { state } returns MutableStateFlow(CallParticipant.State.InCall)
    }

    private val participantMock = mockk<CallParticipant>(relaxed = true) {
        every { state } returns MutableStateFlow(CallParticipant.State.InCall)
    }

    private val callParticipantsMock = mockk<CallParticipants>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.doAnyOfMyStreamsIsLive() } returns flowOf(false)
        }
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callMock) {
            every { isLink } returns false
        }
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { others } returns listOf(participantMock)
        }
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun stateReconnecting_toCallStateUi_callStateReconnecting() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Reconnecting, result.first())
    }

    @Test
    fun stateConnectingAndCallCreatorIsMe_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns participantMeMock
        every { callParticipantsMock.others } returns listOf(mockk(relaxed = true) {
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        })
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun connectedUserNull_toCallStateUi_callStateConnecting() = runTest {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns null
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Connecting, result.first())
    }

    @Test
    fun creatorIsNullAndCallIsConnecting_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns null
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun stateConnecting_toCallStateUi_callStateConnecting() = runTest {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns callParticipantsMock.me
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Connecting, result.first())
    }

    @Test
    fun stateConnectedAndIDoNotHaveLiveStreams_toCallStateUi_callStateConnecting() = runTest {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Connected)
        every { callParticipantsMock.creator() } returns null
        with(StreamMapper) {
            every { callMock.amIAlone() } returns flowOf(false)
        }
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Connecting, result.first())
    }

    @Test
    fun stateConnectingAndIAmNotCallCreator_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Ringing, result.first())
    }

    @Test
    fun callIsLinkAndStateConnectingAndIAmNotCallCreator_toCallStateUi_callStateNotRinging() = runTest {
        every { callMock.isLink } returns true
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected, result.first())
    }

    @Test
    fun callIsLinkAndStateConnectingAndCallCreatorIsNull_toCallStateUi_callStateNotRinging() = runTest {
        every { callMock.isLink } returns true
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns null
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun stateConnectingAndCallCreatorIsNull_toCallStateUi_callStateDialing() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns null
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Dialing, result.first())
    }

    @Test
    fun stateConnectingAndIAmCallCreatorAndOtherIsRinging_toCallStateUi_callStateRingingRemotely() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns callParticipantsMock.me
        every { callParticipantsMock.others } returns listOf(mockk(relaxed = true) {
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall.Ringing)
        })
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.RingingRemotely, result.first())
    }

    @Test
    fun stateConnectedAndIHaveALiveStream_toCallStateUi_callStateConnected() = runTest {
        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Connected)
        every { callParticipantsMock.creator() } returns mockk()
        with(StreamMapper) {
            every { callMock.doAnyOfMyStreamsIsLive() } returns flowOf(true)
        }
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Connected, result.first())
    }

    @Test
    fun stateAnsweredOnAnotherDevice_toCallStateUi_callStateAnsweredOnAnotherDevice() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice, result.first())
    }

    @Test
    fun stateDeclined_toCallStateUi_callStateDeclined() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Declined, result.first())
    }

    @Test
    fun stateLineBusy_toCallStateUi_callStateLineBusy() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.LineBusy, result.first())
    }

    @Test
    fun stateTimeout_toCallStateUi_callStateTimeout() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Timeout, result.first())
    }

    @Test
    fun stateDisconnectedEnded_connectedUserNull_toCallStateUi_callStateEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended, result.first())
    }

    @Test
    fun stateServerError_toCallStateUi_callStateErrorServer() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error.Server, result.first())
    }

    @Test
    fun stateUnknownError_toCallStateUi_callStateUnknownError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error.Unknown, result.first())
    }

    @Test
    fun stateHungUp_toCallStateUi_callStateHungUp() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.HungUp, result.first())
    }

    @Test
    fun stateKicked_toCallStateUi_callStateKicked() = runTest {
        mockkObject(ContactDetailsManager)
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked("adminUserId"))
        with(participantMock) {
            every { userId } returns "adminUserId"
            every { combinedDisplayName }returns MutableStateFlow("adminUserName")
        }
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Kicked("adminUserName"), result.first())
    }

    @Test
    fun stateError_toCallStateUi_callStateError() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.Error, result.first())
    }

    @Test
    fun stateDisconnectedAndIAmNotCallCreator_toCallStateUi_callStateRinging() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Ringing, result.first())
    }

    @Test
    fun stateDisconnectedAndCallCreatorNull_toCallStateUi_callStateDisconnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns null
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected, result.first())
    }

    @Test
    fun stateDisconnectedEndedAndIAmNotCallCreator_toCallStateUi_callStateEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { callParticipantsMock.creator() } returns mockk()
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended, result.first())
    }

    @Test
    fun stateDisconnected_toCallStateUi_callStateDisconnected() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns participantMeMock
        every { callParticipantsMock.others } returns listOf(mockk(relaxed = true) {
            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
        })
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected, result.first())
    }

    @Test
    fun stateDisconnecting_toCallStateUi_callStateHungUp() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnecting)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnecting, result.first())
    }

    @Test
    fun stateCurrentUserInAnotherCall_toCallStateUi_callStateCurrentUserInAnotherCall() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.CurrentUserInAnotherCall)
        val result = callMock.toCallStateUi()
        Assert.assertEquals(CallStateUi.Disconnected.Ended.CurrentUserInAnotherCall, result.first())
    }

    @Test
    fun stateConnected_isConnected_true() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun stateUnknownError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateServerError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateTimeout_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateLineBusy_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateAnsweredOnAnotherDevice_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateKicked_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked(""))
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDeclined_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateHungUp_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateEnded_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateDisconnected_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateConnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun stateReconnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val actual = callMock.isConnected().first()
        Assert.assertEquals(false, actual)
    }
}