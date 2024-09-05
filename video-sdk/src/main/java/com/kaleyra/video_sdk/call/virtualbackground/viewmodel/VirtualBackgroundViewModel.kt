/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.virtualbackground.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Effect
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class VirtualBackgroundViewModel(configure: suspend () -> Configuration) : BaseViewModel<VirtualBackgroundUiState>(configure) {

    override fun initialState() = VirtualBackgroundUiState()

    init {
        viewModelScope.launch {
            val call = call.first()

            call
                .toVirtualBackgroundsUi()
                .onEach { backgrounds -> _uiState.update { it.copy(backgroundList = ImmutableList(backgrounds)) } }
                .launchIn(viewModelScope)

            call
                .toMyCameraStream()
                .flatMapLatest { it.video }
                .filterNotNull()
                .onEach { video ->
                    val background = uiState.value.currentBackground
                    video.tryApplyEffect(getEffect(call, background))
                }.launchIn(viewModelScope)
        }
    }

    fun setEffect(background: VirtualBackgroundUi) {
        _uiState.update { it.copy(currentBackground = background) }
        _isVirtualBackgroundEnabled.update { background != VirtualBackgroundUi.None }

        val call = call.getValue()
        val me = call?.participants?.value?.me
        val stream = me?.streams?.value?.firstOrNull { it.id == CAMERA_STREAM_ID }
        val video = stream?.video?.value ?: return
        video.tryApplyEffect(getEffect(call, background))
    }

    private fun getEffect(call: Call, background: VirtualBackgroundUi): Effect.Video {
        val image = call.effects.preselected.value.takeIf { it is Effect.Video.Background.Image } as? Effect.Video
        val blur = call.effects.available.value.firstOrNull { it is Effect.Video.Background.Blur } as? Effect.Video
        return when {
            background is VirtualBackgroundUi.Blur && blur != null -> blur
            background is VirtualBackgroundUi.Image && image != null -> image
            else -> Effect.Video.None
        }
    }

    companion object {

        private val _isVirtualBackgroundEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isVirtualBackgroundEnabled: StateFlow<Boolean> = _isVirtualBackgroundEnabled

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VirtualBackgroundViewModel(configure) as T
                }
            }
    }
}
