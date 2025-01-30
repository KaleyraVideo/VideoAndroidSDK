//package com.kaleyra.video_sdk.viewmodel.call
//
//import android.net.Uri
//import com.kaleyra.video.conference.Call
//import com.kaleyra.video.conference.CallParticipant
//import com.kaleyra.video.conference.CallParticipants
//import com.kaleyra.video.conference.Input
//import com.kaleyra.video.conference.Stream
//import com.kaleyra.video.conference.StreamView
//import com.kaleyra.video.conference.VideoStreamView
//import com.kaleyra.video_common_ui.CallUI
//import com.kaleyra.video_common_ui.ConferenceUI
//import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
//import com.kaleyra.video_sdk.MainDispatcherRule
//import com.kaleyra.video_sdk.call.mapper.AudioMapper
//import com.kaleyra.video_sdk.call.mapper.AudioMapper.toMyCameraStreamAudioUi
//import com.kaleyra.video_sdk.call.mapper.CallStateMapper
//import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
//import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
//import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
//import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
//import com.kaleyra.video_sdk.call.mapper.StreamMapper
//import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
//import com.kaleyra.video_sdk.call.mapper.VideoMapper
//import com.kaleyra.video_sdk.call.mapper.VideoMapper.toMyCameraVideoUi
//import com.kaleyra.video_sdk.call.screen.model.CallStateUi
//import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
//import com.kaleyra.video_sdk.call.stream.model.StreamItem
//import com.kaleyra.video_sdk.call.stream.model.StreamPreview
//import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
//import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
//import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
//import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
//import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProviderImpl
//import com.kaleyra.video_sdk.call.stream.viewmodel.FullscreenStreamItemProviderImpl
//import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProviderImpl
//import com.kaleyra.video_sdk.call.stream.model.StreamItemState
//import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutControllerImpl
//import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
//import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel.Companion.DEFAULT_DEBOUNCE_MILLIS
//import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel.Companion.DEFAULT_MAX_MOSAIC_STREAMS
//import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel.Companion.SINGLE_STREAM_DEBOUNCE_MILLIS
//import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
//import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
//import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
//import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
//import com.kaleyra.video_sdk.ui.mockkSuccessfulConfiguration
//import io.mockk.coVerify
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.mockkConstructor
//import io.mockk.mockkObject
//import io.mockk.spyk
//import io.mockk.unmockkAll
//import io.mockk.verify
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.UnconfinedTestDispatcher
//import kotlinx.coroutines.test.advanceTimeBy
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.runTest
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//
//@OptIn(ExperimentalCoroutinesApi::class)
//@RunWith(RobolectricTestRunner::class)
//class StreamViewModelTest {
//
//    @get:Rule
//    var mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())
//
//    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)
//
//    private val callMock = mockk<CallUI>(relaxed = true)
//
//    private val streamMock1 = StreamUi(id = "streamId1", username = "username")
//
//    private val streamMock2 = StreamUi(id = "streamId2", username = "username")
//
//    private val streamMock3 = StreamUi(id = "streamId3", username = "username")
//
//    private val participantMock1 = mockk<CallParticipant>()
//
//    private val participantMock2 = mockk<CallParticipant>()
//
//    @Before
//    fun setUp() {
//        mockkObject(com.kaleyra.video_common_ui.mapper.ParticipantMapper)
//        mockkObject(com.kaleyra.video_sdk.call.mapper.ParticipantMapper)
//        mockkObject(StreamMapper)
//        mockkObject(CallStateMapper)
//        mockkObject(VideoMapper)
//        mockkObject(AudioMapper)
//        mockkObject(CallUserMessagesProvider)
//        mockkConstructor(StreamLayoutControllerImpl::class)
//        mockkConstructor(MosaicStreamItemsProviderImpl::class)
//        mockkConstructor(FeaturedStreamItemsProviderImpl::class)
//        mockkConstructor(FullscreenStreamItemProviderImpl::class)
//        every { conferenceMock.call } returns MutableStateFlow(callMock)
//    }
//
//    @After
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun test() {
//        coVerify {
//            anyConstructed<MosaicStreamItemsProviderImpl>().maxStreams.first() == DEFAULT_MAX_MOSAIC_STREAMS
//        }
//    }
//
//    @Test
//    fun `stream preview set on call ringing`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Ringing)
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock)
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview set on call dialing`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Dialing)
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock)
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview set on call ringing remotely`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock),
//            isStartingWithVideo = false
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview set on call reconnecting`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.Reconnecting)
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock)
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `handle empty list for other display names and images`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf())
//            every { toOtherDisplayImages() } returns flowOf(listOf())
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview reset to null after pre call state is ended and streams count is more than 1`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        val callState = MutableStateFlow<CallStateUi>(CallStateUi.RingingRemotely)
//        val streams = MutableStateFlow(listOf(streamMock1))
//        with(callMock) {
//            every { toCallStateUi() } returns callState
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { toStreamsUi() } returns streams
//            every { toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock),
//            isStartingWithVideo = false
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//
//        callState.value = CallStateUi.Connected
//        advanceUntilIdle()
//
//        // check preview is not update yet on non pre call state
//        assertEquals(expected, viewModel.uiState.first().preview)
//
//        // update the streams
//        streams.value = listOf(streamMock1, streamMock2)
//        advanceUntilIdle()
//
//        assertEquals(null, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview is starting with video true if preferred type has video and is enabled`() = runTest {
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
//            every { toMyCameraVideoUi() } returns flowOf(null)
//            every { toMyCameraStreamAudioUi() } returns flowOf(null)
//            every { isGroupCall(any()) } returns flowOf(false)
//            every { toOtherDisplayNames() } returns flowOf(listOf())
//            every { toOtherDisplayImages() } returns flowOf(listOf())
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(isStartingWithVideo = true)
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `stream preview is starting with video false if preferred type has video and is enabled is false`() = runTest {
//        with(callMock) {
//            every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
//            every { toMyCameraVideoUi() } returns flowOf(null)
//            every { toMyCameraStreamAudioUi() } returns flowOf(null)
//            every { isGroupCall(any()) } returns flowOf(false)
//            every { toOtherDisplayNames() } returns flowOf(listOf())
//            every { toOtherDisplayImages() } returns flowOf(listOf())
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioUpgradable())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(isStartingWithVideo = false)
//        assertEquals(expected, viewModel.uiState.first().preview)
//    }
//
//    @Test
//    fun `test streams updated after a debounce time if there is only one stream, there is still a participant in call and the call is connected`() = runTest {
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
//        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
//        every { anyConstructed<StreamLayoutControllerImpl>().streamItems } returns flowOf(listOf(streamMock1.toStreamItem()))
//
//        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
//
//        advanceTimeBy(SINGLE_STREAM_DEBOUNCE_MILLIS)
//
//        val current = viewModel.uiState.first().streamItems.value
//        assertEquals(listOf<StreamItem>(), current)
//
//        advanceTimeBy(1)
//
//        val new = viewModel.uiState.first().streamItems.value
//        val expected = listOf(streamMock1.toStreamItem())
//        assertEquals(expected, new)
//    }
//
//    @Test
//    fun `test streams cleaned on call ended`() = runTest {
//        val streams = listOf(streamMock1, streamMock2, streamMock3)
//        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
//        every { callMock.toCallStateUi() } returns callState
//        every { anyConstructed<StreamLayoutControllerImpl>().streamItems } returns flowOf(streams.map { it.toStreamItem() })
//
//        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
//
//        advanceUntilIdle()
//
//        val current = viewModel.uiState.first().streamItems.value
//        val expected = streams.map { it.toStreamItem() }
//        assertEquals(expected, current)
//
//        callState.value = CallStateUi.Disconnected.Ended
//        advanceUntilIdle()
//
//        val new = viewModel.uiState.first().streamItems.value
//        assertEquals(emptyList<StreamItem>(), new)
//    }
//
//    @Test
//    fun `test setFullscreenStream`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().setFullscreenStream(any()) } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.setFullscreenStream(streamMock1.id)
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().setFullscreenStream(streamMock1.id) }
//        assertEquals(FullScreenMessage.Enabled, viewModel.userMessage.first())
//    }
//
//    @Test
//    fun `test clearFullscreenStream`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().setFullscreenStream(any()) } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.clearFullscreenStream()
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().clearFullscreenStream() }
//        assertEquals(FullScreenMessage.Disabled, viewModel.userMessage.first())
//    }
//
//    @Test
//    fun `test switchToManualLayout`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().switchToManualLayout() } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.switchToManualLayout()
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().switchToManualLayout() }
//    }
//
//    @Test
//    fun `test switchToAutoLayout`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().switchToAutoLayout() } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.switchToAutoLayout()
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().switchToAutoLayout() }
//    }
//
//    @Test
//    fun `test pinStream force true`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns true
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.pinStream("streamId", force = true)
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().pinStream("streamId", force = true) }
//    }
//
//    @Test
//    fun `test pinStream force false`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns true
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.pinStream("streamId", force = false)
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().pinStream("streamId", force = false) }
//    }
//
//    @Test
//    fun `test pinStream prepend true`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns true
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.pinStream("streamId", prepend = true)
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().pinStream("streamId", prepend = true) }
//    }
//
//    @Test
//    fun `test pinStream prepend false`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns true
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.pinStream("streamId", prepend = false)
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().pinStream("streamId", prepend = false) }
//    }
//
//    @Test
//    fun `test pinStream success`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns true
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        val result = viewModel.pinStream("streamId")
//        assertEquals(true, result)
//    }
//
//    @Test
//    fun `test pinStream fail`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().pinStream(any()) } returns false
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.pinStream("streamId")
//        val result = viewModel.pinStream("streamId")
//        assertEquals(false, result)
//    }
//
//    @Test
//    fun `test unpinStream`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().unpinStream(any()) } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.unpinStream("streamId")
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().unpinStream("streamId") }
//    }
//
//    @Test
//    fun `test clearPinnedStreams`() = runTest {
//        every { anyConstructed<StreamLayoutControllerImpl>().clearPinnedStreams() } returns Unit
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        viewModel.clearPinnedStreams()
//        verify(exactly = 1) { anyConstructed<StreamLayoutControllerImpl>().clearPinnedStreams() }
//    }
//
//    @Test
//    fun `test stream preview cleaned on call ended`() = runTest {
//        val video = VideoUi(id = "videoId")
//        val audio = AudioUi(id = "audioId")
//        val uriMock = mockk<Uri>(relaxed = true)
//        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Dialing)
//        with(callMock) {
//            every { toCallStateUi() } returns callState
//            every { toMyCameraVideoUi() } returns flowOf(video)
//            every { toMyCameraStreamAudioUi() } returns flowOf(audio)
//            every { isGroupCall(any()) } returns flowOf(true)
//            every { toOtherDisplayNames() } returns flowOf(listOf("displayName"))
//            every { toOtherDisplayImages() } returns flowOf(listOf(uriMock))
//            every { toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
//            every { toStreamsUi() } returns MutableStateFlow(listOf())
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
//        }
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val expected = StreamPreview(
//            isGroupCall = true,
//            video = video,
//            audio = audio,
//            username = "displayName",
//            avatar = ImmutableUri(uriMock)
//        )
//        assertEquals(expected, viewModel.uiState.first().preview)
//
//        callState.value = CallStateUi.Disconnected.Ended
//        advanceUntilIdle()
//
//        val new = viewModel.uiState.first().preview
//        assertEquals(null, new)
//    }
//
//    @Test
//    fun `test streams updated after default debounce if there is more than one participant in call`() = runTest {
//        val streamItems = listOf(streamMock1.toStreamItem())
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
//        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
//        every { anyConstructed<StreamLayoutControllerImpl>().streamItems } returns flowOf(streamItems)
//
//        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
//
//        advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
//        val current = viewModel.uiState.first().streamItems.value
//        assertEquals(listOf<StreamItem>(), current)
//
//        advanceTimeBy(1)
//        val new = viewModel.uiState.first().streamItems.value
//        assertEquals(streamItems, new)
//    }
//
//    @Test
//    fun `test streams updated immediately if the call is not connected`() = runTest {
//        val streamItems = listOf(streamMock1.toStreamItem())
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
//        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))
//        every { anyConstructed<StreamLayoutControllerImpl>().streamItems } returns flowOf(streamItems)
//
//        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
//
//        advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
//        val current = viewModel.uiState.first().streamItems.value
//        assertEquals(listOf<StreamItem>(), current)
//
//        advanceTimeBy(1)
//        val new = viewModel.uiState.first().streamItems.value
//        assertEquals(streamItems, new)
//    }
//
//    @Test
//    fun `test streams updated immediately if there are more than one stream`() = runTest {
//        val streamItems = listOf(streamMock1, streamMock2).map { it.toStreamItem() }
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
//        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)
//        every { anyConstructed<StreamLayoutControllerImpl>().streamItems } returns flowOf(streamItems)
//
//        val viewModel = spyk(StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) })
//
//        advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
//        val current = viewModel.uiState.first().streamItems.value
//        assertEquals(listOf<StreamItem>(), current)
//
//        advanceTimeBy(1)
//        val new = viewModel.uiState.first().streamItems.value
//        assertEquals(streamItems, new)
//    }
//
//    @Test
//    fun noScreenShareInputAvailable_stopScreenShareFail() = runTest {
//        val cameraInput = mockk<Input.Video.Camera.Internal> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
//        }
//        val usbInput = mockk<Input.Video.Camera.Usb> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
//        }
//        val availableInputs = setOf(cameraInput, usbInput)
//        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val isStopped = viewModel.tryStopScreenShare()
//        assertEquals(false, isStopped)
//    }
//
//    @Test
//    fun noScreenShareInputActive_stopScreenShareFail() = runTest {
//        val screenShareInput = mockk<Input.Video.Application> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.None)
//            every { tryDisable() } returns true
//        }
//        val usbInput = mockk<Input.Video.Camera.Usb> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
//        }
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns SCREEN_SHARE_STREAM_ID
//        }
//        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//        }
//        val participants = mockk<CallParticipants>(relaxed = true) {
//            every { me } returns meMock
//        }
//        val availableInputs = setOf(screenShareInput, usbInput)
//        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
//        every { callMock.participants } returns MutableStateFlow(participants)
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//
//        advanceUntilIdle()
//
//        val isStopped = viewModel.tryStopScreenShare()
//        verify(exactly = 0) { screenShareInput.tryDisable() }
//        assertEquals(false, isStopped)
//    }
//
//    @Test
//    fun noScreenShareStream_stopScreenShareFail() = runTest {
//        val screenShareInput = mockk<Input.Video.Application> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
//            every { tryDisable() } returns true
//        }
//        val usbInput = mockk<Input.Video.Camera.Usb> {
//            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
//        }
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns "streamId"
//        }
//        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//        }
//        val participants = mockk<CallParticipants>(relaxed = true) {
//            every { me } returns meMock
//        }
//        val availableInputs = setOf(screenShareInput, usbInput)
//        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
//        every { callMock.participants } returns MutableStateFlow(participants)
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//
//        advanceUntilIdle()
//
//        val isStopped = viewModel.tryStopScreenShare()
//        assertEquals(false, isStopped)
//    }
//
//    @Test
//    fun deviceScreenShareActive_stopScreenShareSuccess() {
//        val screenShareVideoMock = spyk<Input.Video.Screen>()
//        testTryStopScreenShare(screenShareVideoMock)
//        verify(exactly = 1) { screenShareVideoMock.dispose() }
//    }
//
//    @Test
//    fun appScreenShareActive_stopScreenShareSuccess() {
//        val screenShareVideoMock = spyk<Input.Video.Application>()
//        testTryStopScreenShare(screenShareVideoMock)
//        verify(exactly = 1) { screenShareVideoMock.tryDisable() }
//    }
//
//    @Test
//    fun zoomCalledOnViewModel_zoomCalledOnVideoStreamView() = runTest {
//        val videoStreamView = mockk<VideoStreamView>(relaxed = true) {
//            every { zoomLevel } returns MutableStateFlow(StreamView.ZoomLevel.Fit)
//        }
//        val videoMock = StreamUi(
//            id = "streamId", username = "username",
//            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true))
//        val streams = MutableStateFlow(listOf(videoMock))
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
//        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
//        every { callMock.toStreamsUi() } returns streams
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        viewModel.zoom("streamId")
//
//        verify { videoStreamView.zoom() }
//    }
//
//    @Test
//    fun zoomCalledOnViewModel_streamIdNotPresent_zoomNotCalledOnVideoStreamView() = runTest {
//        val videoStreamView = mockk<VideoStreamView>(relaxed = true) {
//            every { zoomLevel } returns MutableStateFlow(StreamView.ZoomLevel.Fit)
//        }
//        val videoMock = StreamUi(
//            id = "streamId", username = "username",
//            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true))
//        val streams = MutableStateFlow(listOf(videoMock))
//        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
//        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
//        every { callMock.toStreamsUi() } returns streams
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        viewModel.zoom("streamId2")
//
//        verify(exactly = 0) { videoStreamView.zoom() }
//    }
//
//    private fun testTryStopScreenShare(screenShareVideoMock: Input.Video) = runTest {
//        every { screenShareVideoMock.enabled } returns MutableStateFlow(Input.Enabled.Both)
//        every { screenShareVideoMock.tryDisable() } returns true
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns SCREEN_SHARE_STREAM_ID
//        }
//        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//        }
//        val participants = mockk<CallParticipants>(relaxed = true) {
//            every { me } returns meMock
//        }
//        val availableInputs = setOf(screenShareVideoMock, mockk<Input.Video.Screen>(), mockk<Input.Video.Application>(), mockk<Input.Video.Camera>())
//        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
//        every { callMock.participants } returns MutableStateFlow(participants)
//
//        val viewModel = StreamViewModel { mockkSuccessfulConfiguration(conference = conferenceMock) }
//        advanceUntilIdle()
//
//        val isStopped = viewModel.tryStopScreenShare()
//        verify(exactly = 1) { meMock.removeStream(myStreamMock) }
//        assertEquals(true, isStopped)
//    }
//
//    private fun StreamUi.toStreamItem(streamItemState: StreamItemState = StreamItemState.Standard): StreamItem {
//        return StreamItem.Stream(id, this, streamItemState)
//    }
//}
