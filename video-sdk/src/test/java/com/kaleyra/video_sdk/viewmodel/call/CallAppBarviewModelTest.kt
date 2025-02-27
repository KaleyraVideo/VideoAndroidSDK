package com.kaleyra.video_sdk.viewmodel.call

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallAppBarViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallAppBarViewModel

    private val callMock = mockk<CallUI>(relaxed = true)

    private val conference = mockk<ConferenceUI>()

    private val companyMock = mockk<CompanyUI>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val companyThemeMock = MutableStateFlow(CompanyUI.Theme(
        day = CompanyUI.Theme.Style(logo = Uri.parse("https://www.example1.com")),
        night = CompanyUI.Theme.Style(logo = Uri.parse("https://www.example2.com")))
    )

    private val recordingMock: MutableStateFlow<Call.Recording> = MutableStateFlow(object : Call.Recording {
        override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Started)
        override val type: Call.Recording.Type = Call.Recording.Type.Automatic
    })

    private val elapsedMock: MutableStateFlow<Long> = MutableStateFlow(10L)

    private val inCallParticipantsMock: MutableStateFlow<List<CallParticipant>> = MutableStateFlow(listOf(participantMeMock, participantMock1, participantMock2))

    private val packageName = "packageName"

    private val appIconRef = 678

    private val mockkContext: Context = mockk<Context>(relaxed = true)

    private val packageManager: PackageManager = mockk<PackageManager>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockkContext
        every { mockkContext.packageName } returns packageName
        every { mockkContext.packageManager } returns packageManager
        every { packageManager.getApplicationInfo(packageName, 0) } returns ApplicationInfo().apply { icon = appIconRef }
        mockkObject(CallStateMapper)
        mockkObject(com.kaleyra.video_common_ui.mapper.ParticipantMapper)
        with(callMock) {
            every { toInCallParticipants() } returns inCallParticipantsMock
            every { recording } returns recordingMock
            every { time } returns object : Call.Time {
                override val elapsed: StateFlow<Long> = elapsedMock
                override val maxDuration: StateFlow<Long?> = MutableStateFlow(11L)
                override val remaining: StateFlow<Long?> = MutableStateFlow(9L)
            }
        }
        with(conference) {
            every { call } returns MutableSharedFlow<CallUI>(replay = 1).apply {
                tryEmit(callMock)
            }
        }
        every { companyMock.theme } returns companyThemeMock

        viewModel = CallAppBarViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), companyMock, MutableStateFlow(mockk(relaxed = true)))
        }
    }

    @Test
    fun testParticipantsCountReceived() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(3, viewModel.uiState.first().participantCount)
    }

    @Test
    fun testParticipantsCountUpdated() = runTest {
        advanceUntilIdle()
        inCallParticipantsMock.emit(listOf(mockk(relaxed = true), mockk(relaxed = true)))
        advanceUntilIdle()
        Assert.assertEquals(2, viewModel.uiState.first().participantCount)
    }

    @Test
    fun testElapsedTimeReceived() = runTest {
        advanceUntilIdle()
        Assert.assertEquals("00:10", viewModel.uiState.first().title)
    }

    @Test
    fun testElapsedTimeUpdated() = runTest {
        advanceUntilIdle()
        elapsedMock.emit(9L)
        advanceUntilIdle()
        Assert.assertEquals("00:09", viewModel.uiState.first().title)
    }


    @Test
    fun testRecordingReceived() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(true, viewModel.uiState.first().automaticRecording)
    }

    @Test
    fun testAutomaticRecordingStoppedUpdated() = runTest {
        recordingMock.emit(object : Call.Recording {
            override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Stopped)
            override val type: Call.Recording.Type = Call.Recording.Type.Automatic
        })
        viewModel = CallAppBarViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), companyMock, MutableStateFlow(mockk(relaxed = true)))
        }
        advanceUntilIdle()
        Assert.assertEquals(true, viewModel.uiState.first().automaticRecording)
        Assert.assertEquals(RecordingStateUi.Stopped, viewModel.uiState.first().recordingStateUi)
    }

    @Test
    fun testAutomaticRecordingStartedUpdated() = runTest {
        recordingMock.emit(object : Call.Recording {
            override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Started)
            override val type: Call.Recording.Type = Call.Recording.Type.Automatic
        })
        viewModel = CallAppBarViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), companyMock, MutableStateFlow(mockk(relaxed = true)))
        }
        advanceUntilIdle()
        Assert.assertEquals(true, viewModel.uiState.first().automaticRecording)
        Assert.assertEquals(RecordingStateUi.Started, viewModel.uiState.first().recordingStateUi)
    }

    @Test
    fun testManualRecordingStoppedUpdated() = runTest {
        recordingMock.emit(object : Call.Recording {
            override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Stopped)
            override val type: Call.Recording.Type = Call.Recording.Type.Manual
        })
        viewModel = CallAppBarViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), companyMock, MutableStateFlow(mockk(relaxed = true)))
        }
        advanceUntilIdle()
        Assert.assertEquals(false, viewModel.uiState.first().automaticRecording)
        Assert.assertEquals(RecordingStateUi.Stopped, viewModel.uiState.first().recordingStateUi)
    }

    @Test
    fun testManualRecordingStartedUpdated() = runTest {
        recordingMock.emit(object : Call.Recording {
            override val state: StateFlow<Call.Recording.State> = MutableStateFlow(Call.Recording.State.Started)
            override val type: Call.Recording.Type = Call.Recording.Type.Manual
        })
        viewModel = CallAppBarViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), companyMock, MutableStateFlow(mockk(relaxed = true)))
        }
        advanceUntilIdle()
        Assert.assertEquals(false, viewModel.uiState.first().automaticRecording)
        Assert.assertEquals(RecordingStateUi.Started, viewModel.uiState.first().recordingStateUi)
    }

    @Test
    fun testAppIconReceived() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(Uri.parse("android.resource://$packageName/$appIconRef"), viewModel.uiState.first().appIconUri)
    }
}
