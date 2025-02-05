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

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui

import com.kaleyra.video.State
import com.kaleyra.video.conversation.ChatParticipants
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ChatViewModel

    private val conference = mockk<ConferenceUI>()

    private val conversation = mockk<ConversationUI>() {
        every { state } returns MutableStateFlow(State.Connected)
    }

    private val call = mockk<CallUI>()

    private val chat = mockk<ChatUI>(relaxed = true) {
        every { id } returns "chatId"
    }

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        every { KaleyraVideo.conversation } returns mockk {
            every { state } returns MutableStateFlow(State.Connected)
        }
        viewModel = ChatViewModel { CollaborationViewModel.Configuration.Success(conference, conversation, mockk(), MutableStateFlow(mockk())) }
        every { conversation.create(any<String>()) } returns Result.success(chat)
        every { conversation.chats } returns MutableSharedFlow<List<ChatUI>>(replay = 1).apply { tryEmit(listOf(chat)) }
        every { conference.call } returns MutableStateFlow(call)
    }

    @Test
    fun getCall_getCallInstance() = runTest {
        assertEquals(viewModel.call.first(), call)
    }

    @Test
    fun setChatUser_getChatInstance() = runTest {
        advanceUntilIdle()
        assertEquals(viewModel.setChat("loggedUserId", "chatId"), viewModel.chat.first())
    }

    @Test
    fun getMessages_getMessagesInstance() = runTest {
        advanceUntilIdle()
        val messages = mockk<MessagesUI>()
        every { chat.messages } returns MutableStateFlow(messages)
        viewModel.setChat("loggedUserId", "chatId")
        assertEquals(viewModel.messages.first(), messages)
    }

    @Test
    fun getActions_getActionsInstance() = runTest {
        advanceUntilIdle()
        val actions = setOf(ChatUI.Button.Participants)
        every { chat.buttons } returns MutableStateFlow(actions)
        viewModel.setChat("loggedUserId", "chatId")
        assertEquals(viewModel.buttons.first(), actions)
    }

    @Test
    fun getParticipants_getParticipantsInstance() = runTest {
        advanceUntilIdle()
        val participants = mockk<ChatParticipants>()
        every { chat.participants } returns MutableStateFlow(participants)
        viewModel.setChat("loggedUserId", "chatId")
        assertEquals(viewModel.participants.first(), participants)
    }

}