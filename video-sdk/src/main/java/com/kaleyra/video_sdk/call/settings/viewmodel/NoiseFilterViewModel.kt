package com.kaleyra.video_sdk.call.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.noise_filter.DeepFilterNetLoader
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.isCpuThrottling
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.getSupportedNoiseFilterModes
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.toNoiseFilerMode
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class NoiseFilterViewModel(configure: suspend () -> Configuration) : BaseViewModel<NoiseFilterUiState>(configure) {

    override fun initialState() = NoiseFilterUiState()

    init {
        DeepFilterNetLoader.loadingState.onEach { loadingState ->
            _uiState.update { it.copy(deepFilerLoadingState = loadingState) }
        }.launchIn(viewModelScope)

        _uiState.update { it.copy(supportedNoiseFilterModesUi = ImmutableList(getSupportedNoiseFilterModes())) }

        viewModelScope.launch {
            call.first()

            call.getValue()!!
                .isCpuThrottling(this)
                .onEach { isDeviceOverHeating ->
                    _uiState.update { it.copy(isDeviceOverHeating = isDeviceOverHeating) }
                }
                .launchIn(this)
        }
    }

    fun setNoiseSuppressionMode(mode: NoiseFilterModeUi) {
        if (mode !in uiState.value.supportedNoiseFilterModesUi.value) return
        _uiState.update { it.copy(currentNoiseFilterModeUi = mode) }
        val call = call.getValue()
        val me = call?.participants?.value?.me
        val stream = me?.streams?.value?.firstOrNull { it.id == CAMERA_STREAM_ID }
        val audio = stream?.audio?.value ?: return
        audio.setNoiseFilterMode(mode.toNoiseFilerMode())
    }

    companion object {

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoiseFilterViewModel(configure) as T
                }
            }
    }
}
