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
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
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
    var mainDispatcherRule = MainDispatcherRule()

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
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvitedParticipantsUpdated() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(otherMock1))

        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        val expected = ImmutableList(listOf("displayName1", "displayName3"))
        assertEquals(expected, viewModel.uiState.first().invitedParticipants)
    }

    @Test
    fun noInCallParticipants_participantsCountStateIsZero() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf())


        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.first().participantCount)
    }

    @Test
    fun inCallParticipants_participantsCountStateIsUpdated() = runTest {
        every { callMock.toInCallParticipants() } returns MutableStateFlow(listOf(otherMock1, otherMock2))


        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.first().participantCount)
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

        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
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

        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        viewModel.toggleMic(activity)
        runCurrent()

        coVerify(exactly = 1) { inputs.request(activity, Inputs.Type.Microphone) }
        verify(exactly = 1) { audio.tryDisable() }
    }

    @Test
    fun testMuteStreamAudio() = runTest {
        every { audioMock2.enabled } returns MutableStateFlow(Input.Enabled.Both)

        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        viewModel.muteStreamAudio(streamMock2.id)

        verify(exactly = 1) { audioMock2.tryDisable() }
    }

    @Test
    fun testUnmuteStreamAudio() = runTest {
        every { audioMock2.enabled } returns MutableStateFlow(Input.Enabled.None)

        val viewModel = spyk(ParticipantsViewModel{
            mockkSuccessfulConfiguration(conference = conferenceMock)
        })
        advanceUntilIdle()

        viewModel.muteStreamAudio(streamMock2.id)

        verify(exactly = 1) { audioMock2.tryEnable() }
    }
}