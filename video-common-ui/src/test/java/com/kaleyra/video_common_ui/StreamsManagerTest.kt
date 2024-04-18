package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.StreamsManager
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StreamsManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val context = RuntimeEnvironment.getApplication()

    private val callMock = mockk<Call>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantMock = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val streamMock1 = mockk<Stream>(relaxed = true)

    private val streamMock2 = mockk<Stream>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val videoMock1 = mockk<Input.Video>(relaxed = true)

    private val videoMock2 = mockk<Input.Video>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns context
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.list } returns listOf(meMock, participantMock)
        with(meMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
        }
        with(participantMock) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1))
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock2))
        }
        with(myStreamMock) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock1)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock2)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(null)
        }
        with(videoMock1) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(null)
        }
        with(videoMock2) {
            every { id } returns "videoId2"
            every { view } returns MutableStateFlow(null)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBind() = runTest {
        val streamManager = spyk(StreamsManager(backgroundScope))
        streamManager.bind(callMock)
        verifyOrder {
            streamManager.stop()
            streamManager.handleStreamsOpening(callMock)
            streamManager.handleStreamsVideoView(callMock)
        }
    }

    @Test
    fun testStop() = runTest {
        val streamManager = spyk(StreamsManager(this))
        streamManager.bind(callMock)
        streamManager.stop()
    }

    @Test
    fun handleStreamsVideoView_viewsAreSet() = runTest(UnconfinedTestDispatcher()) {
        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsVideoView(callMock)
        Assert.assertNotEquals(null, myVideoMock.view.value)
        Assert.assertNotEquals(null, videoMock1.view.value)
    }

    @Test
    fun handleStreamsVideoViewWhenAParticipantVideoIsNull() = runTest(UnconfinedTestDispatcher()) {
        every { myStreamMock.video } returns MutableStateFlow(null)
        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsVideoView(callMock)
        Assert.assertNotEquals(null, videoMock1.view.value)
    }

    @Test
    fun handleStreamsVideoView_viewSetOnNewParticipant() = runTest(UnconfinedTestDispatcher()) {
        val participants = MutableStateFlow(participantsMock)
        every { callMock.participants } returns participants
        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsVideoView(callMock)
        Assert.assertNotEquals(null, myVideoMock.view.value)
        Assert.assertNotEquals(null, videoMock1.view.value)

        val newParticipantsMock = mockk<CallParticipants>()
        every { newParticipantsMock.list } returns listOf(meMock, participantMock, participantMock2)
        participants.value = newParticipantsMock
        Assert.assertNotEquals(null, videoMock2.view.value)
    }

    @Test
    fun handleStreamsVideoView_viewSetOnNewStream() = runTest(UnconfinedTestDispatcher()) {
        val streams = MutableStateFlow(listOf(streamMock1))
        every { participantMock.streams } returns streams
        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsVideoView(callMock)
        Assert.assertNotEquals(null, myVideoMock.view.value)
        Assert.assertNotEquals(null, videoMock1.view.value)

        streams.value = listOf(streamMock1, streamMock2)
        Assert.assertNotEquals(null, videoMock2.view.value)
    }

    @Test
    fun handleStreamsVideoView_viewSetOnNewVideo() = runTest(UnconfinedTestDispatcher()) {
        val video = MutableStateFlow(videoMock1)
        every { streamMock1.video } returns video
        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsVideoView(callMock)
        Assert.assertNotEquals(null, myVideoMock.view.value)
        Assert.assertNotEquals(null, videoMock1.view.value)

        video.value = videoMock2
        Assert.assertNotEquals(null, videoMock2.view.value)
    }

    @Test
    fun handleStreamsOpening_myStreamsAreOpened() = runTest(UnconfinedTestDispatcher()) {
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { participantsMock.list } returns listOf(meMock)

        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsOpening(callMock)

        verify { myStreamMock.open() }
    }

    @Test
    fun handleStreamsOpening_otherStreamsAreOpened() = runTest(UnconfinedTestDispatcher()) {
        every { participantMock.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock2))
        every { participantsMock.list } returns listOf(participantMock, participantMock2)

        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsOpening(callMock)

        verify { streamMock1.open() }
        verify { streamMock2.open() }
    }

    @Test
    fun handleStreamsOpening_openIsCalledOnNewStreams() = runTest(UnconfinedTestDispatcher()) {
        val myStreamList = MutableStateFlow(listOf<Stream.Mutable>())
        val otherStreamList = MutableStateFlow(listOf<Stream>())
        every { meMock.streams } returns myStreamList
        every { participantMock2.streams } returns otherStreamList
        every { participantsMock.list } returns listOf(meMock, participantMock2)

        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsOpening(callMock)

        myStreamList.value = listOf(myStreamMock)
        otherStreamList.value = listOf(streamMock2)
        verify { myStreamMock.open() }
        verify { streamMock2.open() }
    }

    @Test
    fun handleStreamsOpening_openIsCalledOnNewParticipantStreams() = runTest(UnconfinedTestDispatcher()) {
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        val callParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(meMock)
        }
        val participantsFlow = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participantsFlow

        val streamManager = StreamsManager(backgroundScope)
        streamManager.handleStreamsOpening(callMock)
        verify { myStreamMock.open() }

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(meMock, participantMock2)
        }
        participantsFlow.value = newCallParticipantsMock
        verify { streamMock1.open() }
        verify { streamMock2.open() }
    }
}