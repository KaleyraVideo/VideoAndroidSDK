@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.Input
import com.kaleyra.video.noise_filter.DeepFilterNetModuleLoader
import com.kaleyra.video.noise_filter.DeepFilterNetModule
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.callMock
import com.kaleyra.video_sdk.Mocks.conferenceMock
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.viewmodel.NoiseFilterViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoiseFilterViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: NoiseFilterViewModel
    private val deepFilterNetLoadingState: MutableStateFlow<DeepFilterNetModuleLoader.LoadingState> = MutableStateFlow(DeepFilterNetModuleLoader.LoadingState.Unloaded)

    @Before
    fun setup() {
        mockkObject(DeepFilterNetModule)
        every { DeepFilterNetModule.isAvailable() } returns true
        mockkObject(DeepFilterNetModuleLoader)
        every { DeepFilterNetModuleLoader.loadingState } returns deepFilterNetLoadingState
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns mockk {
                every { streams } returns MutableStateFlow(listOf(mockk {
                    every { id } returns CAMERA_STREAM_ID
                    val noiseFilterModeFlow = MutableStateFlow<Input.Audio.My.NoiseFilterMode>(Input.Audio.My.NoiseFilterMode.Standard)
                    every { audio } returns MutableStateFlow<Input.Audio.My?>(mockk(relaxed = true) {
                        every { noiseFilterMode } returns noiseFilterModeFlow
                        every { setNoiseFilterMode(any<Input.Audio.My.NoiseFilterMode>()) } answers {
                            noiseFilterModeFlow.value = firstArg()
                        }
                    })
                }))
            }
        })
        mockkObject(CallExtensions)
        with(CallExtensions) {
            coEvery { callMock.isCpuThrottling(any()) } returns MutableStateFlow(false)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun deepFilterNetLoaderLoadingStateUpdated_uiStateUpdated() = runTest {
        viewModel = NoiseFilterViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        Assert.assertEquals(DeepFilterNetModuleLoader.LoadingState.Unloaded, viewModel.uiState.first().deepFilerLoadingState)
        deepFilterNetLoadingState.value = DeepFilterNetModuleLoader.LoadingState.InProgress
        advanceUntilIdle()
        Assert.assertEquals(DeepFilterNetModuleLoader.LoadingState.InProgress, viewModel.uiState.first().deepFilerLoadingState)
        deepFilterNetLoadingState.value = DeepFilterNetModuleLoader.LoadingState.Loaded
        advanceUntilIdle()
        Assert.assertEquals(DeepFilterNetModuleLoader.LoadingState.Loaded, viewModel.uiState.first().deepFilerLoadingState)
        deepFilterNetLoadingState.value = DeepFilterNetModuleLoader.LoadingState.Unavailable
        advanceUntilIdle()
        Assert.assertEquals(DeepFilterNetModuleLoader.LoadingState.Unavailable, viewModel.uiState.first().deepFilerLoadingState)
    }

    @Test
    fun deepFilterNetModuleNotAvailable_uiStateUpdated() = runTest {
        every { DeepFilterNetModule.isAvailable() } returns false
        viewModel = NoiseFilterViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertTrue(NoiseFilterModeUi.DeepFilterAi !in viewModel.uiState.first().supportedNoiseFilterModesUi.value)
    }

    @Test
    fun deepFilterNetModuleAvailable_uiStateUpdated() = runTest {
        viewModel = NoiseFilterViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }
        advanceUntilIdle()
        Assert.assertTrue(NoiseFilterModeUi.DeepFilterAi in viewModel.uiState.first().supportedNoiseFilterModesUi.value)
    }
}