@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.appbar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_common_ui.utils.TimerParser
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.model.Logo
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.mapToRecordingStateUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallAppBarViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallAppBarUiState>(configure) {
    override fun initialState() = CallAppBarUiState()

    init {
        viewModelScope.launch {
            val ongoingCall = call.first()

            if (ongoingCall.recording.value.type is Call.Recording.Type.OnConnect)
                _uiState.update { uiState ->
                    uiState.copy(automaticRecording = true)
                }

            ongoingCall.toCallStateUi()
                .onEach { callStateUi ->
                    _uiState.update { uiState ->
                        uiState.copy(callStateUi = callStateUi)
                    }
                }.launchIn(this)

            ongoingCall.toInCallParticipants()
                .onEach { inCallParticipants ->
                    _uiState.update { uiState ->
                        uiState.copy(participantCount = inCallParticipants.size)
                    }
                }.launchIn(this)

            ongoingCall.time.elapsed
                .filter { it > 0 }
                .map { timeStamp -> TimerParser.parseTimestamp(timeStamp) }
                .onEach { formattedElapsedSeconds ->
                    _uiState.update { uiState ->
                        uiState.copy(title = formattedElapsedSeconds)
                    }
                }.launchIn(this)

            ongoingCall.recording.flatMapLatest { it.state }
                .dropWhile { it is Call.Recording.State.Stopped }
                .onEach { recordingState ->
                    _uiState.update { uiState ->
                        uiState.copy(recordingStateUi = recordingState.mapToRecordingStateUi())
                    }
                }.launchIn(this)

            company.first().combinedTheme.onEach { companyTheme ->
                _uiState.update { uiState ->
                    uiState.copy(logo = Logo(companyTheme.day.logo, companyTheme.night.logo))
                }
            }.launchIn(this)
        }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallAppBarViewModel(configure) as T
                }
            }
    }
}
