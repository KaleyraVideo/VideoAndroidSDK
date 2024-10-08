///*
// * Copyright 2023 Kaleyra @ https://www.kaleyra.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.kaleyra.video_sdk.viewmodel.call
//
//import android.content.Context
//import android.net.Uri
//import android.telecom.TelecomManager
//import android.util.Rational
//import android.util.Size
//import androidx.fragment.app.FragmentActivity
//import com.kaleyra.video.Company
//import com.kaleyra.video.conference.*
//import com.kaleyra.video.conference.Call
//import com.kaleyra.video.whiteboard.Whiteboard
//import com.kaleyra.video_common_ui.CallUI
//import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration.Success
//import com.kaleyra.video_common_ui.CompanyUI.Theme
//import com.kaleyra.video_common_ui.ConferenceUI
//import com.kaleyra.video_common_ui.ConnectionServiceOption
//import com.kaleyra.video_common_ui.DisplayModeEvent
//import com.kaleyra.video_common_ui.KaleyraVideo
//import com.kaleyra.video_common_ui.KaleyraVideoService
//import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
//import com.kaleyra.video_common_ui.callservice.KaleyraCallService
//import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
//import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils.isConnectionServiceSupported
//import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
//import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
//import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
//import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
//import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
//import com.kaleyra.video_common_ui.theme.CompanyThemeManager
//import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
//import com.kaleyra.video_sdk.MainDispatcherRule
//import com.kaleyra.video_sdk.call.appbar.model.Logo
//import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
//import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
//import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingTypeUi
//import com.kaleyra.video_sdk.call.screennew.model.CallStateUi
//import com.kaleyra.video_sdk.call.screen.viewmodel.CallViewModel
//import com.kaleyra.video_sdk.call.screen.viewmodel.CallViewModel.Companion.SINGLE_STREAM_DEBOUNCE_MILLIS
//import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
//import com.kaleyra.video_sdk.call.stream.arrangement.StreamsHandler
//import com.kaleyra.video_sdk.call.stream.arrangement.StreamsHandler.Companion.STREAMS_HANDLER_UPDATE_DEBOUNCE
//import com.kaleyra.video_sdk.call.stream.model.StreamUi
//import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
//import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
//import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
//import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
//import io.mockk.*
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.advanceTimeBy
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.runCurrent
//import kotlinx.coroutines.test.runTest
//import org.junit.*
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNotEquals
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class CallViewModelTest {
//
//    @get:Rule
//    var mainDispatcherRule = MainDispatcherRule()
//
//    private lateinit var viewModel: CallViewModel
//
//    private val conferenceMock = mockk<ConferenceUI>()
//
//    private val callMock = mockk<CallUI>(relaxed = true)
//
//    private val inputsMock = mockk<Inputs>(relaxed = true)
//
//    private val uriMock = mockk<Uri>()
//
//    private val viewMock = mockk<VideoStreamView>(relaxed = true)
//
//    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)
//
//    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)
//
//    private val streamMock1 = mockk<Stream>(relaxed = true)
//
//    private val streamMock2 = mockk<Stream>(relaxed = true)
//
//    private val streamMock3 = mockk<Stream>(relaxed = true)
//
//    private val streamMock4 = mockk<Stream>(relaxed = true)
//
//    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)
//
//    private val callParticipantsMock = mockk<CallParticipants>()
//
//    private val participantMeMock = mockk<CallParticipant.Me>()
//
//    private val participantMock1 = mockk<CallParticipant>()
//
//    private val participantMock2 = mockk<CallParticipant>()
//
//    private val recordingMock = mockk<Call.Recording>()
//
//    private val companyMock = mockk<Company>()
//
//    private val themeMock = mockk<Theme>()
//
//    private val dayLogo = mockk<Uri>()
//
//    private val nightLogo = mockk<Uri>()
//
//    @Before
//    fun setUp() {
//        mockkConstructor(StreamsHandler::class)
//        every { anyConstructed<StreamsHandler>().swapThumbnail(any()) } returns Unit
//        mockkObject(KaleyraVideo)
//        every { KaleyraVideo.isConfigured } returns true
//        every { KaleyraVideo.connectedUser } returns mockk(relaxed = true)
//        every { KaleyraVideo.conversation } returns mockk(relaxed = true)
//        mockkObject(CallUserMessagesProvider)
//        mockkObject(ContactDetailsManager)
//        mockkObject(CompanyThemeManager)
//        every { conferenceMock.call } returns MutableStateFlow(callMock)
//        with(recordingMock) {
//            every { type } returns Call.Recording.Type.OnConnect
//            every { state } returns MutableStateFlow(Call.Recording.State.Started)
//        }
//        with(callMock) {
//            every { inputs } returns inputsMock
//            every { participants } returns MutableStateFlow(callParticipantsMock)
//            every { recording } returns MutableStateFlow(recordingMock)
//            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
//            every { state } returns MutableStateFlow<Call.State>(Call.State.Disconnected)
//        }
//        with(callParticipantsMock) {
//            every { others } returns listOf(participantMock1, participantMock2)
//            every { me } returns participantMeMock
//            every { list } returns others + me!!
//            every { creator() } returns me
//        }
//        with(videoMock) {
//            every { id } returns "videoId"
//            every { view } returns MutableStateFlow(viewMock)
//            every { enabled } returns MutableStateFlow(true)
//        }
//        with(myVideoMock) {
//            every { id } returns "myVideoId"
//            every { view } returns MutableStateFlow(viewMock)
//            every { enabled } returns MutableStateFlow(true)
//        }
//        with(streamMock1) {
//            every { id } returns "streamId1"
//            every { video } returns MutableStateFlow(videoMock)
//        }
//        with(streamMock2) {
//            every { id } returns "streamId2"
//            every { video } returns MutableStateFlow(videoMock)
//        }
//        with(streamMock3) {
//            every { id } returns "streamId3"
//            every { video } returns MutableStateFlow(videoMock)
//        }
//        with(streamMock4) {
//            every { id } returns "streamId4"
//            every { video } returns MutableStateFlow(videoMock)
//        }
//        with(myStreamMock) {
//            every { id } returns "myStreamId"
//            every { video } returns MutableStateFlow(myVideoMock)
//            every { state } returns MutableStateFlow(Stream.State.Live)
//        }
//        with(participantMeMock) {
//            every { userId } returns "myUserId"
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
//            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
//            every { feedback } returns MutableStateFlow(null)
//        }
//        with(participantMock1) {
//            every { userId } returns "userId1"
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
//            every { combinedDisplayName } returns MutableStateFlow("displayName1")
//            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
//        }
//        with(participantMock2) {
//            every { userId } returns "userId2"
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//            every { streams } returns MutableStateFlow(listOf(streamMock3))
//            every { combinedDisplayName } returns MutableStateFlow("displayName2")
//            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
//        }
//        with(companyMock) {
//            every { name } returns MutableStateFlow("Kaleyra")
//            every { id } returns MutableStateFlow("companyId")
//        }
//        with(themeMock) {
//            every { day } returns mockk {
//                every { logo } returns dayLogo
//            }
//            every { night } returns mockk {
//                every { logo } returns nightLogo
//            }
//        }
//        every { companyMock.combinedTheme } returns flowOf(themeMock)
//        viewModel = spyk(CallViewModel { Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) })
//        mockkObject(KaleyraVideoService.Companion)
//        coEvery { KaleyraVideoService.get() } returns mockk(relaxed = true)
//    }
//
//    @After
//    fun teardown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun testShouldAskConnectionServicePermissionsFlag() = runTest {
//        mockkObject(ConnectionServiceUtils) {
//            advanceUntilIdle()
//            every { isConnectionServiceSupported } returns true
//            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enforced
//            assertEquals(true, viewModel.shouldAskConnectionServicePermissions)
//
//            every { isConnectionServiceSupported } returns false
//            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enforced
//            assertEquals(false, viewModel.shouldAskConnectionServicePermissions)
//
//            every { isConnectionServiceSupported } returns true
//            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled
//            assertEquals(true, viewModel.shouldAskConnectionServicePermissions)
//
//            every { isConnectionServiceSupported } returns true
//            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Disabled
//            assertEquals(false, viewModel.shouldAskConnectionServicePermissions)
//        }
//    }
//
//    @Test
//    fun testStartConnectionServiceIfItIsSupported() = runTest {
//        mockkObject(ConnectionServiceUtils)
//        mockkObject(TelecomManagerExtensions)
//        val context = mockk<Context>()
//        val telecomManager = mockk<TelecomManager>(relaxed = true)
//        every { context.getSystemService(Context.TELECOM_SERVICE) } returns telecomManager
//        every { telecomManager.addCall(callMock, null) } returns Unit
//        every { isConnectionServiceSupported } returns true
//
//        advanceUntilIdle()
//        viewModel.startConnectionService(context)
//        verify(exactly = 1) { telecomManager.addCall(callMock, null) }
//        unmockkObject(ConnectionServiceUtils)
//        unmockkObject(TelecomManagerExtensions)
//    }
//
//    @Test
//    fun testStartConnectionServiceIfItIsNotSupported() = runTest {
//        mockkObject(ConnectionServiceUtils)
//        mockkObject(TelecomManagerExtensions)
//        val context = mockk<Context>()
//        val telecomManager = mockk<TelecomManager>(relaxed = true)
//        every { context.getSystemService(Context.TELECOM_SERVICE) } returns telecomManager
//        every { telecomManager.addCall(callMock, null) } returns Unit
//        every { isConnectionServiceSupported } returns false
//
//        advanceUntilIdle()
//        viewModel.startConnectionService(context)
//        verify(exactly = 0) { telecomManager.addCall(callMock, null) }
//        unmockkObject(ConnectionServiceUtils)
//        unmockkObject(TelecomManagerExtensions)
//    }
//
//    @Test
//    fun testTryStartCallServiceWithConnectionServiceEnforced() = runTest {
//        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enforced
//
//        advanceUntilIdle()
//        viewModel.tryStartCallService()
//        verify(exactly = 1) { callMock.end() }
//    }
//
//    @Test
//    fun testTryStartCallServiceWithConnectionServiceDisabled() = runTest {
//        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Disabled
//
//        advanceUntilIdle()
//        viewModel.tryStartCallService()
//        verify(exactly = 1) { callMock.end() }
//    }
//
//    @Test
//    fun testTryStartCallServiceWithConnectionServiceEnabled() = runTest {
//        mockkObject(KaleyraCallService) {
//            every { KaleyraCallService.start() } returns Unit
//            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled
//
//            advanceUntilIdle()
//            viewModel.tryStartCallService()
//            verify(exactly = 1) { KaleyraCallService.start() }
//        }
//    }
//
//    @Test
//    fun `test streams updated after a debounce time if there is only one stream, there are still participants in call and the call is connected`() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        every { participantMock1.streams } returns MutableStateFlow(listOf())
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        advanceTimeBy(STREAMS_HANDLER_UPDATE_DEBOUNCE + 1)
//        val current = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        assertEquals(listOf<String>(), current)
//        advanceTimeBy(SINGLE_STREAM_DEBOUNCE_MILLIS)
//        val new = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        assertEquals(listOf(myStreamMock.id), new)
//    }
//
//    @Test
//    fun `test streams cleaned on call ended`() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
//        every { callMock.state } returns callState
//        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        advanceUntilIdle()
//        val featured = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        val thumbnail = viewModel.uiState.first().thumbnailStreams.value.map { it.id }
//        assertEquals(listOf(streamMock1.id), featured)
//        assertEquals(listOf(myStreamMock.id), thumbnail)
//        callState.value = Call.State.Disconnected.Ended
//        advanceUntilIdle()
//        val newFeatured = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        val newThumbnail = viewModel.uiState.first().thumbnailStreams.value.map { it.id }
//        assertEquals(listOf<String>(), newFeatured)
//        assertEquals(listOf<String>(), newThumbnail)
//    }
//
//    @Test
//    fun `test streams updated immediately if there is more than one participant in call`() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        with(participantMock1) {
//            every { streams } returns MutableStateFlow(listOf())
//            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
//        }
//        with(participantMock2) {
//            every { streams } returns MutableStateFlow(listOf())
//            every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
//        }
//        with(participantMeMock) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        advanceTimeBy(STREAMS_HANDLER_UPDATE_DEBOUNCE + 1)
//        val current = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        assertEquals(listOf(myStreamMock.id), current)
//    }
//
//    @Test
//    fun `test streams updated immediately if the call is not connected`() = runTest {
//        every { callMock.state } returns MutableStateFlow(mockk(relaxed = true))
//        with(participantMock1) {
//            every { streams } returns MutableStateFlow(listOf())
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        with(participantMock2) {
//            every { streams } returns MutableStateFlow(listOf())
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        with(participantMeMock) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        advanceTimeBy(STREAMS_HANDLER_UPDATE_DEBOUNCE + 1)
//        val current = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        assertEquals(listOf(myStreamMock.id), current)
//    }
//
//    @Test
//    fun `test streams updated immediately if there are more than one stream`() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        with(participantMock1) {
//            every { streams } returns MutableStateFlow(listOf())
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        with(participantMock2) {
//            every { streams } returns MutableStateFlow(listOf(streamMock2))
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        with(participantMeMock) {
//            every { streams } returns MutableStateFlow(listOf(myStreamMock))
//            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
//        }
//        advanceTimeBy(STREAMS_HANDLER_UPDATE_DEBOUNCE + 1)
//        val currentFeatured = viewModel.uiState.first().featuredStreams.value.map { it.id }
//        val currentThumbnail = viewModel.uiState.first().thumbnailStreams.value.map { it.id }
//        assertEquals(listOf(streamMock2.id), currentFeatured)
//        assertEquals(listOf(myStreamMock.id), currentThumbnail)
//    }
//
//    @Test
//    fun testCallUiState_callStateUpdated() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        val current = viewModel.uiState.first().callState
//        assertEquals(CallStateUi.Disconnected, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().callState
//        assertEquals(CallStateUi.Connected, new)
//    }
//
//    @Test
//    fun testCallUiState_featuredStreamsUpdated() = runTest {
//        val current = viewModel.uiState.first().featuredStreams
//        assertEquals(ImmutableList<StreamUi>(listOf()), current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().featuredStreams
//        val featuredStreamsIds = new.value.map { it.id }
//        assertEquals(listOf(streamMock1.id, streamMock2.id), featuredStreamsIds)
//    }
//
//    @Test
//    fun testCallUiState_thumbnailStreamsUpdated() = runTest {
//        val current = viewModel.uiState.first().thumbnailStreams
//        assertEquals(ImmutableList<StreamUi>(listOf()), current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().thumbnailStreams
//        val thumbnailStreamsIds = new.value.map { it.id }
//        assertEquals(listOf(streamMock3.id, myStreamMock.id), thumbnailStreamsIds)
//    }
//
//    @Test
//    fun testCallUiState_streamsDoNotContainsMyScreenShare() = runTest {
//        with(callParticipantsMock) {
//            every { others } returns listOf()
//            every { me } returns participantMeMock
//            every { list } returns others + me!!
//        }
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        every { myStreamMock.id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
//        advanceUntilIdle()
//        val featured = viewModel.uiState.first().featuredStreams
//        val thumbnails = viewModel.uiState.first().thumbnailStreams
//        val streams = featured.value + thumbnails.value
//        assertEquals(listOf<String>(), streams.map { it.id })
//    }
//
//    @Test
//    fun testCallUiState_isGroupCallUpdated() = runTest {
//        val current = viewModel.uiState.first().isGroupCall
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().isGroupCall
//        assertEquals(true, new)
//    }
//
//    @Test
//    fun testCallUiState_isRecordingUpdated() = runTest {
//        val current = viewModel.uiState.first().recording?.state
//        assertEquals(null, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().recording?.state
//        assertEquals(RecordingStateUi.Started, new)
//    }
//
//    @Test
//    fun testCallUiState_recordingTypeUpdated() = runTest {
//        val current = viewModel.uiState.first().recording?.type
//        assertEquals(null, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().recording?.type
//        assertEquals(RecordingTypeUi.OnConnect, new)
//    }
//
//    @Test
//    fun testCallUiState_isAudioOnlyUpdated() = runTest {
//        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioOnly())
//        val current = viewModel.uiState.first().isAudioOnly
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().isAudioOnly
//        assertEquals(true, new)
//    }
//
//    @Test
//    fun testCallUiState_callIsConnectedAndItsAudioVideo_shouldAutoHideSheetUpdated() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioVideo())
//        every { videoMock.enabled } returns MutableStateFlow(false)
//        every { myVideoMock.enabled } returns MutableStateFlow(false)
//        val current = viewModel.uiState.first().shouldAutoHideSheet
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().shouldAutoHideSheet
//        assertEquals(true, new)
//    }
//
//    @Test
//    fun testCallUiState_callIsConnectedAndAParticipantHasVideoEnabled_shouldAutoHideSheetUpdated() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
//        every { callMock.preferredType } returns  MutableStateFlow(Call.PreferredType.audioUpgradable())
//        every { videoMock.enabled } returns MutableStateFlow(true)
//        every { myVideoMock.enabled } returns MutableStateFlow(false)
//        val current = viewModel.uiState.first().shouldAutoHideSheet
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().shouldAutoHideSheet
//        assertEquals(true, new)
//    }
//
//    @Test
//    fun testCallUiState_watermarkInfoUpdated() = runTest {
//        advanceUntilIdle()
//        val actual = viewModel.uiState.first().watermarkInfo
//        assertEquals(WatermarkInfo(text = "Kaleyra", logo = Logo(dayLogo, nightLogo)), actual)
//    }
//
//    @Test
//    fun testCallUiState_callEndedHungUp_showFeedbackUpdated() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
//        with(callMock) {
//            every { withFeedback } returns true
//            every { state } returns callState
//        }
//        advanceUntilIdle()
//
//        val previous = viewModel.uiState.first().showFeedback
//        assertEquals(false, previous)
//
//        callState.value = Call.State.Disconnected.Ended.HungUp()
//        advanceUntilIdle()
//
//        val current = viewModel.uiState.first().showFeedback
//        assertEquals(true, current)
//    }
//
//    @Test
//    fun testCallUiState_callEndedError_showFeedbackUpdated() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
//        with(callMock) {
//            every { withFeedback } returns true
//            every { state } returns callState
//        }
//        advanceUntilIdle()
//
//        val previous = viewModel.uiState.first().showFeedback
//        assertEquals(false, previous)
//
//        callState.value = Call.State.Disconnected.Ended.Error
//        advanceUntilIdle()
//
//        val current = viewModel.uiState.first().showFeedback
//        assertEquals(true, current)
//    }
//
//    @Test
//    fun testCallUiState_callEndedGeneric_showFeedbackNotUpdated() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
//        with(callMock) {
//            every { withFeedback } returns true
//            every { state } returns callState
//        }
//        advanceUntilIdle()
//
//        val previous = viewModel.uiState.first().showFeedback
//        assertEquals(false, previous)
//
//        callState.value = Call.State.Disconnected.Ended
//        advanceUntilIdle()
//
//        val current = viewModel.uiState.first().showFeedback
//        assertEquals(false, current)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNotUpdatedIfCallIsEnded() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
//        every { participantMock1.streams } returns MutableStateFlow(listOf())
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        val current = viewModel.uiState.first().amILeftAlone
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().amILeftAlone
//        assertEquals(false, new)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNotUpdatedIfCallIsDisconnecting() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Disconnecting)
//        every { participantMock1.streams } returns MutableStateFlow(listOf())
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        val current = viewModel.uiState.first().amILeftAlone
//        assertEquals(false, current)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().amILeftAlone
//        assertEquals(false, new)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneUpdated() = runTest {
//        val participants1StreamsFlow = MutableStateFlow(listOf(streamMock1))
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceUntilIdle()
//        val current = viewModel.uiState.first().amILeftAlone
//        assertEquals(false, current)
//        participants1StreamsFlow.value = listOf()
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().amILeftAlone
//        assertEquals(true, new)
//    }
//
//    @Test
//    fun testCallUiState_waitingForOthersUpdatedAfterDebounceIfValueIsTrue() = runTest {
//        every { participantMock1.streams } returns MutableStateFlow(listOf())
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceTimeBy(CallViewModel.WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
//        assertEquals(false, viewModel.uiState.first().amIWaitingOthers)
//        advanceTimeBy(1)
//        assertEquals(true, viewModel.uiState.first().amIWaitingOthers)
//    }
//
//    @Test
//    fun testCallUiState_waitingForOthersNotUpdatedIfSomeonePreviouslyJoined() = runTest {
//        val participants1StreamsFlow = MutableStateFlow(listOf<Stream>())
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceUntilIdle()
//        assertEquals(true, viewModel.uiState.first().amIWaitingOthers)
//        participants1StreamsFlow.value = listOf(streamMock1)
//        advanceUntilIdle()
//        assertEquals(false, viewModel.uiState.first().amIWaitingOthers)
//        participants1StreamsFlow.value = listOf()
//        advanceUntilIdle()
//        assertEquals(false, viewModel.uiState.first().amIWaitingOthers)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneUpdatedAfterDebounceIfValueIsTrue() = runTest {
//        val participants1StreamsFlow = MutableStateFlow(listOf(streamMock1))
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceUntilIdle()
//        participants1StreamsFlow.value = listOf()
//        advanceTimeBy(CallViewModel.AM_I_LEFT_ALONE_DEBOUNCE_MILLIS)
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//        advanceTimeBy(1)
//        assertEquals(true, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneUpdatedImmediatelyIfValueIsFalse() = runTest {
//        val participants1StreamsFlow = MutableStateFlow(listOf(streamMock1))
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceUntilIdle()
//
//        participants1StreamsFlow.value = listOf()
//        advanceTimeBy(CallViewModel.AM_I_LEFT_ALONE_DEBOUNCE_MILLIS)
//        runCurrent()
//        assertEquals(true, viewModel.uiState.first().amILeftAlone)
//
//        participants1StreamsFlow.value = listOf(streamMock1)
//        advanceTimeBy(1)
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNotUpdatedIfCallIsDialing() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
//        every { callParticipantsMock.creator() } returns participantMeMock
//        every { participantMock1.streams } returns MutableStateFlow(listOf())
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceTimeBy(CallViewModel.AM_I_LEFT_ALONE_DEBOUNCE_MILLIS)
//        runCurrent()
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNoLongerUpdatedAfterCallIsDisconnecting() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connecting)
//        every { callMock.state } returns callState
//        every { callParticipantsMock.creator() } returns participantMeMock
//        val participants1StreamsFlow = MutableStateFlow(listOf(streamMock1))
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        runCurrent()
//        callState.value = Call.State.Connected
//
//        runCurrent()
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//
//        participants1StreamsFlow.value = listOf()
//        callState.value = Call.State.Disconnecting
//        advanceTimeBy(1)
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNoLongerUpdatedAfterCallIsEnded() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connecting)
//        every { callMock.state } returns callState
//        every { callParticipantsMock.creator() } returns participantMeMock
//        val participants1StreamsFlow = MutableStateFlow(listOf(streamMock1))
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        runCurrent()
//        callState.value = Call.State.Connected
//
//        runCurrent()
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//
//        participants1StreamsFlow.value = listOf()
//        callState.value = Call.State.Disconnected.Ended
//        advanceTimeBy(1)
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testCallUiState_amILeftAloneNotUpdatedIfCallIsRinging() = runTest {
//        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
//        every { callParticipantsMock.creator() } returns participantMock1
//        val participants1StreamsFlow = MutableStateFlow(listOf<Stream>())
//        every { participantMock1.streams } returns participants1StreamsFlow
//        every { participantMock2.streams } returns MutableStateFlow(listOf())
//        advanceTimeBy(CallViewModel.AM_I_LEFT_ALONE_DEBOUNCE_MILLIS)
//        runCurrent()
//        assertEquals(false, viewModel.uiState.first().amILeftAlone)
//    }
//
//    @Test
//    fun testStartMicrophone() = runTest {
//        val audioMock = mockk<Input.Audio>(relaxed = true)
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns CAMERA_STREAM_ID
//            every { audio } returns MutableStateFlow(null)
//            every { video } returns MutableStateFlow(null)
//        }
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        val contextMock = mockk<FragmentActivity>()
//        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(audioMock)
//
//        advanceUntilIdle()
//        viewModel.startMicrophone(contextMock)
//
//        advanceUntilIdle()
//        coVerify { inputsMock.request(contextMock, Inputs.Type.Microphone) }
//    }
//
//    @Test
//    fun testStartMicrophone_audioIsAlreadyInitialized_audioIsNotStarted() = runTest {
//        val audioMock = mockk<Input.Audio>(relaxed = true)
//        val newAudioMock = mockk<Input.Audio>(relaxed = true)
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns CAMERA_STREAM_ID
//            every { audio } returns MutableStateFlow(audioMock)
//            every { video } returns MutableStateFlow(null)
//        }
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        val contextMock = mockk<FragmentActivity>()
//        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(newAudioMock)
//
//        advanceUntilIdle()
//        viewModel.startMicrophone(contextMock)
//
//        advanceUntilIdle()
//        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Microphone) }
//    }
//
//    @Test
//    fun testStartCamera() = runTest {
//        val cameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
//            every { enabled } returns MutableStateFlow(true)
//            every { view } returns MutableStateFlow(null)
//        }
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns CAMERA_STREAM_ID
//            every { video } returns MutableStateFlow(null)
//            every { audio } returns MutableStateFlow(null)
//        }
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        val contextMock = mockk<FragmentActivity>()
//        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(cameraMock)
//
//        advanceUntilIdle()
//        viewModel.startCamera(contextMock)
//
//        advanceUntilIdle()
//        coVerify { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
//    }
//
//    @Test
//    fun testStartCamera_videoIsAlreadyInitialized_cameraIsNotStarted() = runTest {
//        val cameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
//            every { view } returns MutableStateFlow(null)
//            every { enabled } returns MutableStateFlow(true)
//        }
//        val newCameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true){
//            every { view } returns MutableStateFlow(null)
//            every { enabled } returns MutableStateFlow(true)
//        }
//        val myStreamMock = mockk<Stream.Mutable>(relaxed = true) {
//            every { id } returns CAMERA_STREAM_ID
//            every { video } returns MutableStateFlow(cameraMock)
//            every { audio } returns MutableStateFlow(null)
//        }
//        every { participantMeMock.streams } returns MutableStateFlow(listOf(myStreamMock))
//        val contextMock = mockk<FragmentActivity>()
//        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(newCameraMock)
//
//        advanceUntilIdle()
//        viewModel.startCamera(contextMock)
//
//        advanceUntilIdle()
//        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
//        assertEquals(cameraMock, myStreamMock.video.value)
//    }
//
//    @Test
//    fun testFullscreenStream() = runTest {
//        viewModel.fullscreenStream(streamMock1.id)
//        advanceUntilIdle()
//        val fullscreenStream = viewModel.uiState.first().fullscreenStream
//        assertEquals(streamMock1.id, fullscreenStream?.id)
//    }
//
//    @Test
//    fun testUpdateStreamArrangement_isNotMediumSizeDevice_twoMaxFeaturedStreams() = runTest {
//        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock4))
//        viewModel.updateStreamsArrangement(false)
//        advanceUntilIdle()
//        val actual = viewModel.uiState.first().featuredStreams
//        assertEquals(2, actual.count())
//    }
//
//    @Test
//    fun testUpdateStreamArrangement_isMediumSizeDevice_fourMaxFeaturedStreams() = runTest {
//        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2, streamMock4))
//        viewModel.updateStreamsArrangement(true)
//        advanceUntilIdle()
//        val actual = viewModel.uiState.first().featuredStreams
//        assertEquals(4, actual.count())
//    }
//
//    @Test
//    fun testSwapThumbnail() {
//        viewModel.swapThumbnail("streamId")
//        verify { anyConstructed<StreamsHandler>().swapThumbnail("streamId") }
//    }
//
//    @Test
//    fun testSendUserFeedback() = runTest {
//        advanceUntilIdle()
//        viewModel.sendUserFeedback(3f, "comment")
//        val actual = participantMeMock.feedback.first()
//        val expected = CallParticipant.Me.Feedback(3, "comment")
//        assertEquals(expected, actual)
//    }
//
//    @Test
//    fun `if the onCallEnded callback is set after the call state is set to ended with error, the lambda is immediately invoked`() = runTest {
//        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended.Error)
//        advanceUntilIdle()
//        var hasFeedback: Boolean? = null
//        var hasErrorOccurred: Boolean? = null
//        var hasBeenKicked: Boolean? = null
//        viewModel.setOnCallEnded { feedback, error, kicked ->
//            hasFeedback = feedback
//            hasErrorOccurred = error
//            hasBeenKicked = kicked
//        }
//        advanceUntilIdle()
//        assertEquals(false, hasFeedback)
//        assertEquals(true, hasErrorOccurred)
//        assertEquals(false, hasBeenKicked)
//    }
//
//    @Test
//    fun `if the onCallEnded callback is set after the call state is set to ended, the lambda is immediately invoked`() = runTest {
//        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnected.Ended)
//        advanceUntilIdle()
//        var hasFeedback: Boolean? = null
//        var hasErrorOccurred: Boolean? = null
//        var hasBeenKicked: Boolean? = null
//        viewModel.setOnCallEnded { feedback, error, kicked ->
//            hasFeedback = feedback
//            hasErrorOccurred = error
//            hasBeenKicked = kicked
//        }
//        advanceUntilIdle()
//        assertEquals(false, hasFeedback)
//        assertEquals(false, hasErrorOccurred)
//        assertEquals(false, hasBeenKicked)
//    }
//
//    @Test
//    fun `if the onCallEnded callback is set after the call state is set to disconnecting, the lambda is immediately invoked`() = runTest {
//        every { callMock.state } returns MutableStateFlow<Call.State>(Call.State.Disconnecting)
//        advanceUntilIdle()
//        var hasFeedback: Boolean? = null
//        var hasErrorOccurred: Boolean? = null
//        var hasBeenKicked: Boolean? = null
//        viewModel.setOnCallEnded { feedback, error, kicked ->
//            hasFeedback = feedback
//            hasErrorOccurred = error
//            hasBeenKicked = kicked
//        }
//        advanceUntilIdle()
//        assertEquals(false, hasFeedback)
//        assertEquals(false, hasErrorOccurred)
//        assertEquals(false, hasBeenKicked)
//    }
//
//    @Test
//    fun `if the onAspectRatio callback is set after the new aspect ratio is received, the lambda is immediately invoked`() = runTest {
//        val mockSize = mockk<Size> {
//            every { width } returns 1080
//            every { height } returns 1920
//        }
//        every { viewMock.videoSize } returns MutableStateFlow(mockSize)
//        advanceUntilIdle()
//        var actual: Rational? = null
//        viewModel.setOnPipAspectRatio {
//            actual = it
//        }
//        advanceUntilIdle()
//        assertNotEquals(null, actual)
//    }
//
//    @Test
//    fun `if the onAudioOrVideoChanged callback is set after the preferred type is received, the lambda is immediately invoked`() = runTest {
//        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
//        advanceUntilIdle()
//        var actualAudio = false
//        var actualVideo = false
//        viewModel.setOnAudioOrVideoChanged { audio, video ->
//            actualAudio = audio
//            actualVideo = video
//        }
//        advanceUntilIdle()
//        assertEquals(true, actualAudio)
//        assertEquals(true, actualVideo)
//    }
//
//    @Test
//    fun `if the onDisplayMode callback is set after the displayModeEvent is received, the lambda is immediately invoked`() = runTest {
//        every { callMock.displayModeEvent } returns MutableStateFlow(DisplayModeEvent("id", CallUI.DisplayMode.PictureInPicture))
//        advanceUntilIdle()
//        var actual: CallUI.DisplayMode? = null
//        viewModel.setOnDisplayMode { displayMode ->
//            actual = displayMode
//        }
//        advanceUntilIdle()
//        assertEquals(CallUI.DisplayMode.PictureInPicture, actual)
//    }
//
//    @Test
//    fun `if the onUsbCameraConnected callback is set after the isUsbConnected is received, the lambda is immediately invoked`() = runTest {
//        val usbCamera = mockk<Input.Video.Camera.Usb>(relaxed = true)
//        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbCamera))
//        every { usbCamera.state } returns MutableStateFlow(Input.State.Closed.AwaitingPermission)
//        advanceUntilIdle()
//        var connected = false
//        viewModel.setOnUsbCameraConnected {
//            connected = true
//        }
//        advanceUntilIdle()
//        assertEquals(true, connected)
//    }
//
//    @Test
//    fun fullscreenStreamRemovedFromStreams_fullscreenStreamIsNull() = runTest {
//        val participantStreams = MutableStateFlow(listOf(streamMock1, streamMock2))
//        every { participantMock1.streams } returns participantStreams
//        viewModel.fullscreenStream(streamMock1.id)
//        advanceUntilIdle()
//        val actual = viewModel.uiState.first().fullscreenStream
//        assertEquals(streamMock1.id, actual?.id)
//
//        participantStreams.value = listOf(streamMock2)
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().fullscreenStream
//        assertEquals(null, new?.id)
//    }
//
//    @Test
//    fun `fullscreen stream is set to null on reconnecting call state`() = runTest {
//        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
//        every { callMock.state } returns callState
//        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
//        viewModel.fullscreenStream(streamMock1.id)
//        advanceUntilIdle()
//        val actual = viewModel.uiState.first().fullscreenStream
//        assertEquals(streamMock1.id, actual?.id)
//
//        callState.value = Call.State.Reconnecting
//        advanceUntilIdle()
//        val new = viewModel.uiState.first().fullscreenStream
//        assertEquals(null, new?.id)
//    }
//
//    @Test
//    fun testUserMessage() = runTest {
//        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
//        advanceUntilIdle()
//        val actual = viewModel.userMessage.first()
//        assert(actual is MutedMessage && actual.admin == "admin")
//    }
//
//    @Test
//    fun testShowWhiteboardRequestReceived() = runTest {
//        every { callMock.whiteboard.events } returns MutableStateFlow(Whiteboard.Event.Request.Show("userId1"))
//        val actual = viewModel.whiteboardRequest.first()
//        assertEquals(true, actual is WhiteboardRequest.Show)
//        assertEquals("displayName1", actual.username)
//    }
//
//    @Test
//    fun testHideWhiteboardRequestReceived() = runTest {
//        every { callMock.whiteboard.events } returns MutableStateFlow(Whiteboard.Event.Request.Hide("userId1"))
//        val actual = viewModel.whiteboardRequest.first()
//        assertEquals(true, actual is WhiteboardRequest.Hide)
//        assertEquals("displayName1", actual.username)
//    }
//}
