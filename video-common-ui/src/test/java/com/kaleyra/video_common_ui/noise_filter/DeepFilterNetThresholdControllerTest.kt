@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.noise_filter

import com.kaleyra.video.noise_filter.DeepFilterNetModule
import com.kaleyra.video.noise_filter.DeepFilterNetModuleLoader
import com.kaleyra.video_core_av.capturer.audio.audio_processor.NoiseProcessor
import com.kaleyra.video_core_av.capturer.audio.audio_processor.NoiseProcessors
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepFilterNetThresholdControllerTest {

    private val mockDeepFilterNetProcessor = mockk<NoiseProcessor>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(DeepFilterNetModule)
        mockkObject(NoiseProcessors)
        mockkObject(DeepFilterNetModuleLoader)
        every { NoiseProcessors.deepFilterNoiseProcessor } returns mockDeepFilterNetProcessor
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBindWithModuleNotAvailable() = runTest {
        every { DeepFilterNetModule.isAvailable() } returns false
        every { DeepFilterNetModuleLoader.loadingState } returns MutableStateFlow(DeepFilterNetModuleLoader.LoadingState.Unloaded)

        DeepFilterNetThresholdController(backgroundScope).bind()
        runCurrent()

        verify(exactly = 0) { mockDeepFilterNetProcessor.setNoiseThreshold(any()) }
    }

    @Test
    fun testBind() = runTest {
        every { DeepFilterNetModule.isAvailable() } returns true
        every { DeepFilterNetModuleLoader.loadingState } returns MutableStateFlow(DeepFilterNetModuleLoader.LoadingState.Loaded)

        DeepFilterNetThresholdController(backgroundScope).bind()
        runCurrent()

        verify(exactly = 1) { mockDeepFilterNetProcessor.setNoiseThreshold(DeepFilterNetThresholdController.DEFAULT_ATTENUATION_LIMIT) }
    }

    @Test
    fun testMultipleBind_setThresholdCalledOnce() = runTest {
        every { DeepFilterNetModule.isAvailable() } returns true
        every { DeepFilterNetModuleLoader.loadingState } returns MutableStateFlow(DeepFilterNetModuleLoader.LoadingState.Loaded)

        val deepFilterNetThresholdController = DeepFilterNetThresholdController(backgroundScope)
        deepFilterNetThresholdController.bind()
        deepFilterNetThresholdController.bind()
        runCurrent()

        verify(exactly = 1) { mockDeepFilterNetProcessor.setNoiseThreshold(DeepFilterNetThresholdController.DEFAULT_ATTENUATION_LIMIT) }
    }

    @Test
    fun testStop() = runTest {
        every { DeepFilterNetModule.isAvailable() } returns true
        val deepFilterNetLoadingState = MutableStateFlow<DeepFilterNetModuleLoader.LoadingState>(DeepFilterNetModuleLoader.LoadingState.Unloaded)
        every { DeepFilterNetModuleLoader.loadingState } returns deepFilterNetLoadingState
        val deepFilterNetThresholdController = DeepFilterNetThresholdController(backgroundScope)
        deepFilterNetThresholdController.bind()
        deepFilterNetThresholdController.stop()

        deepFilterNetLoadingState.value = DeepFilterNetModuleLoader.LoadingState.Loaded
        runCurrent()

        verify(exactly = 0) { mockDeepFilterNetProcessor.setNoiseThreshold(DeepFilterNetThresholdController.DEFAULT_ATTENUATION_LIMIT) }
    }
}
