package com.kaleyra.video_sdk.call.settings.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video.noise_filter.DeepFilterNetLoader
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
data class NoiseFilterUiState(
    val deepFilerLoadingState: DeepFilterNetLoader.LoadingState = DeepFilterNetLoader.LoadingState.Unloaded,
    val supportedNoiseFilterModesUi: ImmutableList<NoiseFilterModeUi> = ImmutableList(listOf()),
    val currentNoiseFilterModeUi: NoiseFilterModeUi = NoiseFilterModeUi.Standard,
) : UiState
