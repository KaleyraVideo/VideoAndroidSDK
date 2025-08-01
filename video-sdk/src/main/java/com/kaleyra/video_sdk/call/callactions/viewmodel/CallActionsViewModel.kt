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
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video.conversation.Chat
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.mapper.InputMapper.toAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraVideoInput
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_common_ui.utils.FlowUtils
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CHAT_ACTION_ID
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FILE_SHARE_ACTION_ID
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.InputCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.NotifiableCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SIGNATURE_ACTION_ID
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SettingsAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SignatureAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.toCallActions
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.toMySignDocumentsCreationTimes
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.toOtherFilesCreationTimes
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasCameraUsageRestriction
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasUsbCamera
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isSharingScreen
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isMeParticipantInitialized
import com.kaleyra.video_sdk.call.mapper.SignDocumentMapper.toSignDocumentUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.call.virtualbackground.state.VirtualBackgroundStateManager
import com.kaleyra.video_sdk.call.virtualbackground.state.VirtualBackgroundStateManagerImpl
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.CameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MicMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CallActionsViewModel(
    configure: suspend () -> Configuration,
    private val virtualBackgroundStateManager: VirtualBackgroundStateManager
) : BaseViewModel<CallActionsUiState>(configure) {

    override fun initialState() = CallActionsUiState()

    private val userMessageChannel = Channel<UserMessage>(Channel.BUFFERED)

    val userMessage: Flow<UserMessage> = userMessageChannel.receiveAsFlow()

    private val availableInputs: Set<Input>?
        get() = call.getValue()?.inputs?.availableInputs?.value

    private var lastFileShareCreationTime = MutableStateFlow(-1L)

    private var lastSignDocumentCreationTime = MutableStateFlow(-1L)

    private val chat: MutableSharedFlow<Chat> = MutableSharedFlow(replay = 1)

    init {
        viewModelScope.launch {
            val call = call.first()

            val availableCallActionsFlow = call
                .toCallActions()
                .shareInEagerly(this)

            val isCallActiveFlow = call.state
                .map { it is Call.State.Connected }
                .stateIn(this, SharingStarted.Eagerly, false)

            val isCallEndedFlow = call.state
                .map { it is Call.State.Disconnecting || it is Call.State.Disconnected.Ended }
                .stateIn(this, SharingStarted.Eagerly, false)

            val isMyMicEnabledFlow = call
                .isMyMicEnabled()
                .stateIn(this, SharingStarted.Eagerly, call.type.value.isAudioEnabled())

            val isMyCameraEnabledFlow = call
                .isMyCameraEnabled()
                .stateIn(this, SharingStarted.Eagerly, call.type.value.isVideoEnabled())

            val hasUsbCameraFlow = call
                .hasUsbCamera()
                .stateIn(this, SharingStarted.Eagerly, false)

            val isSharingScreenFlow = call
                .isSharingScreen()
                .stateIn(this, SharingStarted.Eagerly, false)

            val isVirtualBackgroundEnabledFlow = virtualBackgroundStateManager.isVirtualBackgroundEnabled

            val isLocalParticipantInitializedFlow = call
                .isMeParticipantInitialized()
                .stateIn(this, SharingStarted.Eagerly, false)

            val audioDeviceFlow = call.toCurrentAudioDeviceUi()
                .filterNotNull()
                .debounce(300)
                .stateIn(this, SharingStarted.Eagerly, AudioDeviceUi.Muted)

            val hasCameraUsageRestrictionFlow = call
                .hasCameraUsageRestriction()
                .stateIn(this, SharingStarted.Eagerly, false)

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
                            isEnabled = call.type.value.hasAudio() && isMeParticipantsInitialed && !isCallEnded
                        )

                        is CameraAction -> action.copy(
                            isToggled = !isMyCameraEnabled,
                            isEnabled = call.type.value.hasVideo() && isMeParticipantsInitialed && !isCallEnded
                        )

                        is AudioAction -> action.copy(audioDevice = audioDevice, isEnabled = !isCallEnded)

                        is SettingsAction -> action.copy(isEnabled = !isCallEnded)

                        is FileShareAction -> action.copy(
                            isEnabled = isCallActive && !isCallEnded,
                            notificationCount = (uiState.value.actionList.value.firstOrNull { it is NotifiableCallAction && it.id == FILE_SHARE_ACTION_ID } as? NotifiableCallAction)?.notificationCount
                                ?: 0
                        )

                        is SignatureAction -> action.copy(
                            isEnabled = isCallActive && !isCallEnded,
                            notificationCount = (uiState.value.actionList.value.firstOrNull { it is NotifiableCallAction && it.id == SIGNATURE_ACTION_ID } as? NotifiableCallAction)?.notificationCount
                                ?: 0
                        )

                        is ScreenShareAction.UserChoice -> action.copy(
                            isToggled = isSharingScreen,
                            isEnabled = isCallActive && !isCallEnded
                        )

                        is ScreenShareAction.App -> action.copy(
                            isToggled = isSharingScreen,
                            isEnabled = isCallActive && !isCallEnded
                        )

                        is ScreenShareAction.WholeDevice -> action.copy(
                            isToggled = isSharingScreen,
                            isEnabled = isCallActive && !isCallEnded
                        )

                        is VirtualBackgroundAction -> action.copy(isToggled = call.type.value.hasVideo() && isVirtualBackgroundEnabled, isEnabled = call.type.value.hasVideo() && !isCallEnded)
                        is WhiteboardAction -> action.copy(isEnabled = isCallActive && !isCallEnded)
                        is FlipCameraAction -> action.copy(isEnabled = !hasUsbCamera && isMyCameraEnabled && !isCallEnded)
                        is HangUpAction -> action.copy(isEnabled = !isCallEnded)
                        is ChatAction -> action.copy(
                            isEnabled = !isCallEnded,
                            notificationCount = (uiState.value.actionList.value.firstOrNull { it is NotifiableCallAction && it.id == CHAT_ACTION_ID } as? NotifiableCallAction)?.notificationCount
                                ?: 0
                        )

                        else -> action
                    }
                }
                _uiState.update { it.copy(actionList = updatedActions.toImmutableList()) }
            }.launchIn(this)

            uiState
                .map { it.actionList.value }
                .combine(call.toAudioInput().flatMapLatest { it?.state ?: flowOf(null) }) { _, state ->
                    val inputState = when (state) {
                        is Input.State.Closed.AwaitingPermission -> InputCallAction.State.Warning
                        is Input.State.Closed.Error -> InputCallAction.State.Error
                        else -> InputCallAction.State.Ok
                    }
                    updateAction<MicAction> { action ->
                        action.copy(state = inputState)
                    }
                }.launchIn(this)

            combine(
                uiState.map { it.actionList.value },
                call.toCameraVideoInput().flatMapLatest { it?.state ?: flowOf(null) },
                hasCameraUsageRestrictionFlow
            ) { _, state, cameraUsage ->
                val inputState = when {
                    state is Input.State.Closed.AwaitingPermission -> InputCallAction.State.Warning
                    state is Input.State.Closed.Error || cameraUsage -> InputCallAction.State.Error
                    else -> InputCallAction.State.Ok
                }
                updateAction<CameraAction> { action ->
                    action.copy(state = inputState)
                }
            }.launchIn(this)

            // To ensure that the answer call button is hidden when the user answers the call
            // from the notification, it's needed to check both the call state and the derived state.
            combine(
                call.state,
                call.toCallStateUi()
            ) { callState, callStateUi -> callStateUi == CallStateUi.Ringing && callState != Call.State.Connecting }
                .onEach { isRinging -> _uiState.update { it.copy(isRinging = isRinging) } }
                .launchIn(this)

            hasCameraUsageRestrictionFlow
                .onEach { isUsageRestricted -> _uiState.update { it.copy(isCameraUsageRestricted = isUsageRestricted) } }
                .launchIn(this)

            call.toSignDocumentUi()
                .onEach {
                    val signBadgeCount = it.filter { it.signState != SignDocumentUi.SignStateUi.Completed }.count()
                    uiState.first { it.actionList.value.any { it is SignatureAction } }
                    updateAction<SignatureAction> { action ->
                        action.copy(notificationCount = signBadgeCount)
                    }
                }.launchIn(viewModelScope)

            combine(
                uiState.map { it.actionList.value },
                call.toOtherFilesCreationTimes(),
                lastFileShareCreationTime,
                FileShareVisibilityObserver.isDisplayed
            ) { _, creationTimes, lastFileShareCreationTime, fileShareVisibility ->
                if (fileShareVisibility) {
                    this@CallActionsViewModel.lastFileShareCreationTime.value = creationTimes.lastOrNull() ?: -1
                    return@combine
                }
                val count = creationTimes.count { it > lastFileShareCreationTime }
                uiState.first { it.actionList.value.any { it is FileShareAction } }
                updateAction<FileShareAction> { action ->
                    action.copy(notificationCount = count)
                }
            }.launchIn(this)


            combine(
                uiState.map { it.actionList.value },
                call.whiteboard.notificationCount
            ) { _, notificationCount ->
                uiState.first { it.actionList.value.any { it is WhiteboardAction } }
                updateAction<WhiteboardAction> { action ->
                    action.copy(notificationCount = notificationCount)
                }
            }.launchIn(this)


            val conversation = conversation.first()
            val chatId = call.chatId.first()

            val getChatById: (ChatUI) -> Boolean = {
                it.id == chatId
            }
            val getChatByServerId: suspend (ChatUI) -> Boolean = {
                it.serverId.first() == chatId
            }

            kotlin.runCatching {
                conversation.state.first { it is State.Connected }
                var foundByServerId = false
                val foundById = conversation.chats.getValue()?.any { getChatById(it) } ?: false
                if (!foundById) foundByServerId = conversation.find(chatId).await().isSuccess

                if (foundById || foundByServerId) {
                    conversation.chats.first {
                        it.lastOrNull { getChatById(it) || getChatByServerId(it) }?.let {
                            this@CallActionsViewModel.chat.emit(it)
                            combine(
                                uiState.map { it.actionList.value },
                                it.unreadMessagesCount
                            ) { actionList, unreadMessagesCount ->
                                uiState.first { it.actionList.value.any { it is ChatAction } }
                                updateAction<ChatAction> { action ->
                                    action.copy(notificationCount = unreadMessagesCount)
                                }
                            }.launchIn(this)
                        } != null
                    }
                }
            }
        }
    }

    fun accept() {
        if (ConnectionServiceUtils.isConnectionServiceEnabled) viewModelScope.launch {
            if (KaleyraCallConnectionService.answer()) return@launch
            call.getValue()?.connect()
        }
        else call.getValue()?.connect()
    }

    // TODO remove code duplication in ParticipantsViewModel
    fun toggleMic(activity: Activity?) {
        if (activity !is FragmentActivity) return
        viewModelScope.launch {
            val inputs = call.getValue()?.inputs
            val input = inputs?.request(activity, Inputs.Type.Microphone)?.getOrNull<Input.Audio>() ?: return@launch
            val isMicEnabled = with(input.enabled.value) { this is Input.Enabled.Both || this is Input.Enabled.Local }
            val hasSucceed = if (!isMicEnabled) input.tryEnable() else input.tryDisable()
            if (hasSucceed) {
                val message = if (isMicEnabled) MicMessage.Disabled else MicMessage.Enabled
                userMessageChannel.send(message)
            }
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
            existingCameraVideo.enabled.value.isAtLeastLocallyEnabled() -> {
                val hasSucceed = existingCameraVideo.tryDisable()
                if (hasSucceed) userMessageChannel.trySend(CameraMessage.Disabled)
            }

            else -> {
                val hasSucceed = existingCameraVideo.tryEnable()
                if (hasSucceed) userMessageChannel.trySend(CameraMessage.Enabled)
            }
        }
    }

    private fun requestVideoInputs(call: Call, activity: FragmentActivity) {
        viewModelScope.launch {
            val input = call.inputs.request(activity, Inputs.Type.Camera.External)
                .getOrNull<Input.Video>() ?: return@launch
            val hasSucceed = if (!input.enabled.value.isAtLeastLocallyEnabled()) input.tryEnable() else true
            if (hasSucceed) userMessageChannel.trySend(CameraMessage.Enabled)
        }

        viewModelScope.launch {
            val input = call.inputs.request(activity, Inputs.Type.Camera.Internal)
                .getOrNull<Input.Video>() ?: return@launch
            val hasSucceed = if (!input.enabled.value.isAtLeastLocallyEnabled()) input.tryEnable() else true
            if (hasSucceed) userMessageChannel.trySend(CameraMessage.Enabled)
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
            uiState.value.isRinging -> viewModelScope.launch {
                if (KaleyraCallConnectionService.reject()) return@launch
                call.getValue()?.end()
            }
            else -> viewModelScope.launch {
                if (KaleyraCallConnectionService.hangUp()) return@launch
                call.getValue()?.end()
            }
        }
    }

    fun showChat(context: Context) {
        val conversation = conversation.getValue() ?: return
        viewModelScope.launch {
            val chat = chat.first()
            conversation.show(context, chat)
        }
    }

    // TODO remove code duplication in StreamViewModel
    fun tryStopScreenShare(): Boolean {
        val input = availableInputs?.filter { it is Input.Video.Screen || it is Input.Video.Application }?.firstOrNull { it.enabled.value.isAtLeastLocallyEnabled() }
        val call = call.getValue()
        return if (input == null || call == null) false
        else {
            val me = call.participants.value.me
            val streams = me?.streams?.value
            val stream = streams?.firstOrNull { it.id == SCREEN_SHARE_STREAM_ID }
            if (stream != null) me.removeStream(stream)
            call.inputs.release(Inputs.Type.Screen)
            call.inputs.release(Inputs.Type.Application)
            stream != null
        }
    }

    fun clearFileShareBadge() {
        viewModelScope.launch {
            val call = call.getValue()
            val creationTimes = call?.toOtherFilesCreationTimes()?.first()
            val maxCreationTime = creationTimes?.maxOrNull() ?: return@launch
            lastFileShareCreationTime.value = maxCreationTime
        }
    }

    fun clearSignatureBadge() {
        viewModelScope.launch {
            val call = call.getValue()
            val creationTimes = call?.toMySignDocumentsCreationTimes()?.first()
            val maxCreationTime = creationTimes?.maxOrNull() ?: return@launch
            lastSignDocumentCreationTime.value = maxCreationTime
        }
    }

    private inline fun <reified T> updateAction(
        transform: (T) -> CallActionUI
    ) {
        _uiState.update { state ->
            val actionList = state.actionList.value
            val updatedActionList = actionList.indexOfFirst { it::class == T::class }.takeIf { it != -1 }?.let { index ->
                val action = transform((actionList[index] as T))
                actionList.toMutableList().also { it[index] = action }
            } ?: return
            state.copy(actionList = updatedActionList.toImmutableList())
        }
    }

    companion object {

        private var lastCameraRestrictionMessageTime = 0L

        private const val RESTRICTED_VIDEO_COOLDOWN_MESSAGE_TIME = 1500L

        fun provideFactory(
            configure: suspend () -> Configuration,
            virtualBackgroundStateManager: VirtualBackgroundStateManager = VirtualBackgroundStateManagerImpl.getInstance()
        ) = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallActionsViewModel(configure, virtualBackgroundStateManager) as T
                }
            }
    }
}