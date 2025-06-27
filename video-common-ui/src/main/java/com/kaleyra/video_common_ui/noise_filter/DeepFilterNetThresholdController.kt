package com.kaleyra.video_common_ui.noise_filter

import com.kaleyra.video.noise_filter.DeepFilterNetModule
import com.kaleyra.video.noise_filter.DeepFilterNetModuleLoader

import com.kaleyra.video_core_av.capturer.audio.audio_processor.NoiseProcessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DeepFilterNetThresholdController(val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private var deepFilterNetThresholdControlJob: Job? = null

    fun bind() {
        if (!DeepFilterNetModule.isAvailable()) return
        if (deepFilterNetThresholdControlJob != null) return

        deepFilterNetThresholdControlJob = coroutineScope.launch {
            DeepFilterNetModuleLoader.loadingState.first { it is DeepFilterNetModuleLoader.LoadingState.Loaded}
            NoiseProcessors.deepFilterNoiseProcessor.setNoiseThreshold(DEFAULT_ATTENUATION_LIMIT)
        }
    }

    fun stop() {
        deepFilterNetThresholdControlJob?.cancel()
        deepFilterNetThresholdControlJob = null
    }

    companion object {
        const val DEFAULT_ATTENUATION_LIMIT = 20f
    }
}