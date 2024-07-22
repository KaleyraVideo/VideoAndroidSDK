package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.Company
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.mapper.StreamMapper.doAnyOfMyStreamsIsLive
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.feedback.viewmodel.FeedbackViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FeedbackViewModel
    private val participantMeMock = mockk<CallParticipant.Me>()
    private val participantFeedback: MutableStateFlow<CallParticipant.Me.Feedback?> = MutableStateFlow(null)
    private val mockCall = mockk<CallUI>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(com.kaleyra.video_common_ui.mapper.StreamMapper)
        every { mockCall.doAnyOfMyStreamsIsLive() } returns MutableStateFlow(true)
        with(participantMeMock) {
            every { feedback } returns participantFeedback
        }
        with(mockCall) {
            every { withFeedback } returns true
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
    fun feedbackNotShown_participantFeedback_null() = runTest {
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        participantFeedback.first()
        val actual = participantFeedback.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun withFeedbackFalse_feedbackViewModelInitialState_FeedbackUiStateHidden() = runTest {
        every { mockCall.withFeedback } returns false
        every { mockCall.state } returns MutableStateFlow(Call.State.Connected)
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(FeedbackUiState.Hidden, viewModel.uiState.first())
    }

    @Test
    fun neverPublishedAStream_feedbackViewModelInitialState_FeedbackUiStateHidden() = runTest {
        every { mockCall.withFeedback } returns true
        every { mockCall.doAnyOfMyStreamsIsLive() } returns MutableStateFlow(false)
        every { mockCall.state } returns MutableStateFlow(Call.State.Connected)
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(FeedbackUiState.Hidden, viewModel.uiState.first())
    }

    @Test
    fun callIsNotEnded_feedbackViewModelInitialState_FeedbackUiStateHidden() = runTest {
        every { mockCall.state } returns MutableStateFlow(Call.State.Connected)
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(FeedbackUiState.Hidden, viewModel.uiState.first())
    }

    @Test
    fun callIsEnded_feedbackViewModelInitialState_FeedbackUiStateDisplay() = runTest {
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertEquals(true, viewModel.uiState.first() is FeedbackUiState.Display)
    }

    @Test
    fun callIsEnded_sendUserFeedback_myParticipantUserFeedbackUpdated() = runTest {
        viewModel = FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        val expected = CallParticipant.Me.Feedback(3, "comment")
        participantFeedback.first()
        viewModel.sendUserFeedback("comment", FeedbackUiRating.Neutral)
        val actual = participantFeedback.first()
        Assert.assertEquals(expected, actual)
    }
}