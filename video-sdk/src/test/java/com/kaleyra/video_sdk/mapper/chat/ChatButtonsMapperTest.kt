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
package com.kaleyra.video_sdk.mapper.chat

import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.*
import com.kaleyra.video_common_ui.*
import com.kaleyra.video_sdk.Mocks.callMock
import com.kaleyra.video_sdk.Mocks.callState
import com.kaleyra.video_sdk.Mocks.conversationMock
import com.kaleyra.video_sdk.Mocks.conversationState
import com.kaleyra.video_sdk.Mocks.messagesUIMock
import com.kaleyra.video_sdk.Mocks.myMessageMock
import com.kaleyra.video_sdk.Mocks.otherParticipantEvents
import com.kaleyra.video_sdk.Mocks.otherParticipantState
import com.kaleyra.video_sdk.Mocks.otherYesterdayUnreadMessage
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.mapper.ChatButtonsMapper.mapToChatActions
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatButtonsMapperTest {

    @Before
    fun setUp() {
        every { callMock.state } returns callState
        every { conversationMock.state } returns conversationState
        every { messagesUIMock.my } returns listOf(myMessageMock)
        every { messagesUIMock.other } returns listOf(otherYesterdayUnreadMessage)
        every { messagesUIMock.list } returns messagesUIMock.other + messagesUIMock.my
    }

    @After
    fun tearDown() {
        unmockkAll()
        callState.value = Call.State.Connected
        conversationState.value = State.Connected
        otherParticipantState.value = ChatParticipant.State.Invited
        otherParticipantEvents.value = ChatParticipant.Event.Typing.Idle
    }

    @Test
    fun emptyActions_mapToUiActions_emptyUiActions() {
        assertEquals(setOf<ChatUI.Action>().mapToChatActions { preferredType, maxDuration, recordingType -> }, setOf<ChatAction>())
    }

    @Test
    fun allActions_mapToUiActions_allUiActions() {
        val actions = ChatUI.Action.all
        val result = actions.mapToChatActions { preferredType, maxDuration, recordingType -> }
        assert(result.filterIsInstance<ChatAction.AudioCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.AudioUpgradableCall>().isNotEmpty())
        assert(result.filterIsInstance<ChatAction.VideoCall>().isNotEmpty())
    }

    @Test
    fun allActions_mapToUiActions_onClickCalledCallOptionsReceived() {
        val actions = setOf(
            ChatUI.Action.CreateCall(Call.PreferredType.audioOnly(), 1, Call.Recording.Type.Never),
            ChatUI.Action.CreateCall(Call.PreferredType.audioUpgradable(), 2, Call.Recording.Type.OnDemand),
            ChatUI.Action.CreateCall(Call.PreferredType.audioVideo(), 3, Call.Recording.Type.OnConnect)
        )
        var duration = 0L
        var recType: Call.Recording.Type? = null
        val result = actions.mapToChatActions { preferredType, maxDuration, recordingType ->
            duration = maxDuration ?: 0
            recType = recordingType
        }

        result.first { it is ChatAction.AudioCall }.onClick()
        assertEquals(1, duration)
        assertEquals(Call.Recording.Type.Never, recType)
        result.first { it is ChatAction.AudioUpgradableCall }.onClick()
        assertEquals(2, duration)
        assertEquals(Call.Recording.Type.OnDemand, recType)
        result.first { it is ChatAction.VideoCall }.onClick()
        assertEquals(3, duration)
        assertEquals(Call.Recording.Type.OnConnect, recType)
    }
}
