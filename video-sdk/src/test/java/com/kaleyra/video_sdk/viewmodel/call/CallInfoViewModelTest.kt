package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.callinfo.viewmodel.toTextRef
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallInfoViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallInfoViewModel

    private val callParticipants = mockk<CallParticipants>()

    private val otherParticipant = mockk<CallParticipant>()

    private val call = mockk<CallUI>()

    private val conference = mockk<ConferenceUI>()

    @Before
    fun setup() {
        viewModel = CallInfoViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), mockk(relaxed = true), MutableStateFlow(mockk()))
        }
        mockkObject(CallStateMapper)
        mockkObject(ParticipantMapper)
        mockkObject(ContextRetainer)
        with(callParticipants) {
            every { me } returns mockk(relaxed = true)
            every { creator() } returns mockk(relaxed = true)
            every { callParticipants.others } returns listOf()
        }
        with(call) {
            every { participants } returns MutableStateFlow(callParticipants)
        }
        with(conference) {
            every { call } returns MutableSharedFlow<CallUI>(replay = 1).apply {
                tryEmit(this@CallInfoViewModelTest.call)
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCallStateUi_noDisplayNames_stateUpdated() = runTest {
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(listOf())

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(ImmutableList<String>(), viewModel.uiState.value.displayNames)
    }

    @Test
    fun testCallStateUi_withDisplayNames_stateUpdated() = runTest {
        val userDisplayNames = listOf("user1", "user2")
        every { callParticipants.others } returns listOf(mockk(relaxed = true), mockk(relaxed = true))
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(userDisplayNames)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(userDisplayNames.toImmutableList(), viewModel.uiState.value.displayNames)
    }

    @Test
    fun testCallStateUiConnecting_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connecting)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Connecting, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiDialing_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Dialing)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Dialing, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiRinging_oneParticipant_stateUpdated() = runTest {
        val expected = TextRef.PluralResource(R.plurals.kaleyra_call_incoming_status_ringing, 1)
        every { callParticipants.others } returns listOf(mockk(relaxed = true))
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Ringing)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(listOf("user1"))

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Ringing, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiRinging_moreParticipant_stateUpdated() = runTest {
        val callee = listOf("user1", "user2")
        val expected = TextRef.PluralResource(R.plurals.kaleyra_call_incoming_status_ringing, callee.size)
        every { callParticipants.others } returns callee.map { mockk<CallParticipant>(relaxed = true) }
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Ringing)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(callee)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Ringing, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiRingingRemotely_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.RingingRemotely, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiReconnecting_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Reconnecting)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Reconnecting, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiAnsweredOnAnotherDevice_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_user_answered_on_another_device)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiDeclined_oneParticipant_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_declined)
        every { callParticipants.others } returns listOf(mockk(relaxed = true))
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Declined)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(listOf("user1"))

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.Declined, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiDeclined_moreParticipant_stateUpdated() = runTest {
        val callee = listOf("user1", "user2")
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_declined)
        every { callParticipants.others } returns callee.map { mockk<CallParticipant>(relaxed = true) }
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Declined)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(callee)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.Declined, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiLineBusy_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_line_busy)
        every { callParticipants.others } returns listOf()
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.LineBusy)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.LineBusy, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiTimeout_oneParticipant_stateUpdated() = runTest {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_no_answer)
        every { callParticipants.others } returns listOf(mockk(relaxed = true))
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Timeout)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(listOf("user1"))

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.Timeout, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiTimeout_moreParticipant_stateUpdated() = runTest {
        val callee = listOf("user1", "user2")
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_no_answer)
        every { callParticipants.others } returns callee.map { mockk<CallParticipant>(relaxed = true) }
        every { call.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Timeout)
        every { call.toOtherDisplayNames() } returns MutableStateFlow(callee)

        viewModel.uiState.first()
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.Timeout, viewModel.uiState.value.callStateUi)
        Assert.assertEquals(expected, viewModel.uiState.value.displayState)
    }

    @Test
    fun testCallStateUiConnecting_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        val displayState = CallStateUi.Connecting.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateUiDialing_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        val displayState = CallStateUi.Dialing.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateUiRingingOto_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant)
        val expected = TextRef.PluralResource(id = R.plurals.kaleyra_call_incoming_status_ringing, quantity = call.participants.value.others.size)
        val displayState = CallStateUi.Ringing.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateUiRingingMtm_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant, otherParticipant)
        val expected = TextRef.PluralResource(id = R.plurals.kaleyra_call_incoming_status_ringing, quantity = call.participants.value.others.size)
        val displayState = CallStateUi.Ringing.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateRingingRemotely_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        val displayState = CallStateUi.RingingRemotely.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateReconnecting_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        val displayState = CallStateUi.Reconnecting.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedAnsweredOnAnotherDevice_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_user_answered_on_another_device)
        val displayState = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedDeclinedOto_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant)
        val expected = TextRef.StringResource(id = R.string.kaleyra_strings_info_call_declined)
        val displayState = CallStateUi.Disconnected.Ended.Declined.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedDeclinedMtm_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant, otherParticipant)
        val expected = TextRef.StringResource(id = R.string.kaleyra_strings_info_call_declined)
        val displayState = CallStateUi.Disconnected.Ended.Declined.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedLineBusy_toCallStateUi() {
        val expected = TextRef.StringResource(R.string.kaleyra_strings_info_call_line_busy)
        val displayState = CallStateUi.Disconnected.Ended.LineBusy.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedTimeoutOto_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant)
        val expected =  TextRef.StringResource(id = R.string.kaleyra_strings_info_call_no_answer)
        val displayState = CallStateUi.Disconnected.Ended.Timeout.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateEndedTimeoutMtm_toCallStateUi() {
        every { callParticipants.others } returns listOf(otherParticipant, otherParticipant)
        val expected = TextRef.StringResource(id = R.string.kaleyra_strings_info_call_no_answer)
        val displayState = CallStateUi.Disconnected.Ended.Timeout.toTextRef(call)
        Assert.assertEquals(expected, displayState)
    }

    @Test
    fun testCallStateUiConnectingMultipleTimes_updateNotReceived() = runTest {
        every { callParticipants.others } returns listOf()
        val callStateUiFlow: MutableStateFlow<CallStateUi> = MutableStateFlow(CallStateUi.Connecting)
        every { call.toCallStateUi() } returns callStateUiFlow
        every { call.toOtherDisplayNames() } returns MutableStateFlow(listOf())

        viewModel.uiState.first()
        advanceUntilIdle()
        callStateUiFlow.emit(CallStateUi.Reconnecting)
        advanceUntilIdle()
        callStateUiFlow.emit(CallStateUi.Connecting)
        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Reconnecting, viewModel.uiState.value.callStateUi)
    }

    @Test
    fun testCallStateNotShownStates_toCallStateUi() {
        Assert.assertEquals(null, CallStateUi.Disconnected.Ended.toTextRef(call))
        Assert.assertEquals(null, CallStateUi.Disconnected.Ended.HungUp.toTextRef(call))
        Assert.assertEquals(null, CallStateUi.Disconnected.Ended.Kicked("adminName").toTextRef(call))
    }

    @Test
    fun testCallStateErrorCallFailedStateUi() {
        Assert.assertEquals(TextRef.StringResource(R.string.kaleyra_call_failed), CallStateUi.Disconnected.Ended.Error.toTextRef(call))
    }
}
