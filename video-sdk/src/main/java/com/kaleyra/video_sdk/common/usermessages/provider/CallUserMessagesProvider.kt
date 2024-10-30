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

package com.kaleyra.video_sdk.common.usermessages.provider

import android.util.Log
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIAlone
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper.toAudioConnectionFailureMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toMutedMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toUsbCameraMessage
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingMessage
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * Call User Messages Provider
 */
object CallUserMessagesProvider {

    private var coroutineScope: CoroutineScope? = null

    private val userMessageChannel = Channel<UserMessage>(Channel.BUFFERED)

    private val _alertMessages: MutableStateFlow<Set<AlertMessage>> = MutableStateFlow(emptySet())

    /**
     * User messages flow
     */
    val userMessage: Flow<UserMessage> = userMessageChannel.receiveAsFlow()

    /**
     * Alert messages flow
     */
    val alertMessages: StateFlow<Set<AlertMessage>> = _alertMessages

    /**
     * Starts the call User Message Provider
     * @param call Flow<CallUI> the call flow
     * @param scope CoroutineScope optional coroutine scope in which to execute the observing
     */
    fun start(call: CallUI, scope: CoroutineScope = MainScope() + CoroutineName("CallUserMessagesProvider")) {
        if (coroutineScope != null) dispose()
        coroutineScope = scope

        userMessageChannel.sendRecordingEvents(call, scope)
        userMessageChannel.sendMutedEvents(call, scope)
        userMessageChannel.sendUsbCameraEvents(call, scope)
        userMessageChannel.sendFailedAudioOutputEvents(call, scope)

        _alertMessages.sendAutomaticRecordingAlertEvents(call, scope)
        _alertMessages.sendLeftAloneEvents(call, scope)
        _alertMessages.sendWaitingForOtherParticipantsEvent(call, scope)
    }

    /**
     * Send user message
     * @param userMessage UserMessage the user message to be sent
     */
    fun sendUserMessage(userMessage: UserMessage) {
        coroutineScope?.launch {
            userMessageChannel.send(userMessage)
        }
    }

    /**
     * Dispose User Message Provider
     */
    fun dispose() {
        _alertMessages.value = setOf()
        coroutineScope?.cancel()
        coroutineScope = null
    }

    private fun Channel<UserMessage>.sendRecordingEvents(call: CallUI, scope: CoroutineScope) {
        call.toRecordingMessage()
            .dropWhile {
                it is RecordingMessage.Stopped
            }.onEach {
                send(it)
            }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendMutedEvents(call: CallUI, scope: CoroutineScope) {
        call.toMutedMessage().onEach { send(it) }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendUsbCameraEvents(call: CallUI, scope: CoroutineScope) {
        call.toUsbCameraMessage().dropWhile { it is UsbCameraMessage.Disconnected }.onEach { send(it) }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendFailedAudioOutputEvents(call: CallUI, scope: CoroutineScope) {
        call.toAudioConnectionFailureMessage().onEach { send(it) }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendWaitingForOtherParticipantsEvent(call: CallUI, scope: CoroutineScope) {
        call.amIWaitingOthers().onEach { amIwaitingForOtherParticipants ->
            val mutableList = value.toMutableSet()
            val newList = if (amIwaitingForOtherParticipants) mutableList.plus(AlertMessage.WaitingForOtherParticipantsMessage) else mutableList.minus(AlertMessage.WaitingForOtherParticipantsMessage)
            value = newList
        }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendLeftAloneEvents(call: CallUI, scope: CoroutineScope) {
        call.amIAlone().dropWhile { it }.combine(call.toCallStateUi()) { recording, callStateUi ->
            recording to callStateUi
        }.onEach {
            val amIAlone = it.first && it.second !is CallStateUi.Disconnected.Ended
            val mutableList = value.toMutableSet()
            val newList = if (amIAlone) mutableList.plus(AlertMessage.LeftAloneMessage) else mutableList.minus(AlertMessage.LeftAloneMessage)
            value = newList
        }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendAutomaticRecordingAlertEvents(call: CallUI, scope: CoroutineScope) {
        call.recording.combine(call.toCallStateUi()) { recording, callStateUi ->
            recording to callStateUi
        }.filter { it.first.type is Call.Recording.Type.OnConnect }.onEach {
            val callStateUi = it.second

            val mutableList = value.toMutableSet()
            val newList = if (callStateUi is CallStateUi.Connecting) mutableList.plus(AlertMessage.AutomaticRecordingMessage) else mutableList.minus(AlertMessage.AutomaticRecordingMessage)
            value = newList
        }.launchIn(scope)
    }
}
