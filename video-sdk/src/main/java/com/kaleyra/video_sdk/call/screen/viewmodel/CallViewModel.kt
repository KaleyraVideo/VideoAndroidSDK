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

package com.kaleyra.video_sdk.call.screen.viewmodel

import android.content.Context
import android.telecom.TelecomManager
import android.util.Rational
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.DisplayModeEvent
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.video_common_ui.mapper.StreamMapper.doOthersHaveStreams
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.toCallActions
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioVideo
import com.kaleyra.video_sdk.call.mapper.InputMapper.isUsbCameraWaitingPermission
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.video_sdk.call.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.video_sdk.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.getWhiteboardRequestEvents
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.arrangement.StreamsHandler
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.utils.CallExtensions.toMyCameraStream
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class CallViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallUiState>(configure), UserMessageViewModel {

    override fun initialState() = CallUiState()

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    private val _whiteboardRequest: Channel<WhiteboardRequest> = Channel(Channel.CONFLATED)
    val whiteboardRequest: Flow<WhiteboardRequest> = _whiteboardRequest.receiveAsFlow()

    val theme = company
        .flatMapLatest { it.combinedTheme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CompanyUI.Theme())

    val shouldAskConnectionServicePermissions: Boolean
        get() = ConnectionServiceUtils.isConnectionServiceSupported && conference.getValue()?.connectionServiceOption != ConnectionServiceOption.Disabled

    private val callState = call
        .toCallStateUi()
        .shareInEagerly(viewModelScope)

    private val streams: Flow<List<StreamUi>> =
        combine(call.toInCallParticipants(), call.toStreamsUi(), call.flatMapLatest { it.state }) { participants, streams, callState -> Triple(participants, streams, callState) }
            .debounce { (participants: List<CallParticipant>, streams: List<StreamUi>, callState: Call.State) ->
                if (participants.size != 1 && streams.size == 1 && callState == Call.State.Connected) SINGLE_STREAM_DEBOUNCE_MILLIS
                else 0L
            }
            .map { (_: List<CallParticipant>, streams: List<StreamUi>, _: Call.State) -> streams }
            .shareInEagerly(viewModelScope)

    private val maxNumberOfFeaturedStreams = MutableStateFlow(DEFAULT_FEATURED_STREAMS_COUNT)

    private val streamsHandler = StreamsHandler(
        streams = streams.map { streams -> streams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID } },
        nOfMaxFeatured = maxNumberOfFeaturedStreams,
        coroutineScope = viewModelScope
    )

    private var fullscreenStreamId = MutableStateFlow<String?>(null)

    private var onCallEnded: MutableSharedFlow<(suspend (Boolean, Boolean, Boolean) -> Unit)> = MutableSharedFlow(replay = 1)

    private var onPipAspectRatio: MutableSharedFlow<(Rational) -> Unit> = MutableSharedFlow(replay = 1)

    private var onDisplayMode: MutableSharedFlow<(CallUI.DisplayMode) -> Unit> = MutableSharedFlow(replay = 1)

    private var onAudioOrVideoChanged: MutableSharedFlow<(Boolean, Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    private var onUsbCameraConnected: MutableSharedFlow<(Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    init {
        viewModelScope.launch {
            if (!KaleyraVideo.isConfigured) requestConfiguration()
            if (KaleyraVideo.conversation.state.value is State.Disconnected) requestConnect()
        }

        viewModelScope.launch {
            val result = withTimeoutOrNull(NULL_CALL_TIMEOUT) {
                call.firstOrNull()
            }
            result ?: onCallEnded.first().invoke(false, false, false)
        }

        var hasCallBeenConnected = false
        viewModelScope.launch {
            val connected = callState.firstOrNull { it is CallStateUi.Connected }
            hasCallBeenConnected = connected != null
        }

        CallUserMessagesProvider.start(call)

        streamsHandler.streamsArrangement
            .combine(callState) { (featuredStreams, thumbnailsStreams), state ->
                if (state is CallStateUi.Disconnected.Ended) {
                    _uiState.update {
                        it.copy(featuredStreams = ImmutableList(listOf()), thumbnailStreams = ImmutableList(listOf()))
                    }
                } else {
                    val thumbnails = thumbnailsStreams.filterNot { it.id == ScreenShareViewModel.SCREEN_SHARE_STREAM_ID }
                    _uiState.update {
                        it.copy(
                            featuredStreams = ImmutableList(featuredStreams),
                            thumbnailStreams = ImmutableList(thumbnails)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        combine(streams, fullscreenStreamId) { streams, fullscreenStreamId ->
            val stream = streams.find { it.id == fullscreenStreamId }
            _uiState.update { it.copy(fullscreenStream = stream) }
        }.launchIn(viewModelScope)

        company
            .flatMapLatest { it.combinedTheme }
            .toWatermarkInfo(company.flatMapLatest { it.name })
            .onEach { watermarkInfo -> _uiState.update { it.copy(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        call
            .toCallActions(company.flatMapLatest { it.id })
            .shareInEagerly(viewModelScope)
            .onEach { _uiState.update { it.copy(areCallActionsReady = true) } }
            .launchIn(viewModelScope)

        call
            .isAudioOnly()
            .onEach { isAudioOnly -> _uiState.update { it.copy(isAudioOnly = isAudioOnly) } }
            .launchIn(viewModelScope)

        callState
            .filter { it is CallStateUi.Disconnecting || it is CallStateUi.Disconnected.Ended }
            .combine(onCallEnded) { callState, onCallEnded ->
                val withFeedback = call.getValue()?.withFeedback ?: false
                onCallEnded.invoke(
                    hasCallBeenConnected && withFeedback,
                    callState is CallStateUi.Disconnected.Ended.Error,
                    callState is CallStateUi.Disconnected.Ended.Kicked
                )
            }
            .launchIn(viewModelScope)

        callState
            .filter { it is CallStateUi.Reconnecting }
            .onEach { fullscreenStream(null) }
            .launchIn(viewModelScope)

        callState
            .onEach { callState -> _uiState.update { it.copy(callState = callState) } }
            .launchIn(viewModelScope)

        combine(
            callState,
            call.isAudioVideo(),
            streams.hasAtLeastAVideoEnabled()
        ) { callState, isAudioVideo, hasAtLeastAVideoEnabled ->
            val enable = callState == CallStateUi.Connected && (isAudioVideo || hasAtLeastAVideoEnabled)
            _uiState.update { it.copy(shouldAutoHideSheet = enable) }
            enable
        }.takeWhile { !it }.launchIn(viewModelScope)

        call
            .isGroupCall(company.flatMapLatest { it.id })
            .onEach { isGroupCall -> _uiState.update { it.copy(isGroupCall = isGroupCall) } }
            .launchIn(viewModelScope)

        val doOthersHaveStreams = callState
            .takeWhile { it !is CallStateUi.Disconnecting && it !is CallStateUi.Disconnected.Ended }
            .dropWhile { it is CallStateUi.Dialing || it is CallStateUi.Ringing || it is CallStateUi.RingingRemotely || it is CallStateUi.Connecting }
            .combine(call.doOthersHaveStreams()) { _, doOthersHaveStreams -> doOthersHaveStreams }

        doOthersHaveStreams
            .debounce { if (!it) WAITING_FOR_OTHERS_DEBOUNCE_MILLIS else 0L }
            .onEach { value -> _uiState.update { it.copy(amIWaitingOthers = !value) } }
            .takeWhile { !it }
            .onCompletion { _uiState.update { it.copy(amIWaitingOthers = false) } }
            .launchIn(viewModelScope)

        doOthersHaveStreams
            .dropWhile { !it }
            .debounce { if (!it) AM_I_LEFT_ALONE_DEBOUNCE_MILLIS else 0L }
            .onEach { value -> _uiState.update { it.copy(amILeftAlone = !value) } }
            .launchIn(viewModelScope)

        call
            .toRecordingUi()
            .onEach { rec -> _uiState.update { it.copy(recording = rec) } }
            .launchIn(viewModelScope)

        combine(
            call.flatMapLatest { it.displayModeEvent },
            onDisplayMode
        ) { event, onDisplayMode ->
                if (lastDisplayModeEvent?.id == event.id) return@combine
                lastDisplayModeEvent = event
                onDisplayMode.invoke(event.displayMode)
            }
            .combine(callState) { _, callState -> callState}
            .takeWhile { it !is CallStateUi.Disconnected.Ended }
            .launchIn(viewModelScope)

        callState
            .dropWhile { it !is CallStateUi.Connected }
            .onEach { callState ->
                if (callState !is CallStateUi.Disconnected.Ended.HungUp && callState !is CallStateUi.Disconnected.Ended.Error) return@onEach
                val showFeedback = call.getValue()?.withFeedback ?: false
                _uiState.update { it.copy(showFeedback = showFeedback) }
            }
            .launchIn(viewModelScope)

        combine(
            uiState.toPipAspectRatio(),
            onPipAspectRatio
        ) { aspectRatio, onPipAspectRatio ->
            onPipAspectRatio.invoke(aspectRatio)
        }.launchIn(viewModelScope)

        combine(
            call.flatMapLatest { it.preferredType },
            onAudioOrVideoChanged
        ) { preferredType, onAudioOrVideoChanged ->
            onAudioOrVideoChanged.invoke(
                preferredType.isAudioEnabled(),
                preferredType.isVideoEnabled()
            )
        }.launchIn(viewModelScope)

        combine(
            call.isUsbCameraWaitingPermission(),
            onUsbCameraConnected
        ) { isUsbConnecting, onUsbCameraConnected ->
            onUsbCameraConnected.invoke(isUsbConnecting)
        }.launchIn(viewModelScope)

        call
            .getWhiteboardRequestEvents()
            .onEach { event -> _whiteboardRequest.send(event) }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        CallUserMessagesProvider.dispose()
    }

    fun startMicrophone(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.audio?.value != null) return
        viewModelScope.launch {
            call.participants.first { it.me != null }
            call.inputs.request(context, Inputs.Type.Microphone)
        }
    }

    fun startCamera(context: FragmentActivity) {
        val call = call.getValue() ?: return
        if (call.toMyCameraStream()?.video?.value != null) return
        viewModelScope.launch {
            call.participants.first { it.me != null }
            call.inputs.request(context, Inputs.Type.Camera.Internal)
        }
        viewModelScope.launch {
            call.participants.first { it.me != null }
            call.inputs.request(context, Inputs.Type.Camera.External)
        }
    }

    fun updateStreamsArrangement(isMediumSizeDevice: Boolean) {
        val count = when {
            !isMediumSizeDevice -> 2
            else                -> 4
        }
        maxNumberOfFeaturedStreams.value = count
    }

    fun swapThumbnail(streamId: String) = streamsHandler.swapThumbnail(streamId)

    fun fullscreenStream(streamId: String?) {
        fullscreenStreamId.value = streamId
    }

    fun sendUserFeedback(rating: Float, comment: String) {
        val call = call.getValue() ?: return
        val me = call.participants.value.me ?: return
        me.feedback.value = CallParticipant.Me.Feedback(rating.toInt(), comment)
    }

    fun startConnectionService(context: Context) {
        if (!ConnectionServiceUtils.isConnectionServiceSupported) return
        val call = conference.getValue()?.call?.getValue() ?: return
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        telecomManager.addCall(call)
    }

    fun tryStartCallService() {
        val behaviour = conference.getValue()?.connectionServiceOption
        if (behaviour == ConnectionServiceOption.Enabled) {
            KaleyraCallService.start()
        } else {
            conference.getValue()?.call?.getValue()?.end()
        }
    }

    fun setOnCallEnded(block: suspend (hasFeedback: Boolean, hasErrorOccurred: Boolean, hasBeenKicked: Boolean) -> Unit) {
        viewModelScope.launch {
            onCallEnded.emit(block)
        }
    }

    fun setOnPipAspectRatio(block: (Rational) -> Unit) {
        viewModelScope.launch {
            onPipAspectRatio.emit(block)
        }
    }

    fun setOnDisplayMode(block: (CallUI.DisplayMode) -> Unit) {
        viewModelScope.launch {
            onDisplayMode.emit(block)
        }
    }

    fun setOnAudioOrVideoChanged(block: (isAudioEnabled: Boolean, isVideoEnabled: Boolean) -> Unit) {
        viewModelScope.launch {
            onAudioOrVideoChanged.emit(block)
        }
    }

    fun setOnUsbCameraConnected(block: (Boolean) -> Unit) {
        viewModelScope.launch {
            onUsbCameraConnected.emit(block)
        }
    }

    companion object {

        private var lastDisplayModeEvent: DisplayModeEvent? = null

        const val DEFAULT_FEATURED_STREAMS_COUNT = 2
        const val SINGLE_STREAM_DEBOUNCE_MILLIS = 5000L
        const val WAITING_FOR_OTHERS_DEBOUNCE_MILLIS = 2000L
        const val AM_I_LEFT_ALONE_DEBOUNCE_MILLIS = 5000L
        const val NULL_CALL_TIMEOUT = 1000L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallViewModel(configure) as T
                }
            }
    }

}