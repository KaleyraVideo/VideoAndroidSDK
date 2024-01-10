package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import com.kaleyra.video.Company
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.participantspanel.model.StreamArrangement
import com.kaleyra.video_sdk.call.participantspanel.viewmodel.ParticipantsPanelViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParticipantsPanelViewModelTest {


    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ParticipantsPanelViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>()

    private val viewMock = mockk<VideoStreamView>()

    private val uriMock = mockk<Uri>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    private val myAudioMock = mockk<Input.Audio>(relaxed = true)

    private val streamMock1 = mockk<Stream>()

    private val myStreamMock1 = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val displayNameFlow = MutableStateFlow("displayName")

    private val displayImageFlow = MutableStateFlow(uriMock)

    private val callFlow = MutableSharedFlow<CallUI>()

    private val companyMock = CompanyUI(object : Company {
        override val id: SharedFlow<String> = MutableSharedFlow<String>(replay = 1).apply { tryEmit("cmpnyId") }
        override val name: SharedFlow<String> = MutableSharedFlow<String>(replay = 1).apply { tryEmit("theCompany") }
        override val theme: SharedFlow<Company.Theme> = MutableSharedFlow<CompanyUI.Theme>(replay = 1).apply { tryEmit(CompanyUI.Theme()) }
    })

    @Before
    fun setUp() {
        every { conferenceMock.call } returns callFlow
        mockkObject(ContactDetailsManager)
        // only needed for toCallStateUi function
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(audioMock) {
            every { id } returns "videoId"
            every { enabled } returns MutableStateFlow(true)
        }
        with(myAudioMock) {
            every { id } returns "myVideoId"
            every { enabled } returns MutableStateFlow(true)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
        }
        with(myStreamMock1) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
            every { audio } returns MutableStateFlow(myAudioMock)
        }

        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf())
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock1))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
        }

        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { others } returns  listOf(participantMock1, participantMock2)
            every { list } returns listOf(me!!, participantMock1, participantMock2)
        }
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
    }

    @Test
    fun testNoStreamsAddedNoOneInCall() {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        Assert.assertEquals(0, viewModel.uiState.value.inCallStreamUi.value.count())
    }

    @Test
    fun testTwoStreamsAddedTwoParticipantsInCall() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        Assert.assertEquals(2, viewModel.uiState.value.inCallStreamUi.value.count())
    }

    @Test
    fun testCallAddedWatermarkInfoAvailable() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        Assert.assertEquals("theCompany", viewModel.uiState.value.watermarkInfo?.text)
    }

    @Test
    fun testGridArrangementUpdated() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        viewModel.updateStreamArrangement(StreamArrangement.Grid)
        Assert.assertEquals(StreamArrangement.Grid, viewModel.uiState.value.streamArrangement)
    }

    @Test
    fun testPinArrangementUpdated() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        viewModel.updateStreamArrangement(StreamArrangement.Pin)
        Assert.assertEquals(StreamArrangement.Pin, viewModel.uiState.value.streamArrangement)
    }

    @Test
    fun testStreamUiPinned() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        val streamId = "streamId1"
        viewModel.pin(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        Assert.assertEquals(true, viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId }.pinned)
    }

    @Test
    fun testStreamUiUnPinned() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        val streamId = "streamId1"
        viewModel.pin(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        viewModel.unpin(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        Assert.assertEquals(true, viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId }.pinned)
    }

    @Test
    fun testStreamUiMuted() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        val streamId = "streamId1"
        viewModel.mute(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        Assert.assertEquals(false, viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId }.audio!!.isEnabled)
    }

    @Test
    fun testStreamUiUnMuted() = runTest {
        viewModel = ParticipantsPanelViewModel { CollaborationViewModel.Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        callFlow.emit(callMock)
        runCurrent()
        val streamId = "streamId1"
        viewModel.mute(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        viewModel.unmute(viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId })
        Assert.assertEquals(true, viewModel.uiState.value.inCallStreamUi.value.first { it.id == streamId }.audio!!.isEnabled)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}