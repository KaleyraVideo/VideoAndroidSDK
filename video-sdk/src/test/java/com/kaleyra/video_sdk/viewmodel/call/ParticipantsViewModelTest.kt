package com.kaleyra.video_sdk.viewmodel.call

import androidx.fragment.app.FragmentActivity
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.StreamMapper
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.ui.mockkSuccessfulConfiguration
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
class ParticipantsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val audioMock1 = mockk<Input.Audio>(relaxed = true)

    private val audioMock2 = mockk<Input.Audio>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val meMock = mockk<CallParticipant.Me>()

    private val otherMock1 = mockk<CallParticipant>()

    private val otherMock2 = mockk<CallParticipant>()

    private val participantsMock = mockk<CallParticipants>()

    @Before
    fun setUp() {
        mockkObject(ParticipantMapper)
        mockkObject(ContactDetailsManager)
        mockkObject(StreamMapper)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(mockk(relaxed = true))
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())

        every { audioMock1.enabled } returns MutableStateFlow(Input.Enabled.Both)
        every { audioMock2.enabled } returns MutableStateFlow(Input.Enabled.Both)
        with(streamMock1) {
            every { id } returns "streamId1"
            every { audio } returns MutableStateFlow(audioMock1)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { audio } returns MutableStateFlow(audioMock2)
        }
        with(meMock) {
            every { userId } returns "userId1"
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
        }
        with(otherMock1) {
            every { userId } returns "userId2"
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { streams } returns MutableStateFlow(listOf(streamMock1))
        }
        with(otherMock2) {
            every { userId } returns "userId3"
            every { combinedDisplayName } returns MutableStateFlow("displayName3")
            every { streams } returns MutableStateFlow(listOf(streamMock2))
        }
        with(participantsMock) {
            every { me } returns meMock
            every { others } returns listOf(otherMock1, otherMock2)
        }
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { callMock.toStreamsUi() } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `streams updated on new streams ui`() {
        val streams = listOf(
            StreamUi("1", "user1"),
            StreamUi("2", "user2"),
        )
        every { callMock.toStreamsUi() } returns MutableStateFlow(streams)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = StreamLayoutControllerMock()
        )
        assertEquals(streams.toImmutableList(), viewModel.uiState.value.streams)
    }

    @Test
    fun `streamsLayout is Auto when layout controller is in auto mode`() {
        val layoutController = StreamLayoutControllerMock(initialIsInAutoMode = true)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(StreamsLayout.Auto, viewModel.uiState.value.streamsLayout)
    }

    @Test
    fun `streamsLayout is Mosaic when layout controller is in manual mode`() {
        val layoutController = StreamLayoutControllerMock(initialIsInAutoMode = false)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(StreamsLayout.Mosaic, viewModel.uiState.value.streamsLayout)
    }

    @Test
    fun `hasReachedMaxPinnedStreams is false when layout controller isPinnedStreamLimitReached is true`() {
        val layoutController = StreamLayoutControllerMock(initialIsPinnedStreamLimitReached = false)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(false, viewModel.uiState.value.hasReachedMaxPinnedStreams)
    }

    @Test
    fun `hasReachedMaxPinnedStreams is true when layout controller isPinnedStreamLimitReached is false`() {
        val layoutController = StreamLayoutControllerMock(initialIsPinnedStreamLimitReached = true)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        assertEquals(true, viewModel.uiState.value.hasReachedMaxPinnedStreams)
    }

    @Test
    fun testInvitedParticipantsUpdated() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(otherMock1))

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        val expected = ImmutableList(listOf("displayName1", "displayName3"))
        assertEquals(expected, viewModel.uiState.first().invitedParticipants)
    }

    @Test
    fun noInCallParticipants_joinedParticipantsCountStateIsZero() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.first().joinedParticipantCount)
    }

    @Test
    fun inCallParticipants_joinedParticipantsCountStateIsUpdated() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(otherMock1, otherMock2))

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.first().joinedParticipantCount)
    }

    @Test
    fun testSwitchToManualLayout() = runTest {
        val layoutController = StreamLayoutControllerMock(initialIsInAutoMode = true)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.switchToManualLayout()
        assertEquals(false, layoutController.isInAutoMode.first())
    }

    @Test
    fun testSwitchToAutoLayout() = runTest {
        val layoutController = StreamLayoutControllerMock(initialIsInAutoMode = false)
        val viewModel = ParticipantsViewModel(
            configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
            layoutController = layoutController
        )
        viewModel.switchToAutoLayout()
        assertEquals(true, layoutController.isInAutoMode.first())
    }

    @Test
    fun testToggleMicOn() = runTest {
        val activity = mockk<FragmentActivity>()
        val inputs = mockk<Inputs>(relaxed = true)
        val audio = mockk<Input.Audio>(relaxed = true) {
            every { enabled } returns MutableStateFlow(Input.Enabled.None)
            every { tryEnable() } returns true
        }
        every { callMock.inputs } returns inputs
        coEvery { inputs.request(any(), any()) } returns Inputs.RequestResult.Success(audio)

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) }, 
                layoutController = StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        viewModel.toggleMic(activity)
        runCurrent()

        coVerify(exactly = 1) { inputs.request(activity, Inputs.Type.Microphone) }
        verify(exactly = 1) { audio.tryEnable() }
    }

    @Test
    fun testToggleMicOff() = runTest {
        val activity = mockk<FragmentActivity>()
        val inputs = mockk<Inputs>(relaxed = true)
        val audio = mockk<Input.Audio>(relaxed = true) {
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
            every { tryDisable() } returns true
        }
        every { callMock.inputs } returns inputs
        coEvery { inputs.request(any(), any()) } returns Inputs.RequestResult.Success(audio)

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        viewModel.toggleMic(activity)
        runCurrent()

        coVerify(exactly = 1) { inputs.request(activity, Inputs.Type.Microphone) }
        verify(exactly = 1) { audio.tryDisable() }
    }

    @Test
    fun testMuteStreamAudio() = runTest {
        every { audioMock2.enabled } returns MutableStateFlow(Input.Enabled.Both)

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        viewModel.muteStreamAudio(streamMock2.id)

        verify(exactly = 1) { audioMock2.tryDisable() }
    }

    @Test
    fun testUnmuteStreamAudio() = runTest {
        every { audioMock2.enabled } returns MutableStateFlow(Input.Enabled.None)

        val viewModel = spyk(
            ParticipantsViewModel(
                configure = { mockkSuccessfulConfiguration(conference = conferenceMock) },
                layoutController =  StreamLayoutControllerMock()
            )
        )
        advanceUntilIdle()

        viewModel.muteStreamAudio(streamMock2.id)

        verify(exactly = 1) { audioMock2.tryEnable() }
    }
}