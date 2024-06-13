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

package com.kaleyra.video_sdk.call.ringing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingTypeUi
import com.kaleyra.video_sdk.call.precall.viewmodel.PreCallViewModel
import com.kaleyra.video_sdk.call.ringing.model.RingingUiState
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RingingViewModel(configure: suspend () -> Configuration): PreCallViewModel<RingingUiState>(configure) {

    override fun initialState() = RingingUiState()

    init {
        viewModelScope.launch {
            val call = call.first()

            call
                .toRecordingTypeUi()
                .onEach { rec -> _uiState.update { it.copy(recording = rec) } }
                .launchIn(viewModelScope)

            call
                .amIWaitingOthers()
                .debounce(AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
                .onEach { amIWaitingOthers -> _uiState.update { it.copy(amIWaitingOthers = amIWaitingOthers) } }
                .takeWhile { !it }
                .launchIn(viewModelScope)

            call
                .state
                .map { state ->
                    val isConnecting = state is Call.State.Connecting
                    _uiState.update { it.clone(isConnecting = isConnecting) }
                    isConnecting
                }
                .takeWhile { !it }
                .launchIn(viewModelScope)
        }
    }

    fun accept() {
        if (ConnectionServiceUtils.isConnectionServiceEnabled) viewModelScope.launch { KaleyraCallConnectionService.answer() }
        else call.getValue()?.connect()
        _uiState.update { it.copy(isConnecting = true) }
    }

    fun decline() {
        if (ConnectionServiceUtils.isConnectionServiceEnabled) viewModelScope.launch { KaleyraCallConnectionService.reject() }
        else call.getValue()?.end()
        _uiState.update { it.copy(isConnecting = true) }
    }

    companion object {

        const val AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS = 2000L
        
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RingingViewModel(configure) as T
                }
            }
    }

}