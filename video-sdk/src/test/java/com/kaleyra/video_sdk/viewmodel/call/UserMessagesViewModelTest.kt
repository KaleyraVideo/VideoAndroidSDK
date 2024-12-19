package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserMessagesViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: UserMessagesViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val userMessages = MutableStateFlow<UserMessage>(RecordingMessage.Started)

    private val alertMessages = MutableStateFlow<Set<AlertMessage>>(setOf())

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.conference } returns mockk(relaxed = true)
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { CallUserMessagesProvider.userMessage } returns userMessages
        every { CallUserMessagesProvider.alertMessages } returns alertMessages
        viewModel = spyk(UserMessagesViewModel(
            accessibilityManager = null,
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        ))
    }

    @Test
    fun testCallUserMessagesProviderStarted() = runTest {
        advanceUntilIdle()
        verify { CallUserMessagesProvider.start(callMock, any()) }
    }

    @Test
    fun testUserMessageAdded() = runTest {
        advanceUntilIdle()
        viewModel.userMessage.first { it.value.contains(RecordingMessage.Started) }
    }

    @Test
    fun testAlertMessageAdded() = runTest {
        advanceUntilIdle()
        alertMessages.emit(setOf(AlertMessage.AutomaticRecordingMessage))
        advanceUntilIdle()
        viewModel.uiState.first { it.alertMessages.value.contains(AlertMessage.AutomaticRecordingMessage) }
    }

    @Test
    fun testUserMessageAutoDismissed() = runTest {
        advanceUntilIdle()
        userMessages.emit(RecordingMessage.Started)
        advanceUntilIdle()
        advanceTimeBy(16000L)
        viewModel.userMessage.first { it.value.isEmpty() }
    }

    @Test
    fun testUserMessageRemoved() = runTest {
        advanceUntilIdle()
        viewModel.userMessage.first { it.value.any { it is RecordingMessage }  }
        viewModel.dismiss(RecordingMessage.Started)
        advanceUntilIdle()
        viewModel.userMessage.first { it.value.isEmpty() }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}