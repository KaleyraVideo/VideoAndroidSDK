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

package com.kaleyra.video_sdk.viewmodel.call

import android.content.Context
import android.telecom.TelecomManager
import androidx.fragment.app.FragmentActivity
import com.kaleyra.video.Company
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration.Success
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.DisplayModeEvent
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils.isConnectionServiceSupported
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import com.kaleyra.video_common_ui.theme.CompanyThemeManager
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper
import com.kaleyra.video_sdk.call.mapper.InputMapper.isUsbCameraWaitingPermission
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.getWhiteboardRequestEvents
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel.Companion.NULL_CALL_TIMEOUT
import com.kaleyra.video_sdk.call.utils.CallExtensions
import com.kaleyra.video_sdk.call.utils.CallExtensions.toMyCameraStream
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        every { KaleyraVideo.connectedUser } returns mockk(relaxed = true)
        every { KaleyraVideo.conversation } returns mockk(relaxed = true)
        mockkObject(ContactDetailsManager)
        mockkObject(CompanyThemeManager)
        mockkObject(CallStateMapper)
        mockkObject(InputMapper)
        mockkObject(ConnectionServiceUtils)
        mockkObject(TelecomManagerExtensions)
        mockkObject(CallExtensions)
        mockkObject(WhiteboardMapper)

        every { callMock.toCallStateUi() } returns MutableStateFlow(CallStateUi.Disconnected)
        every { callMock.isUsbCameraWaitingPermission() } returns flowOf(false)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testTheme() = runTest {
        val themeMock = mockk<CompanyUI.Theme>()
        val companyMock = mockk<Company>()
        every { companyMock.combinedTheme } returns flowOf(themeMock)

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) }
        advanceUntilIdle()

        assertEquals(themeMock, viewModel.theme.first())
    }

    @Test
    fun testShouldAskConnectionServicePermissions() = runTest {
        mockkObject(ConnectionServiceUtils)
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        assertEquals(
            true,
            viewModel.shouldAskConnectionServicePermissions
        )
    }

    @Test
    fun connectionsServiceIsNotSupported_shouldAskConnectionServicePermissionsIsFalse() = runTest {
        mockkObject(ConnectionServiceUtils)
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns false
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        assertEquals(
            false,
            viewModel.shouldAskConnectionServicePermissions
        )
    }

    @Test
    fun connectionServiceOptionIsDisabled_shouldAskConnectionServicePermissionsIsFalse() = runTest {
        mockkObject(ConnectionServiceUtils)
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Disabled

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        assertEquals(
            false,
            viewModel.shouldAskConnectionServicePermissions
        )
    }

    @Test
    fun `kaleyra video object not configured, request configuration called`() = runTest {
        every { KaleyraVideo.isConfigured } returns false
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoInitializationProviderKt")
        coEvery { requestConfiguration() } returns mockk(relaxed = true)

        MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        coVerify(exactly = 1) { requestConfiguration() }
    }

    @Test
    fun `conversation state disconnected, requestConnect called`() = runTest {
        every { KaleyraVideo.conversation.state } returns MutableStateFlow(State.Disconnected)
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoInitializationProviderKt")
        coEvery { requestConnect() } returns mockk(relaxed = true)

        MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        coVerify(exactly = 1) { requestConnect() }
    }

    @Test
    fun testMainUiState_isEndedUpdated() = runTest {
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toCallStateUi() } returns callState

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        val current = viewModel.uiState.first().isCallEnded
        assertEquals(false, current)

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        val new = viewModel.uiState.first().isCallEnded
        assertEquals(true, new)
    }

    @Test
    fun testStartConnectionServiceIfItIsSupported() = runTest {
        val context = mockk<Context>()
        val telecomManager = mockk<TelecomManager>(relaxed = true)
        every { context.getSystemService(Context.TELECOM_SERVICE) } returns telecomManager
        every { telecomManager.addCall(callMock, null) } returns Unit
        every { isConnectionServiceSupported } returns true

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startConnectionService(context)
        verify(exactly = 1) { telecomManager.addCall(callMock, null) }
    }

    @Test
    fun testStartConnectionServiceIfItIsNotSupported() = runTest {
        val context = mockk<Context>()
        val telecomManager = mockk<TelecomManager>(relaxed = true)
        every { context.getSystemService(Context.TELECOM_SERVICE) } returns telecomManager
        every { telecomManager.addCall(callMock, null) } returns Unit
        every { isConnectionServiceSupported } returns false

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startConnectionService(context)
        verify(exactly = 0) { telecomManager.addCall(callMock, null) }
    }

    @Test
    fun testTryStartCallServiceWithConnectionServiceEnforced() = runTest {
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enforced

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.tryStartCallService()
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testTryStartCallServiceWithConnectionServiceDisabled() = runTest {
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Disabled

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.tryStartCallService()
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testTryStartCallServiceWithConnectionServiceEnabled() = runTest {
        mockkObject(KaleyraCallService) {
            every { KaleyraCallService.start() } returns Unit
            every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled

            val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

            advanceUntilIdle()
            viewModel.tryStartCallService()
            verify(exactly = 1) { KaleyraCallService.start() }
        }
    }

    @Test
    fun testStartMicrophone() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { toMyCameraStream()!!.audio.value } returns null
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Microphone) }
    }

    @Test
    fun myStreamAudioAlreadySet_microphoneInputIsNotRequested() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { toMyCameraStream()!!.audio.value } returns mockk()
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Microphone) }
    }

    @Test
    fun meParticipantNull_microphoneInputRequestedAfterMeIsSet() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(participantsMock)
        with(callMock) {
            every { participants } returns participantsFlow
            every { toMyCameraStream()!!.audio.value } returns null
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Microphone) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startMicrophone(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Microphone) }

        val newParticipantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        participantsFlow.value = newParticipantsMock

        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Microphone) }
    }

    @Test
    fun testStartCamera() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { toMyCameraStream()!!.video.value } returns null
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(mockk())
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.External) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Camera.External) }
    }

    @Test
    fun myStreamVideoAlreadySet_cameraInputsAreNotRequested() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { toMyCameraStream()!!.video.value } returns mockk()
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(mockk())
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.External) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.External) }
    }

    @Test
    fun meParticipantNull_cameraInputRequestedAfterMeIsSet() = runTest {
        val contextMock = mockk<FragmentActivity>()
        val inputsMock = mockk<Inputs>()
        val participantsMock = mockk<CallParticipants> {
            every { me } returns null
        }
        val participantsFlow = MutableStateFlow(participantsMock)
        with(callMock) {
            every { participants } returns participantsFlow
            every { toMyCameraStream()!!.video.value } returns null
            every { inputs } returns inputsMock
        }
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) } returns Inputs.RequestResult.Success(mockk())
        coEvery { inputsMock.request(contextMock, Inputs.Type.Camera.External) } returns Inputs.RequestResult.Success(mockk())

        val viewModel = spyk(MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) })

        advanceUntilIdle()
        viewModel.startCamera(contextMock)

        advanceUntilIdle()
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        coVerify(exactly = 0) { inputsMock.request(contextMock, Inputs.Type.Camera.External) }

        val newParticipantsMock = mockk<CallParticipants> {
            every { me } returns mockk()
        }
        participantsFlow.value = newParticipantsMock

        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Camera.Internal) }
        coVerify(exactly = 1) { inputsMock.request(contextMock, Inputs.Type.Camera.External) }
    }

    @Test
    fun `hasFeedback flag false when call is ended and with withFeedback call flag is true`() = runTest {
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toCallStateUi() } returns callState
        every { callMock.withFeedback } returns false
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null


        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `hasFeedback flag false when call is ended and has not been previously connected`() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Disconnected.Ended)
        every { callMock.withFeedback } returns true
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null


        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `hasFeedback flag true when call is ended and withFeedback call flag is true and the call has been connected`() = runTest {
        val callState = MutableStateFlow<CallStateUi>(CallStateUi.Connected)
        every { callMock.toCallStateUi() } returns callState
        every { callMock.withFeedback } returns true
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null


        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()

        callState.value = CallStateUi.Disconnected.Ended
        advanceUntilIdle()

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(true, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `if the onCallEnded callback is set after the call state is set to ended with error, the lambda is immediately invoked`() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Disconnected.Ended.Error)
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null


        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(true, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `if the onCallEnded callback is set after the call state is set to kicked, the lambda is immediately invoked`() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Disconnected.Ended.Kicked("name"))
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(true, hasBeenKicked)
    }

    @Test
    fun `if the onCallEnded callback is set after the call state is set to ended, the lambda is immediately invoked`() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Disconnected.Ended)
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `if the call is not set within the timeout, the onCallEnded is invoked`() = runTest {
        every { conferenceMock.call } returns MutableSharedFlow()
        advanceUntilIdle()
        var isInvoked = false

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnCallEnded { _, _, _ ->
            isInvoked = true
        }

        advanceTimeBy(NULL_CALL_TIMEOUT)
        assertEquals(false, isInvoked)

        advanceTimeBy(1)
        assertEquals(true, isInvoked)
    }

    @Test
    fun `if the onCallEnded callback is set after the call state is set to disconnecting, the lambda is immediately invoked`() = runTest {
        every { callMock.toCallStateUi() } returns MutableStateFlow<CallStateUi>(CallStateUi.Disconnecting)
        advanceUntilIdle()
        var hasFeedback: Boolean? = null
        var hasErrorOccurred: Boolean? = null
        var hasBeenKicked: Boolean? = null

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnCallEnded { feedback, error, kicked ->
            hasFeedback = feedback
            hasErrorOccurred = error
            hasBeenKicked = kicked
        }
        advanceUntilIdle()
        assertEquals(false, hasFeedback)
        assertEquals(false, hasErrorOccurred)
        assertEquals(false, hasBeenKicked)
    }

    @Test
    fun `if the onAudioOrVideoChanged callback is set after the preferred type is received, the lambda is immediately invoked`() = runTest {
        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        advanceUntilIdle()
        var actualAudio = false
        var actualVideo = false

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnAudioOrVideoChanged { audio, video ->
            actualAudio = audio
            actualVideo = video
        }
        advanceUntilIdle()
        assertEquals(true, actualAudio)
        assertEquals(true, actualVideo)
    }

    @Test
    fun `if the onDisplayMode callback is set after the displayModeEvent is received, the lambda is immediately invoked`() = runTest {
        every { callMock.displayModeEvent } returns MutableStateFlow(DisplayModeEvent("id", CallUI.DisplayMode.PictureInPicture))
        advanceUntilIdle()
        var actual: CallUI.DisplayMode? = null

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnDisplayMode { displayMode ->
            actual = displayMode
        }
        advanceUntilIdle()
        assertEquals(CallUI.DisplayMode.PictureInPicture, actual)
    }

    @Test
    fun `if the onUsbCameraConnected callback is set after the isUsbConnected is received, the lambda is immediately invoked`() = runTest {
        val usbCamera = mockk<Input.Video.Camera.Usb>(relaxed = true)
        val inputsMock = mockk<Inputs>(relaxed = true)
        every { callMock.inputs } returns inputsMock
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(usbCamera))
        every { usbCamera.state } returns MutableStateFlow(Input.State.Closed.AwaitingPermission)
        advanceUntilIdle()
        var connected = false

        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }

        viewModel.setOnUsbCameraConnected {
            connected = true
        }
        advanceUntilIdle()
        assertEquals(true, connected)
    }

    @Test
    fun testShowWhiteboardRequestReceived() = runTest {
        every { callMock.getWhiteboardRequestEvents() } returns MutableStateFlow(WhiteboardRequest.Show("displayName1"))
        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        val actual = viewModel.whiteboardRequest.first()
        assertEquals(true, actual is WhiteboardRequest.Show)
        assertEquals("displayName1", actual.username)
    }

    @Test
    fun testHideWhiteboardRequestReceived() = runTest {
        every { callMock.getWhiteboardRequestEvents() } returns MutableStateFlow(WhiteboardRequest.Hide("displayName1"))
        val viewModel = MainViewModel { Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        val actual = viewModel.whiteboardRequest.first()
        assertEquals(true, actual is WhiteboardRequest.Hide)
        assertEquals("displayName1", actual.username)
    }

    @Test
    fun oneToOneCall_getOtherUserIdIsSuccessful() = runTest {
        val participant = mockk<CallParticipant> {
            every { userId } returns "participantId"
        }
        val companyParticipant = mockk<CallParticipant> {
            every { userId } returns "companyId"
        }
        val participants = mockk<CallParticipants> {
            every { others } returns listOf(participant, companyParticipant)
        }
        val company = mockk<Company>(relaxed = true)
        every { callMock.participants } returns MutableStateFlow(participants)
        every { company.id } returns MutableStateFlow("companyId")
        val viewModel = MainViewModel { Success(conferenceMock, mockk(), company, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        val actual = viewModel.getOtherUserId()
        assertEquals(participant.userId, actual)
    }

    @Test
    fun groupCall_getOtherUserIdIsNull() = runTest {
        val participant1 = mockk<CallParticipant> {
            every { userId } returns "participantId1"
        }
        val participant2 = mockk<CallParticipant> {
            every { userId } returns "participantId2"
        }
        val participants = mockk<CallParticipants> {
            every { others } returns listOf(participant1, participant2)
        }
        val company = mockk<Company>(relaxed = true)
        every { callMock.participants } returns MutableStateFlow(participants)
        every { company.id } returns MutableStateFlow("companyId")
        val viewModel = MainViewModel { Success(conferenceMock, mockk(), company, MutableStateFlow(mockk())) }
        advanceUntilIdle()
        val actual = viewModel.getOtherUserId()
        assertEquals(null, actual)
    }
}
