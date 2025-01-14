package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.AudioMapper
import com.kaleyra.video_sdk.call.mapper.AudioMapper.toMyCameraStreamAudioUi
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.mapper.StreamMapper
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper
import com.kaleyra.video_sdk.call.mapper.VideoMapper.toMyCameraVideoUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
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
        mockkObject(com.kaleyra.video_common_ui.mapper.ParticipantMapper)
        mockkObject(com.kaleyra.video_sdk.call.mapper.ParticipantMapper)
        mockkObject(StreamMapper)
        mockkObject(CallStateMapper)
        mockkObject(VideoMapper)
        mockkObject(AudioMapper)
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `stream preview set on call ringing`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Ringing)
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview set on call dialing`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Dialing)
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview set on call ringing remotely`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock),
            isStartingWithVideo = false
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview set on call reconnecting`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Reconnecting)
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `handle empty list for other display names and images`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf())
            every { toOtherDisplayImages() } returns flowOf(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview reset to null after pre call state is ended and streams count is more than 1`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.RingingRemotely)
        val streams = MutableStateFlow(listOf(streamMock1))
        with(callMock) {
            every { toCallStateUi() } returns callState
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { toStreamsUi() } returns streams
            every { toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock),
            isStartingWithVideo = false
        )
        assertEquals(expected, viewModel.uiState.first().preview)

        callState.value = CallStateUi.Connected
        advanceUntilIdle()

        // check preview is not update yet on non pre call state
        assertEquals(expected, viewModel.uiState.first().preview)

        // update the streams
        streams.value = listOf(streamMock1, streamMock2)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview is starting with video true if preferred type has video and is enabled`() = runTest {
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
            every { toMyCameraVideoUi() } returns flowOf(null)
            every { toMyCameraStreamAudioUi() } returns flowOf(null)
            every { isGroupCall(any()) } returns flowOf(false)
            every { toOtherDisplayNames() } returns flowOf(listOf())
            every { toOtherDisplayImages() } returns flowOf(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(isStartingWithVideo = true)
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview is starting with video false if preferred type has video and is enabled is false`() = runTest {
        with(callMock) {
            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
            every { toMyCameraVideoUi() } returns flowOf(null)
            every { toMyCameraStreamAudioUi() } returns flowOf(null)
            every { isGroupCall(any()) } returns flowOf(false)
            every { toOtherDisplayNames() } returns flowOf(listOf())
            every { toOtherDisplayImages() } returns flowOf(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioUpgradable())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(isStartingWithVideo = false)
        assertEquals(expected, viewModel.uiState.first().preview)
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
    fun `test pinned streams cleaned on call ended`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
        advanceUntilIdle()

        viewModel.pin(streamMock1.id)
        val current = viewModel.uiState.first().pinnedStreams.value
        assertEquals(listOf(streamMock1), current)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().pinnedStreams.value
        assertEquals(listOf<String>(), new)
    }

    @Test
    fun `test fullscreen stream cleaned on call ended`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
        advanceUntilIdle()

        viewModel.fullscreen(streamMock1.id)
        val current = viewModel.uiState.first().fullscreenStream
        assertEquals(streamMock1, current)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().fullscreenStream
        assertEquals(null, new)
    }

    @Test
    fun `test fullscreen stream set fullscreen user message emitted`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)
        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
        advanceUntilIdle()

        viewModel.fullscreen(streamMock1.id)

        assertEquals(FullScreenMessage.Enabled, viewModel.userMessage.first())
    }

    @Test
    fun `test fullscreen stream null fullscreen user message emitted`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)
        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
        advanceUntilIdle()
        viewModel.fullscreen(streamMock1.id)
        viewModel.userMessage.first()

        viewModel.fullscreen(null)

        assertEquals(FullScreenMessage.Disabled, viewModel.userMessage.first())
    }

    @Test
    fun `test stream preview cleaned on call ended`() = runTest {
        val video = VideoUi(id = "videoId")
        val audio = AudioUi(id = "audioId")
        val uriMock = mockk<Uri>(relaxed = true)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Dialing)
        with(callMock) {
            every { toCallStateUi() } returns callState
            every { toMyCameraVideoUi() } returns flowOf(video)
            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
            every { isGroupCall(any()) } returns flowOf(true)
            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
            every { toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
            every { toStreamsUi() } returns MutableStateFlow(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val expected = StreamPreview(
            isGroupCall = true,
            video = video,
            audio = audio,
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )
        assertEquals(expected, viewModel.uiState.first().preview)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().preview
        assertEquals(null, new)
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
    fun `test my streams is placed after the others`() = runTest {
        val myStreamMock1 = streamMock1.copy(isMine = true)
        val myStreamMock2 = streamMock2.copy(isMine = true)
        val streams = listOf(myStreamMock1, streamMock2, myStreamMock2, streamMock3)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)

        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
        advanceUntilIdle()

        val result = viewModel.uiState.first().streams.value
        val expected = listOf(streamMock2, streamMock3, myStreamMock1, myStreamMock2)
        assertEquals(expected, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setMaxPinnedStreamsToLessThanOne_illegalArgumentExceptionThrown() {
        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        viewModel.maxPinnedStreams = 0
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
    fun setPrependTrue_pinnedStreamIsPrepended() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 3
        viewModel.pin(streamMock2.id)

        val isPinned = viewModel.pin(streamMock1.id, prepend = true)
        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun setPrependFalse_pinnedStreamIsAppended() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 3
        viewModel.pin(streamMock2.id)

        val isPinned = viewModel.pin(streamMock1.id, prepend = false)
        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock2, streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun setForceAppend_pinnedStreamIsPinnedEvenIfMaxPinnedStreamsIsReached() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 2
        viewModel.pin(streamMock2.id)
        viewModel.pin(streamMock3.id)

        val isPinned = viewModel.pin(streamMock1.id, prepend = false, force = true)
        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock2, streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun setForcePrepend_pinnedStreamIsPinnedEvenIfMaxPinnedStreamsIsReached() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 2
        viewModel.pin(streamMock2.id)
        viewModel.pin(streamMock3.id)

        val isPinned = viewModel.pin(streamMock1.id, prepend = true, force = true)
        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock1, streamMock3), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun tryPinSameStreamMultipleTime_secondPinFails() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val isPinned = viewModel.pin(streamMock1.id)

        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)

        val isPinned2 = viewModel.pin(streamMock1.id)

        assertEquals(false, isPinned2)
        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun tryPinSameStreamMultipleTimeWithForceTrue_secondPinFails() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        val isPinned = viewModel.pin(streamMock1.id)

        assertEquals(true, isPinned)
        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)

        val isPinned2 = viewModel.pin(streamMock1.id, force = true)

        assertEquals(false, isPinned2)
        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun forcePrependWithLocalScreenShareAndMaxLimitReached_firstNonLocalScreenShareStreamIsReplaced() = runTest {
        val localScreenShareMock = StreamUi(id = "streamId3", username = "username", isMine = true, video = VideoUi("videoId", isScreenShare = true))

        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, localScreenShareMock))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 2
        viewModel.pin(streamMock1.id)

        assertEquals(listOf(localScreenShareMock, streamMock1), viewModel.uiState.value.pinnedStreams.value)

        val isPinned = viewModel.pin(streamMock2.id, prepend = true, force = true)

        assertEquals(true, isPinned)
        assertEquals(listOf(localScreenShareMock, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun forcePrependWithLocalScreenShareAndMaxLimitOne_localScreenShareIsKept() = runTest {
        val localScreenShareMock = StreamUi(id = "streamId3", username = "username", isMine = true, video = VideoUi("videoId", isScreenShare = true))

        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, localScreenShareMock))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 1
        val isPinned = viewModel.pin(streamMock1.id, prepend = true, force = true)

        assertEquals(false, isPinned)
        assertEquals(listOf(localScreenShareMock), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun testPinOverMaxPinnedStreams() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }

        viewModel.maxPinnedStreams = 1

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
    fun testUnpinAll() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.maxPinnedStreams = 3
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)
        viewModel.pin(streamMock3.id)

        assertEquals(listOf(streamMock1, streamMock2, streamMock3), viewModel.uiState.value.pinnedStreams.value)

        viewModel.unpinAll()

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
    fun `local screen share is not pinned automatically again on stream list update`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = true, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        streams.value = listOf(screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock), viewModel.uiState.value.pinnedStreams.value)

        streams.value = listOf(streamMock1, screenShareMock, streamMock2)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock), viewModel.uiState.value.pinnedStreams.value)
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
        viewModel.maxPinnedStreams = 2
        viewModel.pin(streamMock1.id)
        viewModel.pin(streamMock2.id)

        assertEquals(listOf(streamMock1, streamMock2), viewModel.uiState.value.pinnedStreams.value)
        streams.value = listOf(streamMock1, streamMock2, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock, streamMock2), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `remote screen share is automatically added as first pinned stream`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = false, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        streams.value = listOf(streamMock1, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `remote screen share is not automatically if there is already a pinned stream`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = false, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()
        viewModel.pin(streamMock1.id)

        streams.value = listOf(streamMock1, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)
    }

    @Test
    fun `pin screen share message is sent if remote screen share is not added to the pinned streams`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = false, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()
        viewModel.pin(streamMock1.id)

        streams.value = listOf(streamMock1, screenShareMock)
        advanceUntilIdle()

        assertEquals(listOf(streamMock1), viewModel.uiState.value.pinnedStreams.value)
        verify(exactly = 1) { CallUserMessagesProvider.sendUserMessage(PinScreenshareMessage("screenShareId", "username")) }
    }

    @Test
    fun `remote screen share is not pinned automatically again on stream list update`() = runTest {
        val screenShareMock = StreamUi(id = "screenShareId", username = "username", isMine = false, video = VideoUi(id = "videoId", isScreenShare = true))
        val streams = MutableStateFlow(listOf(streamMock1, screenShareMock))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        assertEquals(listOf(screenShareMock), viewModel.uiState.value.pinnedStreams.value)

        viewModel.unpin(screenShareMock.id)
        assertEquals(listOf<StreamUi>(), viewModel.uiState.value.pinnedStreams.value)

        streams.value = listOf(streamMock1, screenShareMock, streamMock2)
        advanceUntilIdle()

        assertEquals(listOf<StreamUi>(), viewModel.uiState.value.pinnedStreams.value)
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
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
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
            every { enabled } returns MutableStateFlow(Input.Enabled.None)
            every { tryDisable() } returns true
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
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
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
            every { tryDisable() } returns true
        }
        val usbInput = mockk<Input.Video.Camera.Usb> {
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
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

    @Test
    fun zoomCalledOnViewModel_zoomCalledOnVideoStreamView() = runTest {
        val videoStreamView = mockk<VideoStreamView>(relaxed = true) {
            every { zoomLevel } returns MutableStateFlow(StreamView.ZoomLevel.Fit)
        }
        val videoMock = StreamUi(
            id = "streamId", username = "username",
            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true))
        val streams = MutableStateFlow(listOf(videoMock))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.zoom("streamId")

        verify { videoStreamView.zoom() }
    }

    @Test
    fun zoomCalledOnViewModel_streamIdNotPresent_zoomNotCalledOnVideoStreamView() = runTest {
        val videoStreamView = mockk<VideoStreamView>(relaxed = true) {
            every { zoomLevel } returns MutableStateFlow(StreamView.ZoomLevel.Fit)
        }
        val videoMock = StreamUi(
            id = "streamId", username = "username",
            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true))
        val streams = MutableStateFlow(listOf(videoMock))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
        advanceUntilIdle()

        viewModel.zoom("streamId2")

        verify(exactly = 0) { videoStreamView.zoom() }
    }

    private fun testTryStopScreenShare(screenShareVideoMock: Input.Video) = runTest {
        every { screenShareVideoMock.enabled } returns MutableStateFlow(Input.Enabled.Both)
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
