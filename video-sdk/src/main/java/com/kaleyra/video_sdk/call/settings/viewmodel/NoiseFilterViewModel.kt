package com.kaleyra.video_sdk.call.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.noise_filter.DeepFilterNetModuleLoader
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.isCpuThrottling
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.getSupportedNoiseFilterModes
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.toNoiseFilerMode
import com.kaleyra.video_sdk.call.mapper.NoiseFilterMapper.toNoiseFilerUiMode
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class NoiseFilterViewModel(configure: suspend () -> Configuration) : BaseViewModel<NoiseFilterUiState>(configure) {

    override fun initialState() = NoiseFilterUiState()

    init {
        DeepFilterNetModuleLoader.loadingState.onEach { loadingState ->
            _uiState.update { it.copy(deepFilerLoadingState = loadingState) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val call = call.first()

            call.isCpuThrottling(this)
                .onEach { isDeviceOverHeating ->
                    _uiState.update { it.copy(isDeviceOverHeating = isDeviceOverHeating) }
                }
                .launchIn(this)

            val me = call.participants.value.me
            val stream = me?.streams?.firstOrNull { it.any { it.id == CAMERA_STREAM_ID } }?.first { it.id == CAMERA_STREAM_ID }
            stream?.audio?.flatMapLatest { audio ->
                audio?.noiseFilterMode?.map { noiseFilterMode ->
                    audio to noiseFilterMode
                } ?: flowOf()
            }?.onEach { (_, noiseFilterMode) ->
                _uiState.update { it.copy(currentNoiseFilterModeUi = noiseFilterMode.toNoiseFilerUiMode(), supportedNoiseFilterModesUi = ImmutableList(getSupportedNoiseFilterModes())) }
            }?.launchIn(viewModelScope)
        }
    }

    fun setNoiseSuppressionMode(mode: NoiseFilterModeUi) {
        viewModelScope.launch {
            call.first()
            if (mode !in uiState.value.supportedNoiseFilterModesUi.value) return@launch
            val call = call.getValue()
            val me = call?.participants?.value?.me
            val stream = me?.streams?.value?.firstOrNull { it.id == CAMERA_STREAM_ID }
            val audio = stream?.audio?.value ?: return@launch
            audio.setNoiseFilterMode(mode.toNoiseFilerMode())
        }
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
