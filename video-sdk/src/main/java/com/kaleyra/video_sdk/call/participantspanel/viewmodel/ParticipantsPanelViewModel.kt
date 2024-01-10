package com.kaleyra.video_sdk.call.participantspanel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.Contact
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.video_sdk.call.participantspanel.model.ParticipantsPanelUiState
import com.kaleyra.video_sdk.call.participantspanel.model.StreamArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update

@ExperimentalCoroutinesApi
internal class ParticipantsPanelViewModel(configure: suspend () -> Configuration) : BaseViewModel<ParticipantsPanelUiState>(configure) {
    override fun initialState() = ParticipantsPanelUiState(adminUserId = "", isLoggedUserAdmin = true)

    private val inCallParticipants = call.toInCallParticipants()

    private val invitedDisplayNames = hashMapOf<String, String>()
    private var invitedDisplayNameJob: Job? = null

    private val invitedCallParticipants: Flow<List<Contact>> = inCallParticipants.map { inCallParticipants ->
        call.replayCache.firstOrNull()?.participants?.value?.list?.minus(inCallParticipants.toSet()) ?: listOf()
    }

    init {
        company
            .flatMapLatest { it.combinedTheme }
            .toWatermarkInfo(company.flatMapLatest { it.name })
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call.toStreamsUi().onEach { inCallStreamUi ->
            _uiState.update { it.copy(inCallStreamUi = ImmutableList(inCallStreamUi)) }
        }.launchIn(viewModelScope)

        invitedCallParticipants.onEach {
            invitedDisplayNameJob?.cancel()
            it.map { contact ->
                invitedDisplayNameJob = contact.combinedDisplayName.filterNotNull().onEach { displayName ->
                    if (invitedDisplayNames[contact.userId] != displayName) {
                        invitedDisplayNames[contact.userId] = displayName
                        val invited: List<String> = invitedDisplayNames.values.toList()
                        _uiState.update { it.copy(invitedParticipants = ImmutableList(invited)) }
                    }
                }.launchIn(viewModelScope)
            }
        }.launchIn(viewModelScope)
    }

    fun updateStreamArrangement(streamArrangement: StreamArrangement) {
        _uiState.update { currentUiState ->
            when (streamArrangement) {
                StreamArrangement.Grid -> currentUiState.copy(
                    streamArrangement = StreamArrangement.Grid,
                    inCallStreamUi = ImmutableList(currentUiState.inCallStreamUi.value.map { it.copy(pinned = false) })
                )

                StreamArrangement.Pin -> currentUiState.copy(
                    streamArrangement = StreamArrangement.Pin,
                )
            }
        }
    }

    fun pin(streamUi: StreamUi) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                streamArrangement = StreamArrangement.Pin,
                inCallStreamUi = ImmutableList(currentUiState.inCallStreamUi.value.map {
                    if (it != streamUi) it
                    else it.copy(pinned = true)
                })
            )
        }
    }

    fun unpin(streamUi: StreamUi) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                streamArrangement = if (currentUiState.inCallStreamUi.value.all { !it.pinned }) StreamArrangement.Grid else StreamArrangement.Pin,
                inCallStreamUi = ImmutableList(currentUiState.inCallStreamUi.value.map {
                    if (it != streamUi) it
                    else it.copy(pinned = true)
                })
            )
        }
    }

    fun mute(streamUi: StreamUi) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                streamArrangement = StreamArrangement.Pin,
                inCallStreamUi = ImmutableList(currentUiState.inCallStreamUi.value.map {
                    if (it != streamUi) it
                    else it.copy(audio = it.audio?.copy(isEnabled = false))
                })
            )
        }
    }

    fun unmute(streamUi: StreamUi) {
        _uiState.update { currentUiState ->
            currentUiState.copy(
                streamArrangement = StreamArrangement.Pin,
                inCallStreamUi = ImmutableList(currentUiState.inCallStreamUi.value.map {
                    if (it != streamUi) it
                    else it.copy(audio = it.audio?.copy(isEnabled = true))
                })
            )
        }
    }

    companion object {

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ParticipantsPanelViewModel(configure) as T
                }
            }
    }
}