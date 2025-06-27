package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Input
import com.kaleyra.video.noise_filter.DeepFilterNetModule
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi

object NoiseFilterMapper {

    internal fun getSupportedNoiseFilterModes(): List<NoiseFilterModeUi> = listOfNotNull(
        NoiseFilterModeUi.Standard,
        NoiseFilterModeUi.DeepFilterAi.takeIf { DeepFilterNetModule.isAvailable() }
    )

    internal fun NoiseFilterModeUi.toNoiseFilerMode(): Input.Audio.My.NoiseFilterMode = when (this) {
        NoiseFilterModeUi.DeepFilterAi -> Input.Audio.My.NoiseFilterMode.DeepFilterAi
        NoiseFilterModeUi.Standard -> Input.Audio.My.NoiseFilterMode.Standard
    }

    internal fun Input.Audio.My.NoiseFilterMode.toNoiseFilerUiMode(): NoiseFilterModeUi = when (this) {
        Input.Audio.My.NoiseFilterMode.DeepFilterAi -> NoiseFilterModeUi.DeepFilterAi
        Input.Audio.My.NoiseFilterMode.Standard -> NoiseFilterModeUi.Standard
    }
}