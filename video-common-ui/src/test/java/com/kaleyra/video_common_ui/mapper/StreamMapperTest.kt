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

package com.kaleyra.video_common_ui.mapper

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIAlone
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_common_ui.mapper.StreamMapper.doAnyOfMyStreamsIsLive
import com.kaleyra.video_common_ui.mapper.StreamMapper.doOthersHaveStreams
import com.kaleyra.video_common_ui.mapper.StreamMapper.mapStreamsToVideos
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StreamMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val zoomLevelFlow: MutableStateFlow<StreamView.ZoomLevel> = MutableStateFlow(StreamView.ZoomLevel.Fit)

    private val viewMock = mockk<VideoStreamView> {
        every { zoomLevel } returns zoomLevelFlow
    }

    private val uriMock = mockk<Uri>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    private val myStreamMock1 = mockk<Stream.Mutable>()

    private val myStreamMock2 = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val callParticipantsMock = mockk<CallParticipants>()

    @Before
    fun setUp() = runTest {
        mockkObject(ContactDetailsManager)
        zoomLevelFlow.emit(StreamView.ZoomLevel.Fit)

        // only needed for toCallStateUi function
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1, participantMock2)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock3) {
            every { id } returns "streamId3"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(myStreamMock1) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myStreamMock2) {
            every { id } returns "myStreamId2"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun meParticipantIsNull_doAnyOfMyStreamsIsLive_false() = runTest {
        every { callParticipantsMock.me } returns null
        val actual = callMock.doAnyOfMyStreamsIsLive().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun noStreamIsLive_doAnyOfMyStreamsIsLive_false() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Open)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Closed)
        
        val actual = callMock.doAnyOfMyStreamsIsLive().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun oneStreamIsLive_doAnyOfMyStreamsIsLive_true() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)
        
        val actual = callMock.doAnyOfMyStreamsIsLive().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun newLiveStreamAdded_doAnyOfMyStreamsIsLive_true() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)

        val myStreams = MutableStateFlow(listOf(myStreamMock1))
        every { participantMeMock.streams } returns myStreams
        val result = callMock.doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)

        myStreams.value = listOf(myStreamMock1, myStreamMock2)
        val new = result.first()
        Assert.assertEquals(true, new)
    }

    @Test
    fun newLiveStreamRemoved_doAnyOfMyStreamsIsLive_false() = runTest {
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns MutableStateFlow(Stream.State.Live)

        val myStreams = MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
        every { participantMeMock.streams } returns myStreams
        val result = callMock.doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(true, actual)

        myStreams.value = listOf(myStreamMock1)
        val new = result.first()
        Assert.assertEquals(false, new)
    }

    @Test
    fun doNotHaveStreams_doAnyOfMyStreamsIsLive_false() = runTest {
        every { participantMeMock.streams } returns MutableStateFlow(listOf())
        val result = callMock.doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun myStreamGoesLive_doAnyOfMyStreamsIsLive_true() = runTest {
        val state = MutableStateFlow<Stream.State>(Stream.State.Closed)
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns state

        val result = callMock.doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(false, actual)

        state.value = Stream.State.Live
        val new = result.first()
        Assert.assertEquals(true, new)
    }

    @Test
    fun myStreamNoMoreLive_doAnyOfMyStreamsIsLive_false() = runTest {
        val state = MutableStateFlow<Stream.State>(Stream.State.Live)
        every { myStreamMock1.state } returns MutableStateFlow(Stream.State.Closed)
        every { myStreamMock2.state } returns state

        val result = callMock.doAnyOfMyStreamsIsLive()
        val actual = result.first()
        Assert.assertEquals(true, actual)

        state.value = Stream.State.Closed
        val new = result.first()
        Assert.assertEquals(false, new)
    }

    @Test
    fun noParticipants_doOthersHaveStreams_false() = runTest {
        every { callParticipantsMock.others } returns listOf()
        
        val actual = callMock.doOthersHaveStreams().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun otherHaveNoStreams_doOthersHaveStreams_false() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { participantMock1.streams } returns MutableStateFlow(listOf())
        every { participantMock2.streams } returns MutableStateFlow(listOf())
        
        val actual = callMock.doOthersHaveStreams().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun othersHaveStreams_doOthersHaveStreams_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf())
        
        val actual = callMock.doOthersHaveStreams().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `I have live streams and other participants have streams, am i alone is false`() = runTest {
        
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.doOthersHaveStreams() } returns flowOf(true)
            every { callMock.doAnyOfMyStreamsIsLive() } returns flowOf(true)
        }
        val result = callMock.amIAlone()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun `I have no live stream, am I alone is true`() = runTest {
        
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.doOthersHaveStreams() } returns flowOf(true)
            every { callMock.doAnyOfMyStreamsIsLive() } returns flowOf(false)
        }
        val result = callMock.amIAlone()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `other participants does not have streams, am I alone is true`() = runTest {
        
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.doOthersHaveStreams() } returns flowOf(false)
            every { callMock.doAnyOfMyStreamsIsLive() } returns flowOf(true)
        }
        val result = callMock.amIAlone()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `call is connected and I am alone and there is only one in call participant, am I waiting others is true`() = runTest {
        
        mockkObject(StreamMapper)
        mockkObject(ParticipantMapper)
        with(StreamMapper) {
            every { callMock.amIAlone() } returns flowOf(true)
        }
        with(ParticipantMapper) {
            every { callMock.toInCallParticipants() } returns flowOf(listOf(participantMeMock))
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callMock.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun `call is not connected, am I waiting others is false`() = runTest {
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.amIAlone() } returns flowOf(true)
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val result = callMock.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun `I am no alone, am I waiting others is false`() = runTest {
        mockkObject(StreamMapper)
        with(StreamMapper) {
            every { callMock.amIAlone() } returns flowOf(false)
        }
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = callMock.amIWaitingOthers()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun emptyList_mapStreamsToVideos_emptyVideoList() = runTest {
        val streams = listOf<Stream>()
        val result = flowOf(streams).mapStreamsToVideos().first()
        assertEquals(listOf<Input.Video>(), result)
    }

    @Test
    fun streamList_mapStreamsToVideos_videoList() = runTest {
        val stream1 = object : Stream {
            override val audio: StateFlow<Input.Audio?> = MutableStateFlow(null)
            override val createdAt: Long = 123L
            override val id: String = "id1"
            override val state: StateFlow<Stream.State> = MutableStateFlow(Stream.State.Open)
            override val video: StateFlow<Input.Video?> = MutableStateFlow(videoMock)
            override fun close() = Unit
            override fun open() = Unit
        }
        val stream2 = object : Stream {
            override val audio: StateFlow<Input.Audio?> = MutableStateFlow(null)
            override val id: String = "id2"
            override val createdAt: Long = 123L
            override val state: StateFlow<Stream.State> = MutableStateFlow(Stream.State.Open)
            override val video: StateFlow<Input.Video?> = MutableStateFlow(myVideoMock)
            override fun close() = Unit
            override fun open() = Unit
        }
        val streams = listOf(stream1, stream2,)
        val result = flowOf(streams).mapStreamsToVideos().first()
        assertEquals(listOf<Input.Video?>(videoMock, myVideoMock), result)
    }

    @Test
    fun streamWithNullVideo_mapStreamsToVideos_listOfNull() = runTest {
        val stream3 = object : Stream {
            override val audio: StateFlow<Input.Audio?> = MutableStateFlow(null)
            override val createdAt: Long = 123L
            override val id: String = "id3"
            override val state: StateFlow<Stream.State> = MutableStateFlow(Stream.State.Open)
            override val video: StateFlow<Input.Video?> = MutableStateFlow(null)
            override fun close() = Unit
            override fun open() = Unit
        }
        val streams = listOf(stream3)
        val result = flowOf(streams).mapStreamsToVideos().first()
        assertEquals(listOf<Input.Video?>(null), result)
    }
}