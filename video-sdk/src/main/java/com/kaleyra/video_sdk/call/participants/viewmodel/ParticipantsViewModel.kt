package com.kaleyra.video_sdk.call.participants.viewmodel

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ParticipantsViewModel(configure: suspend () -> Configuration) :
    BaseViewModel<ParticipantsUiState>(configure) {

    override fun initialState() = ParticipantsUiState()

    init {
        viewModelScope.launch {
            val call = call.first()

            combine(
                call.participants,
                call.toInCallParticipants()
            ) { participants, inCallParticipants ->
                val invited = (listOfNotNull(participants.me) + participants.others - inCallParticipants.toSet()).map { it.userId }
                _uiState.update {
                    it.copy(invitedParticipants = invited.toImmutableList())
                }
            }.launchIn(this)
        }
    }

    // TODO remove code duplication in CallActionsViewModel
    fun toggleMic(activity: Activity?) {
        if (activity !is FragmentActivity) return
        viewModelScope.launch {
            val inputs = call.getValue()?.inputs
            val input = inputs?.request(activity, Inputs.Type.Microphone)?.getOrNull<Input.Audio>() ?: return@launch
            val isMicEnabled = input.enabled.value.local
            if (!isMicEnabled) input.tryEnable() else input.tryDisable()
        }
    }

    fun muteStreamAudio(streamId: String) {
        val participants = call.getValue()?.participants?.getValue()?.others
        val stream = participants?.firstNotNullOfOrNull { other ->
            other.streams.value.find { it.id == streamId }
        }
        val audio = stream?.audio?.getValue() ?: return
        if (!audio.enabled.value.local) audio.tryEnable()
        else audio.tryDisable()
    }

    companion object {

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ParticipantsViewModel(configure) as T
                }
            }
    }

}