package com.kaleyra.video_sdk.call.stream.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Conference
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_sdk.call.mapper.AudioMapper.toMyCameraStreamAudioUi
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.toMyCameraVideoUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutControllerImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutSettings
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.utils.isLocalScreenShare
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class StreamViewModel(
    configure: suspend () -> Configuration,
    private val layoutController: StreamLayoutController
) : BaseViewModel<StreamUiState>(configure) {

    override fun initialState() = StreamUiState()

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    private val userMessageChannel = Channel<UserMessage>(Channel.BUFFERED)
    val userMessage: Flow<UserMessage> = userMessageChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            layoutController.switchToAutoMode()

            val conference = conference.first()
            val call = call.first()
            val companyIdFlow = company.flatMapLatest { it.id }

            val defaultCameraIsBack = conference.settings.camera == Conference.Settings.Camera.Back
            layoutController.applySettings(StreamLayoutSettings(defaultCameraIsBack = defaultCameraIsBack))
            call
                .toInCallParticipants()
                .onEach { inCallParticipants ->
                    val currentSettings = layoutController.layoutSettings.value
                    layoutController.applySettings(currentSettings.copy(isGroupCall = inCallParticipants.size > 2))
                }
                .launchIn(this)

            call
                .toStreamsUi()
                .onEach { streams ->
                    val controllerStreams = streams.filterNot { it.isLocalScreenShare() }
                    layoutController.applyStreams(controllerStreams)
                    _uiState.update { state ->
                        state.copy(isScreenShareActive = controllerStreams.size != streams.size)
                    }
                }
                .launchIn(this)

            val callStateFlow = call.toCallStateUi()
            combine(
                call.toInCallParticipants(),
                layoutController.streamItems,
                callStateFlow
            ) { p, s, c -> Triple(p, s, c) }
                .debounce { (participants, streamItems, callState: CallStateUi) ->
                    determineDebounceDelay(participants, streamItems, callState)
                }
                .mapLatest { (_, streamItems, callState) ->
                    _uiState.update { it.copy(streamItems = streamItems.toImmutableList()) }
                    callState is CallStateUi.Disconnected.Ended
                }
                .takeWhile { !it }
                .onCompletion { _uiState.update { StreamUiState() } }
                .launchIn(this)

            val shouldShowStreamPreviewFlow = callStateFlow
                .map { state -> state == CallStateUi.Ringing || state == CallStateUi.Dialing || state == CallStateUi.RingingRemotely || state == CallStateUi.Reconnecting }
                .distinctUntilChanged()
            combine(
                shouldShowStreamPreviewFlow,
                call.toMyCameraVideoUi(),
                call.toMyCameraStreamAudioUi(),
                call.preferredType
            ) { shouldShowStreamPreview, video, audio, preferredType ->
                if (shouldShowStreamPreview) {
                    val isGroupCall = call.isGroupCall(companyIdFlow).first()
                    val otherUsername = call.toOtherDisplayNames().first().firstOrNull()
                    val otherAvatar = call.toOtherDisplayImages().first().firstOrNull()
                    _uiState.update {
                        it.copy(
                            preview = StreamPreview(
                                isGroupCall = isGroupCall,
                                video = video,
                                audio = audio,
                                username = otherUsername,
                                avatar = otherAvatar?.let { avatar -> ImmutableUri(avatar) },
                                isStartingWithVideo = preferredType.hasVideo() && preferredType.isVideoEnabled()
                            )
                        )
                    }
                } else {
                    // wait for at least another participant's stream to be added before setting the preview to null
                    uiState.first { it.streamItems.value.size > 1 }
                    _uiState.update { it.copy(preview = null) }
                }
            }.launchIn(this)

            layoutController.isPinnedStreamLimitReached
                .onEach { isPinnedStreamLimitReached ->
                    _uiState.update { state -> state.copy(hasReachedMaxPinnedStreams = isPinnedStreamLimitReached)}
                }
                .launchIn(this)
        }
    }

    fun setStreamLayoutConstraints(
        mosaicStreamThreshold: Int,
        featuredStreamThreshold: Int,
        thumbnailStreamThreshold: Int
    ) {
        layoutController.applyConstraints(
            StreamLayoutConstraints(mosaicStreamThreshold, featuredStreamThreshold, thumbnailStreamThreshold)
        )
    }

    fun setFullscreenStream(streamId: String) {
        layoutController.setFullscreenStream(streamId)
        viewModelScope.launch { userMessageChannel.send(FullScreenMessage.Enabled) }
    }

    fun clearFullscreenStream() {
        layoutController.clearFullscreenStream()
        viewModelScope.launch { userMessageChannel.send(FullScreenMessage.Disabled) }
    }

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean {
        return layoutController.pinStream(streamId, prepend, force)
    }

    fun unpinStream(streamId: String) {
        layoutController.unpinStream(streamId)
    }

    fun clearPinnedStreams() {
        layoutController.clearPinnedStreams()
    }

    fun zoom(streamId: String) {
        val streamItem = uiState.value.streamItems.value.firstOrNull { it.id == streamId }
        val stream = (streamItem as? StreamItem.Stream)?.stream ?: return
        stream.video?.view?.value?.zoom()
    }

    // TODO remove code duplication in CallActionsViewModel
    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value.isAtLeastLocallyEnabled() }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me
            val streams = me?.streams?.value
            val stream = streams?.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
            if (stream != null) me.removeStream(stream)
            val hasStopped = when (input) {
                is Input.Video.Screen -> true.also {
                    input.dispose()
                }

                is Input.Video.Application -> input.tryDisable()
                else -> false
            }
            hasStopped && stream != null
        }
    }

    private fun determineDebounceDelay(participants: List<CallParticipant>, streamItems: List<StreamItem>, callState: CallStateUi): Long {
        // Implement a debounce mechanism to prevent streams updates during audio-to-video call upgrades (republishing),
        // triggering the update only when the local participant remains alone in the call.
        return if (participants.size > 1 || streamItems.size != 1 || callState != CallStateUi.Connected) DEFAULT_DEBOUNCE_MILLIS
        else SINGLE_STREAM_DEBOUNCE_MILLIS
    }

    companion object {

        const val SINGLE_STREAM_DEBOUNCE_MILLIS = 5000L

        const val DEFAULT_DEBOUNCE_MILLIS = 100L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val layoutController = StreamLayoutControllerImpl.getInstance(
                        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
                    )
                    return StreamViewModel(configure, layoutController) as T
                }
            }
    }
}