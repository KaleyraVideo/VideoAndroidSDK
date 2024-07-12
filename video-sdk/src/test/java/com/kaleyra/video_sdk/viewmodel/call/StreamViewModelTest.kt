package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.mapper.ParticipantMapper
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.StreamMapper
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.ui.mockkSuccessfulConfiguration
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val streamMock1 = StreamUi(id = "streamId1", username = "username")

    private val streamMock2 = StreamUi(id = "streamId2", username = "username")

    private val streamMock3 = StreamUi(id = "streamId3", username = "username")

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    @Before
    fun setUp() {
        mockkObject(ParticipantMapper)
        mockkObject(StreamMapper)
        mockkObject(CallStateMapper)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test streams updated after a debounce time if there is only one stream, there are still participants in call and the call is connected`() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1))

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        val current = viewModel.uiState.first().streams.value
        assertEquals(listOf<StreamUi>(), current)

        advanceUntilIdle()

        val new = viewModel.uiState.first().streams.value
        assertEquals(listOf(streamMock1), new)
    }

    @Test
    fun `test streams cleaned on call ended`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        advanceUntilIdle()

        val current = viewModel.uiState.first().streams.value
        assertEquals(streams, current)

        callState.value = CallStateUi.Disconnected.Ended

        advanceUntilIdle()

        val new = viewModel.uiState.first().streams.value
        assertEquals(listOf<String>(), new)
    }

    @Test
    fun `test streams updated after default debounce if there is more than one participant in call`() = runTest {
        val streams = listOf(streamMock1)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        val current = viewModel.uiState.first().streams.value
        assertEquals(listOf<StreamUi>(), current)

        advanceTimeBy(StreamViewModel.DEFAULT_DEBOUNCE_MILLIS + 1)
        val new = viewModel.uiState.first().streams.value
        assertEquals(streams, new)
    }

    @Test
    fun `test streams updated immediately if the call is not connected`() = runTest {
        val streams = listOf(streamMock1)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        val current = viewModel.uiState.first().streams.value
        assertEquals(listOf<StreamUi>(), current)

        advanceTimeBy(StreamViewModel.DEFAULT_DEBOUNCE_MILLIS + 1)
        val new = viewModel.uiState.first().streams.value
        assertEquals(streams, new)
    }

    @Test
    fun `test streams updated immediately if there are more than one stream`() = runTest {
        val streams = listOf(streamMock1, streamMock2)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        val current = viewModel.uiState.first().streams.value
        assertEquals(listOf<StreamUi>(), current)

        advanceTimeBy(StreamViewModel.DEFAULT_DEBOUNCE_MILLIS + 1)
        val new = viewModel.uiState.first().streams.value
        assertEquals(streams, new)
    }

    @Test
    fun setExistentFullscreenStream_succeeds() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        viewModel.fullscreen(streamMock1.id)

        assertEquals(streamMock1, viewModel.uiState.value.fullscreenStream)
    }

    @Test
    fun setNonExistentFullscreenStream_fails() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        viewModel.fullscreen(streamMock2.id)

        assertEquals(null, viewModel.uiState.value.fullscreenStream)
    }

    @Test
    fun setFullscreenStreamNull_succeeds() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.fullscreen(streamMock1.id)
        assertEquals(streamMock1, viewModel.uiState.value.fullscreenStream)

        viewModel.fullscreen(null)
        assertEquals(null, viewModel.uiState.value.fullscreenStream)
    }

    @Test
    fun fullscreenStreamRemovedFromStreams_fullscreenStreamIsNull() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toStreamsUi() } returns streams

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        advanceUntilIdle()
        viewModel.fullscreen(streamMock1.id)

        val actual = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1, actual)

        streams.value = listOf(streamMock2)
        advanceUntilIdle()

        val new = viewModel.uiState.first().fullscreenStream
        assertEquals(null, new)
    }

    @Test
    fun `fullscreen stream is set to null on reconnecting call state`() = runTest {
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })

        advanceUntilIdle()
        viewModel.fullscreen(streamMock1.id)

        val actual = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1, actual)

        callState.value = CallStateUi.Reconnecting
        advanceUntilIdle()
        val new = viewModel.uiState.first().fullscreenStream
        assertEquals(null, new)
    }

    @Test
    fun setNonExistentPinnedStream_fails() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        val isPinned = viewModel.pin(streamMock1.id)

        assertEquals(false, isPinned)
        assertEquals(listOf<StreamUi>(), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun setExistentPinnedStream_succeeds() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        val isPinned = viewModel.pin(streamMock1.id)

        assertEquals(true, isPinned)
        assertEquals(streamMock1, viewModel.uiState.value.pinnedStreams.value[0])
    }

    @Test
    fun testPinOverMaxPinnedStreams() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        viewModel.setMaxPinnedStreams(1)

        advanceUntilIdle()
        viewModel.pin(streamMock1.id)
        val isPinned = viewModel.pin(streamMock2.id)

        assertEquals(false, isPinned)
        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun testMaxPinnedStreams() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)
        val isPinned = viewModel.pin(streamMock3.id)

        assertEquals(false, isPinned)
        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun testUnpin() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()
        viewModel.pin(streamMock1.id)

        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)

        viewModel.unpin(streamMock1.id)

        assertEquals(listOf<StreamUi>(), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `local screen share is added as first pinned stream`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = true, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()
        viewModel.pin(streamMock1.id)

        streams.value = listOf(streamMock1, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock, streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `first pinned stream is removed if a local screen share is added and max pinned limit is reached`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = true, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()
        viewModel.setMaxPinnedStreams(2)
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)

        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)
        streams.value = listOf(streamMock1, streamMock2, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `clean pinned stream if they were removed from the stream list`() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)

        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)

        streams.value = listOf(streamMock2)
        advanceUntilIdle()

        assertEquals(listOf(streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `previous pinned stream is updated on stream update`() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)

        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)

        val updatedStreamMock1 = streamMock1.copy(isMine = true)
        streams.value = listOf(updatedStreamMock1, streamMock2)
        advanceUntilIdle()

        assertEquals(listOf(updatedStreamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun noScreenShareInputAvailable_stopScreenShareFail() = runTest {
        val cameraInput = mockk<Input.Video.Camera.Internal> {
            every { enabled } returns MutableStateFlow(true)
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(true)
        }
        val availableInputs = setOf(cameraInput, usbInput)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val isStopped = viewModel.tryStopScreenShare()
        assertEquals(false, isStopped)
    }

    @Test
    fun noScreenShareInputActive_stopScreenShareFail() = runTest {
        val screenShareInput = mockk<Input.Video.Application> {
            every { enabled } returns MutableStateFlow(false)
            every { tryDisable() } returns true
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(true)
        }
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns SCREEN_SHARE_STREAM_ID
        }
        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
        }
        val participants = mockk<CallParticipants>(relaxed = true) {
            every { me } returns meMock
        }
        val availableInputs = setOf(screenShareInput, usbInput)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
        every { callMock.participants } returns MutableStateFlow(participants)

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()

        val isStopped = viewModel.tryStopScreenShare()
        verify(exactly = 0) { screenShareInput.tryDisable() }
        assertEquals(false, isStopped)
    }

    @Test
    fun noScreenShareStream_stopScreenShareFail() = runTest {
        val screenShareInput = mockk<Input.Video.Application> {
            every { enabled } returns MutableStateFlow(true)
            every { tryDisable() } returns true
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(true)
        }
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns "streamId"
        }
        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
        }
        val participants = mockk<CallParticipants>(relaxed = true) {
            every { me } returns meMock
        }
        val availableInputs = setOf(screenShareInput, usbInput)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
        every { callMock.participants } returns MutableStateFlow(participants)

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        advanceUntilIdle()

        val isStopped = viewModel.tryStopScreenShare()
        assertEquals(false, isStopped)
    }

    @Test
    fun deviceScreenShareActive_stopScreenShareSuccess() {
        val screenShareVideoMock = spyk<Input.Video.Screen>()
        testTryStopScreenShare(screenShareVideoMock)
        verify(exactly = 1) { screenShareVideoMock.dispose() }
    }

    @Test
    fun appScreenShareActive_stopScreenShareSuccess() {
        val screenShareVideoMock = spyk<Input.Video.Application>()
        testTryStopScreenShare(screenShareVideoMock)
        verify(exactly = 1) { screenShareVideoMock.tryDisable() }
    }

    private fun testTryStopScreenShare(screenShareVideoMock: Input.Video) = runTest {
        every { screenShareVideoMock.enabled } returns MutableStateFlow(true)
        every { screenShareVideoMock.tryDisable() } returns true
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns SCREEN_SHARE_STREAM_ID
        }
        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
        }
        val participants = mockk<CallParticipants>(relaxed = true) {
            every { me } returns meMock
        }
        val availableInputs = setOf(screenShareVideoMock, mockk<Input.Video.Screen>(), mockk<Input.Video.Application>(), mockk<Input.Video.Camera>())
        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
        every { callMock.participants } returns MutableStateFlow(participants)

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val isStopped = viewModel.tryStopScreenShare()
        verify(exactly = 1) { meMock.removeStream(myStreamMock) }
        assertEquals(true, isStopped)
    }
}
