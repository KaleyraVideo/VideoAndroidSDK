package com.kaleyra.video_sdk.call.participants.viewmodel

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutControllerImpl
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ParticipantsViewModel(
    configure: suspend () -> Configuration,
    private val layoutController: StreamLayoutController
) : BaseViewModel<ParticipantsUiState>(configure) {

    override fun initialState() = ParticipantsUiState()

    init {
        viewModelScope.launch {
            val call = call.first()

            val inCallParticipantsFlow = call.toInCallParticipants()
            combine(
                call.participants,
                inCallParticipantsFlow
            ) { participants, inCallParticipants ->
                val invitedUsers = (listOfNotNull(participants.me) + participants.others - inCallParticipants.toSet())
                val invitedNames = invitedUsers.map { user -> user.combinedDisplayName.firstOrNull() ?: user.userId }
                _uiState.update {
                    it.copy(invitedParticipants = invitedNames.toImmutableList())
                }
            }.launchIn(this)

            inCallParticipantsFlow
                .onEach { inCallParticipants ->
                    _uiState.update { uiState -> uiState.copy(joinedParticipantCount = inCallParticipants.size) }
                }.launchIn(this)

            call
                .toStreamsUi()
                .onEach { streams ->
                    _uiState.update { uiState -> uiState.copy(streams = streams.toImmutableList()) }
                }
                .launchIn(this)

            layoutController.isPinnedStreamLimitReached
                .onEach { isPinnedStreamLimitReached ->
                    _uiState.update { state -> state.copy(hasReachedMaxPinnedStreams = isPinnedStreamLimitReached)}
                }
                .launchIn(this)

            layoutController.isInAutoMode
                .onEach { isInAutoMode ->
                    val streamsLayout = if (isInAutoMode) StreamsLayout.Auto else StreamsLayout.Mosaic
                    _uiState.update { it.copy(streamsLayout = streamsLayout) }
                }
                .launchIn(this)

            layoutController.streamItems
                .onEach { items ->
                    val ids = items.filter { it.isPinned() }.map { it.id }
                    _uiState.update { it.copy(pinnedStreamIds = ids.toImmutableList()) }
                }
                .launchIn(this)
        }
    }

    fun switchToManualLayout() {
        layoutController.switchToManualMode()
    }

    fun switchToAutoLayout() {
        layoutController.switchToAutoMode()
    }

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean {
        return layoutController.pinStream(streamId, prepend, force)
    }

    fun unpinStream(streamId: String) {
        layoutController.unpinStream(streamId)
    }

    // TODO remove code duplication in CallActionsViewModel
    fun toggleMic(activity: Activity?) {
        if (activity !is FragmentActivity) return
        viewModelScope.launch {
            val inputs = call.getValue()?.inputs
            val input = inputs?.request(activity, Inputs.Type.Microphone)?.getOrNull<Input.Audio>() ?: return@launch
            val isMicEnabled = input.enabled.value.isAtLeastLocallyEnabled()
            if (!isMicEnabled) input.tryEnable() else input.tryDisable()
        }
    }

    fun muteStreamAudio(streamId: String) {
        val participants = call.getValue()?.participants?.getValue()?.others
        val stream = participants?.firstNotNullOfOrNull { other ->
            other.streams.value.find { it.id == streamId }
        }
        val audio = stream?.audio?.getValue() ?: return
        if (!audio.enabled.value.isAtLeastLocallyEnabled()) audio.tryEnable()
        else audio.tryDisable()
    }

    companion object {

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val layoutController = StreamLayoutControllerImpl.getInstance(
                        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
                    )
                    return ParticipantsViewModel(configure, layoutController) as T
                }
            }
    }

}