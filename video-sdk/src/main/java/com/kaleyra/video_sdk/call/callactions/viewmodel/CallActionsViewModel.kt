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
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.utils.FlowUtils.combine
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.toCallActions
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasUsbCamera
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isSharingScreen
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isMeParticipantInitialized
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.isVirtualBackgroundEnabled
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CallActionsViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallActionsUiState>(configure) {
    override fun initialState() = CallActionsUiState()

    private val callActions = call
        .toCallActions(company.flatMapLatest { it.id })
        .shareInEagerly(viewModelScope)

    private val isCallConnected = call
        .flatMapLatest { it.state }
        .map { it is Call.State.Connected }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isCallEnded = call
        .flatMapLatest { it.state }
        .map { it is Call.State.Disconnecting || it is Call.State.Disconnected.Ended }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isMyCameraEnabled = call
        .isMyCameraEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val isMyMicEnabled = call
        .isMyMicEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val isSharingScreen = call
        .isSharingScreen()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val hasUsbCamera = call
        .hasUsbCamera()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val currentAudioDevice = call.toCurrentAudioDeviceUi()
        .filterNotNull()
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AudioDeviceUi.Muted)

    private val isVirtualBackgroundEnabled = call
        .isVirtualBackgroundEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val isMeParticipantsInitialed = call
        .isMeParticipantInitialized()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    private var wasCameraRestrictionMessageSent = false

    init {
        // TODO check that only the modified call action will be updated ui side

        combine(
            callActions,
            isCallConnected,
            isMyCameraEnabled,
            isMyMicEnabled,
            isSharingScreen,
            currentAudioDevice,
            isVirtualBackgroundEnabled,
            hasUsbCamera,
            isMeParticipantsInitialed
        ) { callActions, isCallConnected, isMyCameraEnabled, isMyMicEnabled, isSharingScreen, currentAudioDevice, isVirtualBackgroundEnabled, hasUsbCamera, isMeParticipantsInitialed ->
            val actions = callActions
                .updateActionIfExists(CallAction.Microphone(isToggled = !isMyMicEnabled, isEnabled = isMeParticipantsInitialed))
                .updateActionIfExists(CallAction.Camera(isToggled = !isMyCameraEnabled, isEnabled = isMeParticipantsInitialed))
                .updateActionIfExists(CallAction.Audio(device = currentAudioDevice))
                .updateActionIfExists(CallAction.FileShare(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.ScreenShare(isToggled = isSharingScreen, isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.VirtualBackground(isToggled = isVirtualBackgroundEnabled))
                .updateActionIfExists(CallAction.Whiteboard(isEnabled = isCallConnected))
                .updateActionIfExists(CallAction.SwitchCamera(isEnabled = !hasUsbCamera && isMyCameraEnabled))
            ImmutableList(actions)
        }
            .distinctUntilChanged()
            .combine(isCallEnded) { actions, isCallEnded ->
                if (!isCallEnded) _uiState.update { it.copy(actionList = actions) }
                isCallEnded
            }
            .launchIn(viewModelScope)
    }

    fun toggleMic(activity: Activity?) {
        if (activity !is FragmentActivity) return
        viewModelScope.launch {
            call.getValue()?.inputs?.request(activity, Inputs.Type.Microphone)
            val input = availableInputs?.lastOrNull { it is Input.Audio }
            if (!isMyMicEnabled.value) input?.tryEnable() else input?.tryDisable()
        }
    }

    fun toggleCamera(activity: Activity?) {
        if (activity !is FragmentActivity) return
        val call = call.getValue() ?: return
        val me = call.participants.value.me ?: return
        val canUseCamera = !me.restrictions.camera.value.usage
        if (!canUseCamera) {
            // Avoid sending a burst of camera restriction message event
            if (wasCameraRestrictionMessageSent) return
            viewModelScope.launch {
                wasCameraRestrictionMessageSent = true
                CallUserMessagesProvider.sendUserMessage(CameraRestrictionMessage())
                delay(1500L)
                wasCameraRestrictionMessageSent = false
            }
            return
        }

        val video = me.streams.value.firstOrNull { it.id == CameraStreamConstants.CAMERA_STREAM_ID }?.video?.value
        if (video != null && call.inputs.availableInputs.value.contains<Input>(video as Input)) {
            if (video.enabled.value) video.tryDisable() else video.tryEnable()
        } else {
            viewModelScope.launch {
                val input = call.inputs.request(activity, Inputs.Type.Camera.External).getOrNull<Input.Video>() ?: return@launch
                input.tryEnable()
            }

            viewModelScope.launch {
                val input = call.inputs.request(activity, Inputs.Type.Camera.Internal).getOrNull<Input.Video>() ?: return@launch
                input.tryEnable()
            }
        }
    }

    fun switchCamera() {
        val camera = availableInputs?.filterIsInstance<Input.Video.Camera.Internal>()?.firstOrNull()
        val currentLens = camera?.currentLens?.value
        val newLens = camera?.lenses?.firstOrNull { it.isRear != currentLens?.isRear } ?: return
        camera.setLens(newLens)
    }

    fun hangUp() {
        if (ConnectionServiceUtils.isConnectionServiceEnabled) viewModelScope.launch { KaleyraCallConnectionService.hangUp() }
        else call.getValue()?.end()
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

    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me ?: return false
            val streams = me.streams.value
            val stream = streams.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
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

    private fun List<CallAction>.updateActionIfExists(action: CallAction): List<CallAction> {
        val index = indexOfFirst { it.javaClass == action.javaClass }.takeIf { it != -1 } ?: return this
        return if (this[index] == action) this else toMutableList().apply { this[index] = action }
    }

    companion object {

        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallActionsViewModel(configure) as T
            }
        }
    }
}