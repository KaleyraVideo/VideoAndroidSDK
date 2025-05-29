package com.kaleyra.video_common_ui.performance

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Input
import com.kaleyra.video.utils.logger.PHONE_BOX
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.isCpuThrottling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


internal class CallPerformanceManager(val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private var logger = KaleyraVideo.logger

    private var disablePerformanceFeaturesJob: Job? = null

    fun bind(call: Call) {
        coroutineScope.launch {
            disablePerformanceFeaturesJob = call.isCpuThrottling(this)
                .onEach { isThrottlingCpu ->
                    logger?.info(logTarget = PHONE_BOX, message = "Cpu is throttling: $isThrottlingCpu")
                    if (isThrottlingCpu) disablePerformanceFeatures(call)
                }
                .launchIn(this)
        }
    }

    private fun disablePerformanceFeatures(call: Call) {
        call.participants.value.me?.streams?.value?.firstOrNull { it.id == CameraStreamConstants.CAMERA_STREAM_ID }?.let {
            with(it.video.value) {
                this ?: return@with
                val currentEffect = currentEffect.value
                if (currentEffect !is Effect.Video.None) {
                    tryApplyEffect(Effect.Video.None)
                    tryDisable()
                    logger?.info(logTarget = PHONE_BOX, message = "Disabled video effects due to device overheat.")
                }
            }

            with(it.audio.value) {
                this ?: return@with
                val currentEffect = noiseFilterMode.value
                if (currentEffect is Input.Audio.My.NoiseFilterMode.DeepFilterAi) {
                    setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard)
                    logger?.info(logTarget = PHONE_BOX, message = "Disabled ai audio noise filtering due to device overheat.")
                }
            }
        }
    }

    fun stop() {
        disablePerformanceFeaturesJob?.cancel()
        logger = null
    }
}