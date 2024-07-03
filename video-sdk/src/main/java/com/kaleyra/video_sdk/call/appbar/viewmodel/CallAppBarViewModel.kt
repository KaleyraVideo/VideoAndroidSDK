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
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

            ongoingCall.toInCallParticipants()
                .onEach { inCallParticipants ->
                    println(inCallParticipants)
                    _uiState.update { uiState ->
                        uiState.copy(participantCount = inCallParticipants.size)
                    }
                }.launchIn(this)

            ongoingCall.time.elapsed
                .map { timeStamp -> TimerParser.parseTimestamp(timeStamp) }
                .onEach { formattedElapsedSeconds ->
                    _uiState.update { uiState ->
                        uiState.copy(title = formattedElapsedSeconds)
                    }
                }.launchIn(this)

            ongoingCall.recording.flatMapLatest { it.state }.onEach { recordingState ->
                _uiState.update { uiState ->
                    uiState.copy(recording = recordingState == Call.Recording.State.Started)
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
                    return CallInfoViewModel(configure) as T
                }
            }
    }
}
