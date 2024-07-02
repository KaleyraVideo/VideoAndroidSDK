package com.kaleyra.video_sdk.ui.call.usermessage.viemodel

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserMessagesViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: UserMessagesViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val userMessages = MutableStateFlow<UserMessage>(RecordingMessage.Started)

    private val alertMessags = MutableStateFlow<List<AlertMessage>>(listOf())

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { CallUserMessagesProvider.userMessage } returns userMessages
        every { CallUserMessagesProvider.alertMessages } returns alertMessags
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
        Assert.assertEquals(ImmutableList(listOf(RecordingMessage.Started)), viewModel.userMessage.first())
    }

    @Test
    fun testAlertMessageAdded() = runTest {
        alertMessags.emit(listOf(AlertMessage.AutomaticRecordingMessage))
        advanceUntilIdle()
        Assert.assertEquals(ImmutableList<AlertMessage>(listOf(AlertMessage.AutomaticRecordingMessage)), viewModel.uiState.first().alertMessages)
    }

    @Test
    fun testUserMessageAutoDismissed() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(ImmutableList(listOf(RecordingMessage.Started)), viewModel.userMessage.first())
        advanceTimeBy(15000L)
        Assert.assertEquals(ImmutableList<UserMessage>(listOf()), viewModel.userMessage.first())
    }

    @Test
    fun testUserMessageRemoved() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(ImmutableList(listOf(RecordingMessage.Started)), viewModel.userMessage.first())
        viewModel.dismiss(RecordingMessage.Started)
        Assert.assertEquals(ImmutableList<UserMessage>(listOf()), viewModel.userMessage.first())
    }
}