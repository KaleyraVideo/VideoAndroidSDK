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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.State
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.PresentationModeEvent
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper.isUsbCameraWaitingPermission
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.getWhiteboardRequestEvents
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.utils.CallExtensions.toMyCameraStream
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class MainViewModel(configure: suspend () -> Configuration) : BaseViewModel<MainUiState>(configure) {

    override fun initialState() = MainUiState()

    private val _whiteboardRequest: Channel<WhiteboardRequest> = Channel(Channel.CONFLATED)
    val whiteboardRequest: Flow<WhiteboardRequest> = _whiteboardRequest.receiveAsFlow()

    val theme: StateFlow<Theme> = company
        .flatMapLatest { it.combinedTheme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Theme())

    val shouldAskConnectionServicePermissions: Boolean
        get() = ConnectionServiceUtils.isConnectionServiceSupported && conference.getValue()?.connectionServiceOption != ConnectionServiceOption.Disabled

    private var onCallEnded: MutableSharedFlow<(suspend (Boolean, Boolean, Boolean) -> Unit)> = MutableSharedFlow(replay = 1)

    private var onDisplayMode: MutableSharedFlow<(CallUI.PresentationMode) -> Unit> = MutableSharedFlow(replay = 1)

    private var onAudioOrVideoChanged: MutableSharedFlow<(Boolean, Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    private var onUsbCameraConnected: MutableSharedFlow<(Boolean) -> Unit> = MutableSharedFlow(replay = 1)

    init {
        viewModelScope.launch {
            if (!KaleyraVideo.isConfigured) requestConfiguration()
            if (KaleyraVideo.conversation.state.value is State.Disconnected) requestConnect()

            val call = withTimeoutOrNull(NULL_CALL_TIMEOUT) {
                call.firstOrNull()
            }

            if (call == null) {
                onCallEnded.first().invoke(false, false, false)
                return@launch
            }

            val callState = call.toCallStateUi()

            var hasCallBeenConnected = false
            launch {
                val connected = callState.firstOrNull { it is CallStateUi.Connected }
                hasCallBeenConnected = connected != null
            }

            callState
                .filter { it is CallStateUi.Disconnecting || it is CallStateUi.Disconnected.Ended }
                .combine(onCallEnded) { state, onCallEnded ->
                    val withFeedback = call.withFeedback
                    onCallEnded.invoke(
                        hasCallBeenConnected && withFeedback,
                        state is CallStateUi.Disconnected.Ended.Error,
                        state is CallStateUi.Disconnected.Ended.Kicked
                    )
                }
                .launchIn(viewModelScope)

            callState
                .onEach { state ->
                    _uiState.update { it.copy(isCallEnded = state == CallStateUi.Disconnected.Ended) }
                }
                .launchIn(viewModelScope)

            combine(
                call.presentationModeEvent,
                onDisplayMode
            ) { event, onDisplayMode ->
                if (lastPresentationModeEvent?.id == event.id) return@combine
                lastPresentationModeEvent = event
                onDisplayMode.invoke(event.presentationMode)
            }
                .combine(callState) { _, callState -> callState}
                .takeWhile { it !is CallStateUi.Disconnected.Ended }
                .launchIn(viewModelScope)

            combine(
                call.preferredType,
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

    suspend fun getChatId(): String? {
        val call = call.getValue()
        return call?.chatId?.first()
    }

    fun setOnCallEnded(block: suspend (hasFeedback: Boolean, hasErrorOccurred: Boolean, hasBeenKicked: Boolean) -> Unit) {
        viewModelScope.launch {
            onCallEnded.emit(block)
        }
    }

    fun setOnDisplayMode(block: (CallUI.PresentationMode) -> Unit) {
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

        private var lastPresentationModeEvent: PresentationModeEvent? = null

        const val NULL_CALL_TIMEOUT = 1000L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(configure) as T
                }
            }
    }

}