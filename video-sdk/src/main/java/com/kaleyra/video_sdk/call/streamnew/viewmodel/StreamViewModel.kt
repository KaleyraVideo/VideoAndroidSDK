package com.kaleyra.video_sdk.call.streamnew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.toMyCameraVideoUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.streamnew.model.StreamPreview
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class StreamViewModel(configure: suspend () -> Configuration) : BaseViewModel<StreamUiState>(configure) {

    override fun initialState() = StreamUiState()

    private var maxPinnedStreams = DEFAULT_MAX_PINNED_STREAMS

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {
        viewModelScope.launch {
            val call = call.first()

            val callState = call.toCallStateUi()
            combine(
                call.toInCallParticipants(),
                call.toStreamsUi(),
                callState
            ) { p, s, c -> Triple(p, s, c) }
                .debounce { (participants, streams, callState: CallStateUi) ->
                    determineDebounceDelay(participants, streams, callState)
                }
                .mapLatest { (_, streams, callState) ->
                    val updatedStreams = when {
                        streams == uiState.value.streams.value -> uiState.value.streams
                        else -> streams.toImmutableList()
                    }

                    _uiState.update {
                        it.copy(
                            streams = updatedStreams,
                            fullscreenStream = findCurrentFullscreenStream(streams, callState),
                            pinnedStreams = updatePinnedStreams(streams).toImmutableList()
                        )
                    }
                    callState is CallStateUi.Disconnected.Ended
                }
                .takeWhile { !it }
                .onCompletion { _uiState.update { StreamUiState() } }
                .launchIn(this)

            val isPreCallState = callState
                .map { state -> state == CallStateUi.Ringing || state == CallStateUi.Dialing || state == CallStateUi.RingingRemotely }
                .distinctUntilChanged()
            combine(
                isPreCallState,
                call.toMyCameraVideoUi()
            ) { state, video -> state to video }
                .onEach { (isPreCallState, video) ->
                    if (!isPreCallState) return@onEach
                    val isGroupCall = call.isGroupCall(company.flatMapLatest { it.id }).first()
                    val otherUsername = call.toOtherDisplayNames().first()[0]
                    val otherAvatar = call.toOtherDisplayImages().first()[0]
                    _uiState.update {
                        it.copy(
                            preview = StreamPreview(
                                isGroupCall = isGroupCall,
                                video = video,
                                username = otherUsername,
                                avatar = ImmutableUri(otherAvatar)
                            )
                        )
                    }
                }
                .takeWhile { (isPreCallState, _) -> isPreCallState }
                .onCompletion {
                    // wait for at least another participant's stream to be added before setting the preview to null
                    uiState.first { it.streams.value.size > 1 }
                    _uiState.update { it.copy(preview = null) }
                }
                .launchIn(this)
        }
    }

    fun fullscreen(streamId: String?) {
        val streams = uiState.value.streams.value
        val stream = streams.find { it.id == streamId }
        if (streamId != null && stream == null) return
        _uiState.update { it.copy(fullscreenStream = stream) }
    }

    fun setMaxPinnedStreams(count: Int) {
        maxPinnedStreams = count
    }

    fun pin(streamId: String): Boolean {
        val streams = uiState.value.streams.value
        val stream = streams.find { it.id == streamId }
        if (stream == null || uiState.value.pinnedStreams.count() >= maxPinnedStreams) return false
        _uiState.update {
            val new = (it.pinnedStreams.value + stream).toImmutableList()
            it.copy(pinnedStreams = new)
        }
        return true
    }

    fun unpin(streamId: String) {
        val streams = uiState.value.streams.value
        val stream = streams.find { it.id == streamId }
        if (stream == null) return
        _uiState.update {
            val new = (it.pinnedStreams.value - stream).toImmutableList()
            it.copy(pinnedStreams = new)
        }
    }

    fun unpinAll() {
        _uiState.update { it.copy(pinnedStreams = ImmutableList()) }
    }

    // TODO remove code duplication in CallActionsViewModel
    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value.local }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me
            val streams = me?.streams?.value
            val stream = streams?.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
            if (stream != null) me.removeStream(stream)
            val hasStopped = when(input) {
                is Input.Video.Screen -> true.also {
                    input.dispose()
                }
                is Input.Video.Application -> input.tryDisable()
                else -> false
            }
            hasStopped && stream != null
        }
    }

    private fun updatePinnedStreams(streams: List<StreamUi>): List<StreamUi> {
        val localScreenShare = streams.find { it.video?.isScreenShare == true && it.isMine }
        // Clear the removed pinned streams
        val updatedPinnedStreams = uiState.value.pinnedStreams.value.mapNotNull { stream ->
            streams.find { it.id == stream.id }
        }.toMutableList()
        // Pin the local screen share as first stream
        localScreenShare?.let {
            updatedPinnedStreams.add(0, it)
            if (updatedPinnedStreams.size > maxPinnedStreams) {
                updatedPinnedStreams.removeAt(1)
            }
        }
        return updatedPinnedStreams
    }

    private fun findCurrentFullscreenStream(streams: List<StreamUi>, callState: CallStateUi): StreamUi? {
        val currentFullscreenStream = uiState.value.fullscreenStream
        // Reset fullscreen stream on reconnection
        return if (callState == CallStateUi.Reconnecting) null
        else streams.find { it.id == currentFullscreenStream?.id }
    }

    private fun determineDebounceDelay(participants: List<CallParticipant>, streams: List<StreamUi>, callState: CallStateUi): Long {
        // Implement a debounce mechanism to prevent streams updates during audio-to-video call upgrades (republishing),
        // triggering the update only when the local participant remains alone in the call.
        return if (participants.size > 1 || streams.size != 1 || callState != CallStateUi.Connected) DEFAULT_DEBOUNCE_MILLIS
        else SINGLE_STREAM_DEBOUNCE_MILLIS
    }

    companion object {

        const val SINGLE_STREAM_DEBOUNCE_MILLIS = 5000L

        const val DEFAULT_DEBOUNCE_MILLIS = 100L

        const val DEFAULT_MAX_PINNED_STREAMS = 2

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StreamViewModel(configure) as T
                }
            }
    }
}