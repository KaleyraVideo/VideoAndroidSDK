package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.Company
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.mapper.CallStateMapper
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrandLogoViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: BrandLogoViewModel

    private val callParticipants = mockk<CallParticipants>()

    private val call = mockk<CallUI>()

    private val conference = mockk<ConferenceUI>()

    private val company = mockk<CompanyUI>()

    private val mutableTheme = MutableSharedFlow<Company.Theme>(replay = 1)

    private val mutableCallState = MutableStateFlow<Call.State>(Call.State.Connected)

    @Before
    fun setup() {
        viewModel = BrandLogoViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), company, MutableStateFlow(mockk()))
        }
        mockkObject(CallStateMapper)
        mockkObject(ParticipantMapper)
        mockkObject(ContextRetainer)
        with(callParticipants) {
            every { me } returns mockk(relaxed = true)
            every { creator() } returns mockk(relaxed = true)
            every { callParticipants.others } returns listOf()
        }
        with(call) {
            every { state } returns mutableCallState
            every { participants } returns MutableStateFlow(callParticipants)
        }
        with(conference) {
            every { call } returns MutableSharedFlow<CallUI>(replay = 1).apply {
                tryEmit(this@BrandLogoViewModelTest.call)
            }
        }
        with(company) {
            every { theme } returns mutableTheme.apply {
                viewModel.viewModelScope.launch {
                    emit(CompanyUI.Theme())
                }
            }
        }
    }

    @Test
    fun testEmptyLogoReceived() = runTest {
        advanceUntilIdle()
        Assert.assertEquals(Uri.EMPTY, viewModel.uiState.first().logo.dark)
        Assert.assertEquals(Uri.EMPTY, viewModel.uiState.first().logo.light)
    }

    @Test
    fun testLogoReceived() = runTest {
        val logoNightUri = Uri.parse("https://www.example.com/image1.png")
        val logoDayUri = Uri.parse("https://www.example.com/image2.png")
        viewModel.viewModelScope.launch {
            mutableTheme.emit(CompanyUI.Theme(
                night = CompanyUI.Theme.Style(logo = logoNightUri),
                day = CompanyUI.Theme.Style(logo = logoDayUri))
            )
        }

        advanceUntilIdle()

        Assert.assertEquals(logoNightUri, viewModel.uiState.first().logo.dark)
        Assert.assertEquals(logoDayUri, viewModel.uiState.first().logo.light)
    }

    @Test
    fun testCallStateUiReceived() = runTest {
        mockkObject(CallStateMapper)
        every { call.toCallStateUi() } returns flowOf(CallStateUi.Disconnected.Ended.Timeout)

        advanceUntilIdle()

        Assert.assertEquals(CallStateUi.Disconnected.Ended.Timeout, viewModel.uiState.first().callStateUi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}