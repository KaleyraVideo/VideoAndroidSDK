package com.kaleyra.video_common_ui.performance

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Input
import com.kaleyra.video.utils.logger.PHONE_BOX
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.isCpuThrottling
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class CallPerformanceManager(val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO), val logger: PriorityLogger? = KaleyraVideo.logger) {

    private var disablePerformanceFeaturesJob: Job? = null

    private var isThrottlingCpu = MutableStateFlow(false)

    fun bind(call: Call) {
        coroutineScope.launch {
            disablePerformanceFeaturesJob = call.isCpuThrottling(this)
                .onEach { isThrottlingCpu ->
                    logger?.info(logTarget = PHONE_BOX, message = "Cpu is throttling: $isThrottlingCpu")
                    this@CallPerformanceManager.isThrottlingCpu.value = isThrottlingCpu
                    if (isThrottlingCpu) disablePerformanceFeatures(call)
                }
                .launchIn(this)
        }
    }

    private fun disablePerformanceFeatures(call: Call) {
        call.type.combine(isThrottlingCpu) { a, b -> a to b }
            .onEach { (callType, isThrottlingCpu) ->
                if (!isThrottlingCpu) return@onEach

                val cameraStream = call.participants.first { it.me != null }.me!!.streams.first { it.any { it.id == CameraStreamConstants.CAMERA_STREAM_ID } }.first {
                    it.id == CameraStreamConstants.CAMERA_STREAM_ID
                }

                if (callType.hasAudio()) {
                    coroutineScope.launch {
                        with(cameraStream.audio.first { it != null }!!) {
                            val currentEffect = noiseFilterMode.value
                            if (currentEffect is Input.Audio.My.NoiseFilterMode.DeepFilterAi) {
                                setNoiseFilterMode(Input.Audio.My.NoiseFilterMode.Standard)
                                logger?.info(logTarget = PHONE_BOX, message = "Disabled ai audio noise filtering due to device overheat.")
                            }
                        }
                    }
                }
                if (callType.hasVideo()) {
                    coroutineScope.launch {
                        with(cameraStream.video.first { it != null }!!) {
                            val currentEffect = currentEffect.value
                            if (currentEffect !is Effect.Video.None) {
                                tryApplyEffect(Effect.Video.None)
                                tryDisable()
                                logger?.info(logTarget = PHONE_BOX, message = "Disabled video effects due to device overheat.")
                            }
                        }
                    }
                }
            }.launchIn(coroutineScope)
    }

    fun stop() {
        disablePerformanceFeaturesJob?.cancel()
    }
}