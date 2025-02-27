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

package com.kaleyra.video_sdk.viewmodel.chat

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video.State
import com.kaleyra.video.User
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video.conversation.ChatParticipant
import com.kaleyra.video.conversation.Message
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.MessagesUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.callMock
import com.kaleyra.video_sdk.Mocks.chatMock
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.Mocks.conversationMock
import com.kaleyra.video_sdk.Mocks.groupChatParticipantsMock
import com.kaleyra.video_sdk.Mocks.messagesUIMock
import com.kaleyra.video_sdk.Mocks.myParticipantMock
import com.kaleyra.video_sdk.Mocks.oneToOneChatParticipantsFlow
import com.kaleyra.video_sdk.Mocks.otherParticipantMock
import com.kaleyra.video_sdk.Mocks.otherParticipantMock2
import com.kaleyra.video_sdk.Mocks.otherTodayReadMessage
import com.kaleyra.video_sdk.Mocks.otherTodayUnreadMessage
import com.kaleyra.video_sdk.Mocks.otherTodayUnreadMessage2
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantState
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PhoneChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PhoneChatViewModel

    private val myUri = mockk<Uri>()

    private val otherUri = mockk<Uri>()

    private val otherUri2 = mockk<Uri>()

    private val chatParticipantsFlow = MutableStateFlow(oneToOneChatParticipantsFlow)

    private val messagesFlow = MutableStateFlow(messagesUIMock)

    private val connectedUserFlow = MutableStateFlow<User?>(mockk())

    private val chatMockState: MutableStateFlow<Chat.State> = MutableStateFlow(Chat.State.Active)

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connected)
        every { conversationMock.state } returns MutableStateFlow(State.Connected)
        every { KaleyraVideo.conversation } returns conversationMock
        every { KaleyraVideo.conference } returns conferenceMock
        mockkObject(ContactDetailsManager)
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { chatMock.state } returns chatMockState
        with(conversationMock) {
            every { chats } returns MutableStateFlow(listOf(chatMock))
            every { create(any<String>()) } returns Result.success(chatMock)
            every { create(any<List<String>>(), any<String>()) } returns Result.success(chatMock)
        }
        with(messagesUIMock) {
            every { list } returns listOf(otherTodayUnreadMessage, otherTodayReadMessage)
            every { other } returns listOf(otherTodayUnreadMessage, otherTodayReadMessage)
        }
        with(myParticipantMock) {
            every { userId } returns "myUserId"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("myUsername")
            every { combinedDisplayImage } returns flowOf(myUri)
        }
        with(otherParticipantMock) {
            every { userId } returns "otherUserId"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("otherDisplayName")
            every { combinedDisplayImage } returns flowOf(otherUri)
        }
        with(otherParticipantMock2) {
            every { userId } returns "otherUserId2"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("otherDisplayName2")
            every { combinedDisplayImage } returns flowOf(otherUri2)
        }
        with(chatMock) {
            every { id } returns "chatId"
            every { isGroup } returns true
            every { messages } returns messagesFlow
            every { unreadMessagesCount } returns MutableStateFlow(5)
            every { participants } returns chatParticipantsFlow
            every { actions } returns MutableStateFlow(
                setOf(
                    ChatUI.Action.CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    ChatUI.Action.CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            )
            every { buttons } returns MutableStateFlow(
                setOf(
                    ChatUI.Button.Call(callType = Call.Type.audioUpgradable()),
                    ChatUI.Button.Call(callType = Call.Type.audioVideo())
                )
            )
        }
        chatParticipantsFlow.value = oneToOneChatParticipantsFlow
        viewModel = spyk(PhoneChatViewModel {
            Configuration.Success(
                conferenceMock,
                conversationMock,
                mockk(relaxed = true),
                connectedUserFlow
            )
        })
        TestScope().launch {
            viewModel.setChat("userId", "chatId")
        }
    }

    @Test
    fun testChatUiState_groupChat_otherParticipantsStateUpdated() = runTest {
        chatParticipantsFlow.value = groupChatParticipantsMock
        val uiState = viewModel.uiState.first() as? ChatUiState.Group
        val actual = uiState?.participantsState
        assertEquals(null, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as? ChatUiState.Group
        val newActual = newUiState?.participantsState
        val newExpected = ChatParticipantsState(online = ImmutableList(listOf("otherDisplayName", "otherDisplayName2")))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testChatUiState_groupChat_participantsDetailsUpdated() = runTest {
        chatParticipantsFlow.value = groupChatParticipantsMock
        val uiState = viewModel.uiState.first() as? ChatUiState.Group
        val actual = uiState?.participantsDetails
        assertEquals(null, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as ChatUiState.Group
        val newActual = newUiState.participantsDetails.value
        val newExpected = hashMapOf(
            "myUserId" to ChatParticipantDetails("myUsername", ImmutableUri(myUri), flowOf(ChatParticipantState.Online)),
            "otherUserId" to ChatParticipantDetails("otherDisplayName", ImmutableUri(otherUri), flowOf(ChatParticipantState.Online)),
            "otherUserId2" to ChatParticipantDetails("otherDisplayName2", ImmutableUri(otherUri2), flowOf(ChatParticipantState.Online))
        )
        newActual.forEach { (key, entry) ->
            areChatParticipantDetailsEquals(newExpected[key]!!, entry)
        }
    }

    @Test
    fun testChatUiState_oneToOneChat_recipientDetailsUpdates() = runTest {
        chatParticipantsFlow.value = oneToOneChatParticipantsFlow
        every { chatMock.isGroup } returns false
        val uiState = viewModel.uiState.first() as ChatUiState.OneToOne
        val actual = uiState.recipientDetails
        val expected = ChatParticipantDetails()
        areChatParticipantDetailsEquals(expected, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as ChatUiState.OneToOne
        val newActual = newUiState.recipientDetails
        val newExpected = ChatParticipantDetails(
            username = "otherDisplayName",
            image = ImmutableUri(otherUri),
            state = flowOf(ChatParticipantState.Online)
        )
        areChatParticipantDetailsEquals(newExpected, newActual)
    }

    @Test
    fun testChatUiState_isInCallUpdated() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val current = viewModel.uiState.first().isInCall
        Assert.assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isInCall
        Assert.assertEquals(true, new)
    }

    @Test
    fun testChatUiState_actionsUpdated() = runTest {
        val current = viewModel.uiState.first().actions
        Assert.assertEquals(ImmutableSet<ChatAction>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().actions.value
        assert(new.filterIsInstance<ChatAction.AudioUpgradableCall>().isNotEmpty())
        assert(new.filterIsInstance<ChatAction.VideoCall>().isNotEmpty())
    }

    @Test
    fun testChatUiState_conversationItemsUpdated() = runTest {
        val current = viewModel.uiState.first().conversationState.conversationItems
        Assert.assertEquals(null, current)
        advanceUntilIdle()
        val unreadMessage = findFirstUnreadMessageId(messagesUIMock) { mockk() }

        val actual = viewModel.uiState.first().conversationState.conversationItems
        val expected = listOf(otherTodayUnreadMessage, otherTodayReadMessage).mapToConversationItems(unreadMessage)
        Assert.assertEquals(ImmutableList(expected), actual)
    }

    @Test
    fun `unread messages items is removed if a message is sent`() = runTest {
        val current = viewModel.uiState.first().conversationState.conversationItems
        Assert.assertEquals(null, current)
        advanceUntilIdle()
        val unreadMessage = findFirstUnreadMessageId(messagesUIMock) { mockk() }

        val actual = viewModel.uiState.first().conversationState.conversationItems
        val expected = listOf(otherTodayUnreadMessage, otherTodayReadMessage).mapToConversationItems(unreadMessage)
        Assert.assertEquals(ImmutableList(expected), actual)

        viewModel.sendMessage("text")

        val newMessagesUIMock = mockk<MessagesUI>(relaxed = true)
        every { newMessagesUIMock.list } returns messagesUIMock.list
        messagesFlow.value =  newMessagesUIMock

        advanceUntilIdle()
        val newActual = viewModel.uiState.first().conversationState.conversationItems
        val newExpected = messagesUIMock.list.mapToConversationItems()
        Assert.assertEquals(ImmutableList(newExpected), newActual)
    }

    @Test
    fun testChatUiState_connectionStateUpdated() = runTest {
        every { conversationMock.state } returns MutableStateFlow(State.Connecting)
        val current = viewModel.uiState.first().connectionState
        Assert.assertEquals(ConnectionState.Unknown, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().connectionState
        Assert.assertEquals(ConnectionState.Connecting, new)
    }

    @Test
    fun testChatUiState_isUserConnectedUpdated() = runTest {
        connectedUserFlow.value = mockk()
        val current = viewModel.uiState.first().isUserConnected
        Assert.assertEquals(true, current)

        connectedUserFlow.value = null
        advanceUntilIdle()
        val new = viewModel.uiState.first().isUserConnected
        Assert.assertEquals(false, new)
    }

    @Test
    fun testChatUiState_unreadMessagesUpdated() = runTest {
        val current = viewModel.uiState.first().conversationState.unreadMessagesCount
        Assert.assertEquals(0, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().conversationState.unreadMessagesCount
        Assert.assertEquals(5, new)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }

    @Test
    fun testSendMessage() = runTest {
        advanceUntilIdle()
        val text = "text"
        viewModel.sendMessage(text)
        verify { chatMock.add(match { it is Message.Content.Text && it.message == text }) }
    }

    @Test
    fun testTyping() = runTest {
        advanceUntilIdle()
        viewModel.typing()
        verify { oneToOneChatParticipantsFlow.me!!.typing() }
    }

    @Test
    fun testFetchMessages() = runTest {
        advanceUntilIdle()
        mockkObject(MessagesMapper)
        val fetchedMessagesUIMock = mockk<MessagesUI>()
        every { fetchedMessagesUIMock.list } returns listOf(otherTodayUnreadMessage2, otherTodayUnreadMessage, otherTodayReadMessage)
        coEvery { chatMock.fetch(any()) } coAnswers {
            delay(2000L)
            messagesFlow.value = fetchedMessagesUIMock
            Result.success(mockk(relaxed = true))
        }

        val isFetchingValues = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.map { it.conversationState.isFetching }.toList(isFetchingValues)
        }

        viewModel.fetchMessages()
        advanceUntilIdle()

        coVerify { chatMock.fetch(any()) }
        assertEquals(false, isFetchingValues[0])
        assertEquals(true, isFetchingValues[1])
        assertEquals(false, isFetchingValues[2])
        val actualItems = viewModel.uiState.first().conversationState.conversationItems?.value
        val expectedItems = fetchedMessagesUIMock.list.mapToConversationItems(otherTodayUnreadMessage.id)
        assertEquals(expectedItems, actualItems)
    }

    @Test
    fun testOnMessageScrolled() = runTest {
        advanceUntilIdle()
        val message = mockk<com.kaleyra.video_sdk.chat.conversation.model.Message.OtherMessage>()
        every { message.id } returns otherTodayUnreadMessage.id
        viewModel.onMessageScrolled(ConversationItem.Message(message))
        verify { otherTodayUnreadMessage.markAsRead() }
    }

    @Test
    fun testOnAllMessagesScrolled() = runTest {
        advanceUntilIdle()
        viewModel.onAllMessagesScrolled()
        verify { otherTodayUnreadMessage.markAsRead() }
    }

    @Test
    fun testShowCall() = runTest {
        advanceUntilIdle()
        viewModel.showCall()
        verify { callMock.show() }
    }

    @Test
    fun testGetLoggedUserId() = runTest {
        val user = mockk<User> {
            every { userId } returns "customUserId"
        }
        connectedUserFlow.value = user
        advanceUntilIdle()
        val userId = viewModel.getLoggedUserId()
        assertEquals(user.userId, userId)
    }

    @Test
    fun testChatDeletedUiStateUpdated() = runTest {
        chatMockState.emit(Chat.State.Closed.Companion)
        advanceUntilIdle()

        val uiState = viewModel.uiState.first()

        Assert.assertEquals(true, uiState.isDeleted)
    }

    @Test
    fun testChatCreationFailedUiStateUpdated() = runTest {
        chatMockState.emit(Chat.State.Closed.Error.InvalidParticipants)
        advanceUntilIdle()

        val uiState = viewModel.uiState.first()

        Assert.assertEquals(true, uiState.hasFailedCreation)
    }

    private suspend fun areChatParticipantDetailsEquals(expected: ChatParticipantDetails,  actual: ChatParticipantDetails) {
        assertEquals(expected.username, actual.username)
        assertEquals(expected.image, actual.image)
        assertEquals(expected.state.firstOrNull(), actual.state.firstOrNull())
    }
}
