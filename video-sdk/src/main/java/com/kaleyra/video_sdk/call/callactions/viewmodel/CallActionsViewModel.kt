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

package com.kaleyra.video_sdk.call.callactions.viewmodel

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.utils.FlowUtils
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.toCallActions
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasUsbCamera
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isSharingScreen
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isMeParticipantInitialized
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.isVirtualBackgroundEnabled
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
  
    override fun initialState() = CallActionsUiState()

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    init {
        viewModelScope.launch {
            val call = call.first()

            val availableCallActionsFlow = call
                .toCallActions(company.flatMapLatest { it.id })
                .shareInEagerly(this)

            val isCallActiveFlow = call.state
                .map { it is Call.State.Connected }
                .stateIn(this, SharingStarted.Eagerly, false)

            val isCallEndedFlow = call.state
                .map { it is Call.State.Disconnecting || it is Call.State.Disconnected.Ended }
                .stateIn(this, SharingStarted.Eagerly, false)

            val isMyMicEnabledFlow = call
                .isMyMicEnabled()
                .stateIn(this, SharingStarted.Eagerly, true)

            val isMyCameraEnabledFlow = call
                .isMyCameraEnabled()
                .stateIn(this, SharingStarted.Eagerly, true)

            val hasUsbCameraFlow = call
                .hasUsbCamera()
                .stateIn(this, SharingStarted.Eagerly, false)

            val isSharingScreenFlow = call
                .isSharingScreen()
                .stateIn(this, SharingStarted.Eagerly, false)

            val isVirtualBackgroundEnabledFlow = call
                .isVirtualBackgroundEnabled()
                .stateIn(this, SharingStarted.Eagerly, false)

            val isLocalParticipantInitializedFlow = call
                .isMeParticipantInitialized()
                .stateIn(this, SharingStarted.Eagerly, false)

            val audioDeviceFlow = call.toCurrentAudioDeviceUi()
                .filterNotNull()
                .debounce(300)
                .stateIn(this, SharingStarted.Eagerly, AudioDeviceUi.Muted)

            FlowUtils.combine(
                availableCallActionsFlow,
                isCallActiveFlow,
                isMyMicEnabledFlow,
                isMyCameraEnabledFlow,
                hasUsbCameraFlow,
                isSharingScreenFlow,
                isLocalParticipantInitializedFlow,
                isVirtualBackgroundEnabledFlow,
                audioDeviceFlow,
                isCallEndedFlow
            ) { actions, isCallActive, isMyMicEnabled, isMyCameraEnabled, hasUsbCamera, isSharingScreen, isMeParticipantsInitialed, isVirtualBackgroundEnabled, audioDevice, isCallEnded ->
                val updatedActions = actions.map { action ->
                    when (action) {
                        is MicAction -> action.copy(
                            isToggled = !isMyMicEnabled,
                            isEnabled = isMeParticipantsInitialed && !isCallEnded
                        )

                        is CameraAction -> action.copy(
                            isToggled = !isMyCameraEnabled,
                            isEnabled = isMeParticipantsInitialed && !isCallEnded
                        )

                        is AudioAction -> action.copy(audioDevice = audioDevice, isEnabled = !isCallEnded)
                        is FileShareAction -> action.copy(isEnabled = isCallActive && !isCallEnded)
                        is ScreenShareAction -> action.copy(
                            isToggled = isSharingScreen,
                            isEnabled = isCallActive && !isCallEnded
                        )

                        is VirtualBackgroundAction -> action.copy(isToggled = isVirtualBackgroundEnabled, isEnabled = !isCallEnded)
                        is WhiteboardAction -> action.copy(isEnabled = isCallActive && !isCallEnded)
                        is FlipCameraAction -> action.copy(isEnabled = !hasUsbCamera && isMyCameraEnabled && !isCallEnded)
                        is HangUpAction -> action.copy(isEnabled = !isCallEnded)
                        is ChatAction -> action.copy(isEnabled = !isCallEnded)
                        else -> action
                    }
                }
                _uiState.update { it.copy(actionList = updatedActions.toImmutableList()) }
            }
                .launchIn(this)

            call
                .toCallStateUi()
                .onEach { state -> _uiState.update { it.copy(isRinging = state == CallStateUi.Ringing) } }
                .launchIn(this)
        }
    }

    fun accept() {
        if (ConnectionServiceUtils.isConnectionServiceEnabled) viewModelScope.launch { KaleyraCallConnectionService.answer() }
        else call.getValue()?.connect()
    }

    fun toggleMic(activity: Activity?) {
        if (activity !is FragmentActivity) return
        viewModelScope.launch {
            val inputs = call.getValue()?.inputs
            val input = inputs?.request(activity, Inputs.Type.Microphone)?.getOrNull<Input.Audio>() ?: return@launch
            if (!input.enabled.value) input.tryEnable() else input.tryDisable()
        }
    }

    fun toggleCamera(activity: Activity?) {
        if (activity !is FragmentActivity) return

        val currentCall = call.getValue() ?: return
        val me = currentCall.participants.value.me ?: return
        val canUseCamera = !me.restrictions.camera.value.usage

        // Early exit if camera usage is restricted, with cooldown for messages
        if (!canUseCamera) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCameraRestrictionMessageTime < RESTRICTED_VIDEO_COOLDOWN_MESSAGE_TIME) return
            lastCameraRestrictionMessageTime = currentTime
            CallUserMessagesProvider.sendUserMessage(CameraRestrictionMessage())
            return
        }

        val existingCameraVideo =
            me.streams.value.firstOrNull { it.id == CameraStreamConstants.CAMERA_STREAM_ID }?.video?.value

        when {
            existingCameraVideo == null || !currentCall.inputs.availableInputs.value.contains(existingCameraVideo) -> requestVideoInputs(currentCall, activity)
            existingCameraVideo.enabled.value -> existingCameraVideo.tryDisable()
            else -> existingCameraVideo.tryEnable()
        }
    }

    private fun requestVideoInputs(call: Call, activity: FragmentActivity) {
        viewModelScope.launch {
            val input = call.inputs.request(activity, Inputs.Type.Camera.External)
                .getOrNull<Input.Video>() ?: return@launch
            input.tryEnable()
        }

        viewModelScope.launch {
            val input = call.inputs.request(activity, Inputs.Type.Camera.Internal)
                .getOrNull<Input.Video>() ?: return@launch
            input.tryEnable()
        }
    }

    fun switchCamera() {
        val camera = availableInputs?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    fun hangUp() {
        when {
            !ConnectionServiceUtils.isConnectionServiceEnabled -> call.getValue()?.end()
            uiState.value.isRinging -> viewModelScope.launch { KaleyraCallConnectionService.reject() }
            else -> viewModelScope.launch { KaleyraCallConnectionService.hangUp() }
        }
    }

    fun showChat(context: Context) {
        val conversation = conversation.getValue()
        val call = call.getValue()
        val participants = call?.participants?.getValue()
        if (conversation == null || participants == null) return
        val companyId = company.getValue()?.id?.getValue()
        val otherParticipants = participants.others.filter { it.userId != companyId }.map { it.userId }
        if (otherParticipants.size > 1) return
        conversation.chat(context = context, otherParticipants.first())
    }

    // TODO remove code duplication in StreamViewModel
    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me
            val streams = me?.streams?.value
            val stream = streams?.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
            if (stream != null) me.removeStream(stream)
            input.tryDisable() && stream != null
        }
    }

    companion object {

        private var lastCameraRestrictionMessageTime = 0L

        private const val RESTRICTED_VIDEO_COOLDOWN_MESSAGE_TIME = 1500L

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallActionsViewModel(configure) as T
                }
            }
    }
}