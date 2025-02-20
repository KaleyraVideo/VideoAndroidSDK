package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Conference
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
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherUserInfo
import com.kaleyra.video_sdk.call.mapper.StreamMapper
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper
import com.kaleyra.video_sdk.call.mapper.VideoMapper.toMyCameraVideoUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutSettings
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel.Companion.DEFAULT_DEBOUNCE_MILLIS
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel.Companion.SINGLE_STREAM_DEBOUNCE_MILLIS
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    var mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val streamMock1 = StreamUi(id = "streamId1", userInfo = UserInfo("userId", "username", ImmutableUri()))

    private val streamMock2 = StreamUi(id = "streamId2", userInfo = UserInfo("userId", "username", ImmutableUri()))

    private val streamMock3 = StreamUi(id = "streamId3", userInfo = UserInfo("userId", "username", ImmutableUri()))

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
        every { conferenceMock.settings } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `stream auto mode set by default`() {
        val layoutController = spyk(StreamLayoutControllerMock())
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        verify(exactly = 1) { layoutController.switchToAutoMode() }
    }

    @Test
    fun `hasReachedMaxPinnedStreams is false when layout controller isPinnedStreamLimitReached is true`() {
        val layoutController = StreamLayoutControllerMock(initialIsPinnedStreamLimitReached = false)
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(false, viewModel.uiState.value.hasReachedMaxPinnedStreams)
    }

    @Test
    fun `hasReachedMaxPinnedStreams is true when layout controller isPinnedStreamLimitReached is false`() {
        val layoutController = StreamLayoutControllerMock(initialIsPinnedStreamLimitReached = true)
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(true, viewModel.uiState.value.hasReachedMaxPinnedStreams)
    }

    @Test
    fun `isGroupCall layout settings is true when the call is a many to many`() {
        every { callMock.toInCallParticipants() } returns flowOf(listOf(mockk(), mockk(), mockk()))
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            StreamLayoutSettings(isGroupCall = true),
            layoutController.layoutSettings.value
        )
    }

    @Test
    fun `isGroupCall layout settings is false when the call is a one to one`() {
        every { callMock.toInCallParticipants() } returns flowOf(listOf(mockk(), mockk()))
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            StreamLayoutSettings(isGroupCall = false),
            layoutController.layoutSettings.value
        )
    }

    @Test
    fun `defaultCameraIsBack setting is preserved on isGroupCall setting update`() {
        val toInCallParticipantsFlow = MutableStateFlow(listOf<CallParticipant>(mockk(), mockk()))
        every { conferenceMock.settings } returns Conference.Settings(camera = Conference.Settings.Camera.Back)
        every { callMock.toInCallParticipants() } returns toInCallParticipantsFlow
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            StreamLayoutSettings(isGroupCall = false, defaultCameraIsBack = true),
            layoutController.layoutSettings.value
        )
        toInCallParticipantsFlow.value = listOf(mockk(), mockk(), mockk())
        assertEquals(
            StreamLayoutSettings(isGroupCall = true, defaultCameraIsBack = true),
            layoutController.layoutSettings.value
        )
    }

    @Test
    fun `defaultCameraIsBack is true when the conference camera settings is the back camera`() = runTest {
        every { conferenceMock.settings } returns Conference.Settings(camera = Conference.Settings.Camera.Back)
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            StreamLayoutSettings(defaultCameraIsBack = true),
            layoutController.layoutSettings.value
        )
    }

    @Test
    fun `defaultCameraIsBack is false when the conference camera settings is the front camera`() = runTest {
        every { conferenceMock.settings } returns Conference.Settings(camera = Conference.Settings.Camera.Front)
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            StreamLayoutSettings(defaultCameraIsBack = false),
            layoutController.layoutSettings.value
        )
    }

    @Test
    fun `the layout controller's stream list does not include the local screen share`() {
        every { callMock.toStreamsUi() } returns flowOf(
            listOf(
                StreamUi(id = "1", userInfo = UserInfo("userId", "user1", ImmutableUri())),
                StreamUi(id = "2", userInfo = UserInfo("userId2", "user2", ImmutableUri()), isMine = true, video = VideoUi(id = "1", isScreenShare = true)),
                StreamUi(id = "3", userInfo = UserInfo("userId3", "user3", ImmutableUri()), video = VideoUi(id = "2", isScreenShare = true))
            )
        )
        val layoutController = StreamLayoutControllerMock()
        StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            listOf(
                StreamUi(id = "1", userInfo = UserInfo("userId", "user1", ImmutableUri())),
                StreamUi(id = "3", userInfo = UserInfo("userId3", "user3", ImmutableUri()), video = VideoUi(id = "2", isScreenShare = true))
            ),
            layoutController.layoutStreams.value
        )
    }

    @Test
    fun `isScreenShareActive is true when the local screen share stream exists`() {
        every { callMock.toStreamsUi() } returns flowOf(
            listOf(StreamUi(id = "1", userInfo = UserInfo("userId", "user1", ImmutableUri()), isMine = true, video = VideoUi(id = "1", isScreenShare = true)))
        )
        val layoutController = StreamLayoutControllerMock()
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            true,
            viewModel.uiState.value.isScreenShareActive
        )
    }

    @Test
    fun `isScreenShareActive is false when the local screen share stream does not exists`() {
        every { callMock.toStreamsUi() } returns flowOf(
            listOf(StreamUi(id = "1", userInfo = UserInfo("userId", "user1", ImmutableUri()), video = VideoUi(id = "1", isScreenShare = true)))
        )
        val layoutController = StreamLayoutControllerMock()
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(
            false,
            viewModel.uiState.value.isScreenShareActive
        )
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
            every { toOtherUserInfo() } returns flowOf(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            )
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio,
            userInfos = ImmutableList(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            ),
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
            every { toOtherUserInfo() } returns flowOf(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            )
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio,
            userInfos = ImmutableList(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            ),
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
            every { toOtherUserInfo() } returns flowOf(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            )
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio,
            userInfos = ImmutableList(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            ),
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
            every { toOtherUserInfo() } returns flowOf(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            )
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio,
            userInfos = ImmutableList(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            ),
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
            every { toOtherUserInfo() } returns flowOf(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio
        )
        assertEquals(expected, viewModel.uiState.first().preview)
    }

    @Test
    fun `stream preview reset to null after pre call state is ended and streams count is more than 1`() =
        runTest {
            val video = VideoUi(id = "videoId")
            val audio = AudioUi(id = "audioId")
            val uriMock = mockk<Uri>(relaxed = true)
            val callState = MutableStateFlow<CallStateUi>(CallStateUi.RingingRemotely)
            with(callMock) {
                every { toCallStateUi() } returns callState
                every { toMyCameraVideoUi() } returns flowOf(video)
                every { toMyCameraStreamAudioUi() } returns flowOf(audio)
                every { toOtherUserInfo() } returns flowOf(
                    listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
                )
                every { toInCallParticipants() } returns MutableStateFlow(
                    listOf(participantMock1, participantMock2)
                )
                every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
            }

            val layoutController = StreamLayoutControllerMock(initialStreamItems = listOf(streamMock1.toStreamItem()))
            val viewModel = StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = layoutController
            )
            advanceUntilIdle()

            val expected = StreamPreview(
                video = video,
                audio = audio,
                isStartingWithVideo = false,
                userInfos = ImmutableList(
                    listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
                ),
            )
            assertEquals(expected, viewModel.uiState.first().preview)

            callState.value = CallStateUi.Connected
            advanceUntilIdle()

            // check preview is not update yet on non pre call state
            assertEquals(expected, viewModel.uiState.first().preview)

            // update the streams
            layoutController.setStreamItems(
                listOf(streamMock1.toStreamItem(), streamMock2.toStreamItem())
            )
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.first().preview)
        }

    @Test
    fun `stream preview is starting with video true if preferred type has video and is enabled`() =
        runTest {
            with(callMock) {
                every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
                every { toMyCameraVideoUi() } returns flowOf(null)
                every { toMyCameraStreamAudioUi() } returns flowOf(null)
                every { toOtherUserInfo() } returns flowOf(listOf())
                every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
            }

            val viewModel = StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = StreamLayoutControllerMock()
            )
            advanceUntilIdle()

            val expected = StreamPreview(isStartingWithVideo = true)
            assertEquals(expected, viewModel.uiState.first().preview)
        }

    @Test
    fun `stream preview is starting with video false if preferred type has video and is enabled is false`() =
        runTest {
            with(callMock) {
                every { toCallStateUi() } returns MutableStateFlow(CallStateUi.RingingRemotely)
                every { toMyCameraVideoUi() } returns flowOf(null)
                every { toMyCameraStreamAudioUi() } returns flowOf(null)
                every { toOtherUserInfo() } returns flowOf(listOf())
                every { preferredType } returns MutableStateFlow(Call.PreferredType.audioUpgradable())
            }

            val viewModel = StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = StreamLayoutControllerMock()
            )
            advanceUntilIdle()

            val expected = StreamPreview(isStartingWithVideo = false)
            assertEquals(expected, viewModel.uiState.first().preview)
        }

    @Test
    fun `test streams updated after a debounce time if there is only one stream, there is still a participant in call and the call is connected`() =
        runTest {
            every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
            every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)

            val viewModel = spyk(
                StreamViewModel(
                    configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                    layoutController = StreamLayoutControllerMock(
                        initialStreamItems = listOf(streamMock1.toStreamItem())
                    )
                )
            )

            advanceTimeBy(SINGLE_STREAM_DEBOUNCE_MILLIS)

            val current = viewModel.uiState.first().streamItems.value
            assertEquals(listOf<StreamItem>(), current)

            advanceTimeBy(1)

            val new = viewModel.uiState.first().streamItems.value
            val expected = listOf(streamMock1.toStreamItem())
            assertEquals(expected, new)
        }

    @Test
    fun `test streams cleaned on call ended`() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3)
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
        every { callMock.toCallStateUi() } returns callState

        val viewModel = spyk(
            StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = StreamLayoutControllerMock(
                    initialStreamItems = streams.map { it.toStreamItem() }
                )
            )
        )

        advanceUntilIdle()

        val current = viewModel.uiState.first().streamItems.value
        val expected = streams.map { it.toStreamItem() }
        assertEquals(expected, current)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().streamItems.value
        assertEquals(emptyList<StreamItem>(), new)
    }

    @Test
    fun `test setFullscreenStream`() = runTest {
        val layoutController = StreamLayoutControllerMock()
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.setFullscreenStream(streamMock1.id)
        assertEquals(streamMock1.id, layoutController.fullscreenId)
        assertEquals(FullScreenMessage.Enabled, viewModel.userMessage.first())
    }

    @Test
    fun `test clearFullscreenStream`() = runTest {
        val layoutController = StreamLayoutControllerMock(initialFullscreenId = "streamId")
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.clearFullscreenStream()
        assertEquals(null, layoutController.fullscreenId)
        assertEquals(FullScreenMessage.Disabled, viewModel.userMessage.first())
    }

    @Test
    fun `test pinStream force true`() = runTest {
        val layoutController = StreamLayoutControllerMock(
            initialPinnedStreamIds = listOf("streamId1", "streamId2"),
            initialLayoutConstraints = StreamLayoutConstraints(featuredStreamThreshold = 2)
        )
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.pinStream("streamId3", force = true)
        assertEquals(listOf("streamId1", "streamId3"), layoutController.pinnedStreamIds)
    }

    @Test
    fun `test pinStream force false`() = runTest {
        val layoutController = StreamLayoutControllerMock(
            initialPinnedStreamIds = listOf("streamId1", "streamId2"),
            initialLayoutConstraints = StreamLayoutConstraints(featuredStreamThreshold = 2)
        )
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.pinStream("streamId", force = false)
        assertEquals(listOf("streamId1", "streamId2"), layoutController.pinnedStreamIds)
    }

    @Test
    fun `test pinStream prepend true`() = runTest {
        val layoutController = StreamLayoutControllerMock(initialPinnedStreamIds = listOf("streamId1"))
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.pinStream("streamId2", prepend = true)
        assertEquals(listOf("streamId2", "streamId1"), layoutController.pinnedStreamIds)
    }

    @Test
    fun `test pinStream prepend false`() = runTest {
        val layoutController = StreamLayoutControllerMock(initialPinnedStreamIds = listOf("streamId1"))
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.pinStream("streamId2", prepend = false)
        assertEquals(listOf("streamId1", "streamId2"), layoutController.pinnedStreamIds)

    }

    @Test
    fun `test pinStream success`() = runTest {
        val layoutController = StreamLayoutControllerMock(
            initialLayoutConstraints = StreamLayoutConstraints(featuredStreamThreshold = 1)
        )
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        val result = viewModel.pinStream("streamId")
        assertEquals(true, result)
    }

    @Test
    fun `test pinStream fail`() = runTest {
        val layoutController = StreamLayoutControllerMock(
            initialLayoutConstraints = StreamLayoutConstraints(featuredStreamThreshold = 0)
        )
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        val result = viewModel.pinStream("streamId")
        assertEquals(false, result)
    }

    @Test
    fun `test unpinStream`() = runTest {
        val layoutController = StreamLayoutControllerMock(initialPinnedStreamIds = listOf("streamId"))
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.unpinStream("streamId")
        assertEquals(emptyList<String>(), layoutController.pinnedStreamIds)
    }

    @Test
    fun `test clearPinnedStreams`() = runTest {
        val layoutController = StreamLayoutControllerMock(initialPinnedStreamIds = listOf("streamId1", "streamId2"))
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.clearPinnedStreams()
        assertEquals(emptyList<String>(), layoutController.pinnedStreamIds)
    }

    @Test
    fun `test setStreamLayoutConstraints`() = runTest {
        val layoutController = StreamLayoutControllerMock()
        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.setStreamLayoutConstraints(mosaicStreamThreshold = 3, featuredStreamThreshold = 4, thumbnailStreamThreshold = 5)
        assertEquals(
            StreamLayoutConstraints(mosaicStreamThreshold = 3, featuredStreamThreshold = 4, thumbnailStreamThreshold = 5),
            layoutController.layoutConstraints.value
        )
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
            every { toOtherUserInfo() } returns flowOf(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            )
            every { toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1, participantMock2))
            every { toStreamsUi() } returns MutableStateFlow(listOf())
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val expected = StreamPreview(
            video = video,
            audio = audio,
            userInfos = ImmutableList(
                listOf(UserInfo("userId1", "displayName", ImmutableUri(uriMock)))
            ),
        )
        assertEquals(expected, viewModel.uiState.first().preview)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().preview
        assertEquals(null, new)
    }

    @Test
    fun `test streams updated after default debounce if there is more than one participant in call`() =
        runTest {
            val streamItems = listOf(streamMock1.toStreamItem())
            every { callMock.toInCallParticipants() } returns MutableStateFlow(
                listOf(
                    participantMock1,
                    participantMock2
                )
            )
            every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)

            val viewModel = spyk(
                StreamViewModel(
                    configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                    layoutController = StreamLayoutControllerMock(initialStreamItems = streamItems)
                )
            )

            advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
            val current = viewModel.uiState.first().streamItems.value
            assertEquals(listOf<StreamItem>(), current)

            advanceTimeBy(1)
            val new = viewModel.uiState.first().streamItems.value
            assertEquals(streamItems, new)
        }

    @Test
    fun `test streams updated immediately if the call is not connected`() = runTest {
        val streamItems = listOf(streamMock1.toStreamItem())
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
        every { callMock.toCallStateUi() } returns MutableStateFlow(mockk(relaxed = true))

        val viewModel = spyk(
            StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = StreamLayoutControllerMock(initialStreamItems = streamItems)
            )
        )

        advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
        val current = viewModel.uiState.first().streamItems.value
        assertEquals(listOf<StreamItem>(), current)

        advanceTimeBy(1)
        val new = viewModel.uiState.first().streamItems.value
        assertEquals(streamItems, new)
    }

    @Test
    fun `test streams updated immediately if there are more than one stream`() = runTest {
        val streamItems = listOf(streamMock1, streamMock2).map { it.toStreamItem() }
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(participantMock1))
        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Connected)

        val viewModel = spyk(
            StreamViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController = StreamLayoutControllerMock(initialStreamItems = streamItems)
            )
        )

        advanceTimeBy(DEFAULT_DEBOUNCE_MILLIS)
        val current = viewModel.uiState.first().streamItems.value
        assertEquals(listOf<StreamItem>(), current)

        advanceTimeBy(1)
        val new = viewModel.uiState.first().streamItems.value
        assertEquals(streamItems, new)
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

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
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

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )

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

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )

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
            id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri()),
            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true)
        )
        val streamItems = listOf(videoMock.toStreamItem())
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock(initialStreamItems = streamItems)
        )
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
            id = "streamId", userInfo = UserInfo("userId", "username", ImmutableUri()),
            video = VideoUi(id = "videoId", view = ImmutableView(videoStreamView), isEnabled = true)
        )
        val streams = MutableStateFlow(listOf(videoMock))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toStreamsUi() } returns streams

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
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
        val availableInputs = setOf(
            screenShareVideoMock,
            mockk<Input.Video.Screen>(),
            mockk<Input.Video.Application>(),
            mockk<Input.Video.Camera>()
        )
        every { callMock.inputs.availableInputs } returns MutableStateFlow(availableInputs)
        every { callMock.participants } returns MutableStateFlow(participants)

        val viewModel = StreamViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        advanceUntilIdle()

        val isStopped = viewModel.tryStopScreenShare()
        verify(exactly = 1) { meMock.removeStream(myStreamMock) }
        assertEquals(true, isStopped)
    }

    private fun StreamUi.toStreamItem(streamItemState: StreamItemState = StreamItemState.Standard): StreamItem {
        return StreamItem.Stream(id, this, streamItemState)
    }
}
