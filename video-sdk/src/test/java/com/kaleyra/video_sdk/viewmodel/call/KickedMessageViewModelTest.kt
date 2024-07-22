@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.Company
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.kicked.viewmodel.KickedMessageViewModel
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_utils.MutableSharedStateFlow
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class KickedMessageViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: KickedMessageViewModel
    private val participantMeMock = mockk<CallParticipant.Me>()
    private val participantFeedback: MutableStateFlow<CallParticipant.Me.Feedback?> = MutableStateFlow(null)
    private val mockCall = mockk<CallUI>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(CallStateMapper)
        with(participantMeMock) {
            every { feedback } returns participantFeedback
        }
        with(mockCall) {
            every { state } returns MutableStateFlow(Call.State.Disconnected.Ended)
            every { participants } returns MutableStateFlow(mockk(relaxed = true) {
                every { me } returns participantMeMock
            })
        }
        with(conferenceMock) {
            every { call } returns MutableSharedStateFlow<CallUI>(mockCall)
        }
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun callEndedNoKick_viewModelUiState_kickedMessageUiStateHidden() = runTest {
        every { mockCall.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Companion)
        every { mockCall.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Companion)
        viewModel = KickedMessageViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(KickedMessageUiState.Hidden, viewModel.uiState.first())
    }

    @Test
    fun callEndedKicked_viewModelUiState_kickedMessageUiStateDisplay() = runTest {
        every { mockCall.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked(userId = "admin"))
        every { mockCall.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected.Ended.Kicked(adminName = "admin"))
        viewModel = KickedMessageViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(KickedMessageUiState.Display(adminName = "admin"), viewModel.uiState.first())
    }
}
