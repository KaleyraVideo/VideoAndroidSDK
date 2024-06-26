package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.Company
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.viewmodel.FeedbackViewModel
import com.kaleyra.video_utils.MutableSharedStateFlow
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
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

    @Before
    fun setup() {
        with(participantMeMock) {
            every { feedback } returns participantFeedback
        }
        with(conferenceMock) {
            every { call } returns MutableSharedStateFlow<CallUI>(mockk(relaxed = true) {
                every { participants } returns MutableStateFlow(mockk(relaxed = true) {
                    every { me } returns participantMeMock
                })
            })
        }
        viewModel = spyk(FeedbackViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), mockk<Company>(), MutableStateFlow(mockk())) })
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testInitialState() = runTest {
        advanceUntilIdle()
        participantFeedback.first()
        val actual = participantFeedback.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun testSendUserFeedback() = runTest {
        advanceUntilIdle()
        val expected = CallParticipant.Me.Feedback(3, "comment")
        participantFeedback.first()
        viewModel.sendUserFeedback("comment", FeedbackUiRating.Neutral)
        val actual = participantFeedback.first()
        Assert.assertEquals(expected, actual)
    }
}