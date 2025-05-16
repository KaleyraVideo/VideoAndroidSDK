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

@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.common.usermessages.provider

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_common_ui.mapper.StreamMapper.doOthersHaveStreams
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_common_ui.notification.signature.SignDocumentsVisibilityObserver
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.InputMapper.toAudioConnectionFailureMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toMutedMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.toUsbCameraMessage
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingMessage
import com.kaleyra.video_sdk.call.mapper.toCustomAlertMessage
import com.kaleyra.video_sdk.call.pip.CallUiPipVisibilityObserver
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.DownloadFileMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.SignatureMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.Executors

/**
 * Call User Messages Provider
 */
object CallUserMessagesProvider {

    private const val AM_I_LEFT_ALONE_DEBOUNCE_MILLIS = 5000L
    private const val AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS = 3000L

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
    fun start(call: CallUI, scope: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) + CoroutineName("CallUserMessagesProvider")) {
        if (coroutineScope != null) dispose()
        coroutineScope = scope

        userMessageChannel.sendRecordingEvents(call, scope)
        userMessageChannel.sendMutedEvents(call, scope)
        userMessageChannel.sendUsbCameraEvents(call, scope)
        userMessageChannel.sendFailedAudioOutputEvents(call, scope)
        userMessageChannel.sendSignDocumentsEvents(call, scope)
        userMessageChannel.sendDownloadFilesEvents(call, scope)

        _alertMessages.sendAutomaticRecordingAlertEvents(call, scope)
        _alertMessages.sendAmIAloneEvents(call, scope)
        _alertMessages.sendWaitingForOtherParticipantsEvents(call, scope)
        _alertMessages.sendCustomMessages(call, scope)
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
        _alertMessages.tryEmit(setOf())
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

    private fun Channel<UserMessage>.sendSignDocumentsEvents(call: CallUI, scope: CoroutineScope) {
        val sentSignDocumentsIds = mutableListOf<String>()
        call.sharedFolder.signDocuments.filter { it.isNotEmpty() }.onEach { files ->
            if (SignDocumentsVisibilityObserver.isDisplayed.value) return@onEach
            if (CallUiPipVisibilityObserver.isDisplayed.value) return@onEach
            val file = files.filter { it.id !in sentSignDocumentsIds }.maxByOrNull { it.creationTime } ?: return@onEach
            sentSignDocumentsIds += file.id
            send(SignatureMessage.New(file.id))
        }.launchIn(scope)
    }

    private fun Channel<UserMessage>.sendDownloadFilesEvents(call: CallUI, scope: CoroutineScope) {
        val sentFilesIds = mutableListOf<String>()
        call.sharedFolder.files.filter { it.isNotEmpty() }.onEach { files ->
            if (FileShareVisibilityObserver.isDisplayed.value) return@onEach
            if (CallUiPipVisibilityObserver.isDisplayed.value) return@onEach
            val file = files.filter { it.id !in sentFilesIds && it.sender.userId != call.participants.value.me?.userId }.maxByOrNull { it.creationTime } ?: return@onEach
            val sender = file.sender.combinedDisplayName.first { it != null }
            sentFilesIds += file.id
            send(DownloadFileMessage.New(file.id, sender!!))
        }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendAmIAloneEvents(call: CallUI, scope: CoroutineScope) {
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion {
                value = value.toMutableSet().minus(AlertMessage.LeftAloneMessage)
            }
            .launchIn(scope)

        call.toCallStateUi()
            .filterNot {
                it is CallStateUi.Disconnected.Ended ||
                    it is CallStateUi.Ringing ||
                    it is CallStateUi.RingingRemotely ||
                    it is CallStateUi.Dialing
            }
            .combine(call.doOthersHaveStreams()) { _, doOthersHaveStreams -> doOthersHaveStreams }
            .dropWhile { !it }
            .debounce { doOthersHaveStreams -> if (!doOthersHaveStreams) AM_I_LEFT_ALONE_DEBOUNCE_MILLIS else 0L }
            .onEach { doOthersHaveStreams ->
                val mutableList = value.toMutableSet()
                val newList = if (!doOthersHaveStreams) mutableList.plus(AlertMessage.LeftAloneMessage) else mutableList.minus(AlertMessage.LeftAloneMessage)
                value = newList
            }
            .launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendWaitingForOtherParticipantsEvents(call: CallUI, scope: CoroutineScope) {
        call.state
            .filter { it is Call.State.Connected }
            .onEach {
                call
                    .amIWaitingOthers()
                    .debounce { amIWaitingOthers -> if (amIWaitingOthers) AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS else 0L }
                    .onEach {
                        val mutableList = value.toMutableSet()
                        val newList = if (it) mutableList.plus(AlertMessage.WaitingForOtherParticipantsMessage) else mutableList.minus(AlertMessage.WaitingForOtherParticipantsMessage)
                        value = newList
                    }
                    .takeWhile { it }
                    .onCompletion {
                        value = value.toMutableSet().minus(AlertMessage.WaitingForOtherParticipantsMessage)
                    }
                    .launchIn(scope)
            }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendAutomaticRecordingAlertEvents(call: CallUI, scope: CoroutineScope) {
        call.recording.combine(call.toCallStateUi()) { recording, callStateUi ->
            recording to callStateUi
        }.filter { it.first.type is Call.Recording.Type.Automatic }.onEach {
            val callStateUi = it.second

            val mutableList = value.toMutableSet()
            val newList = if (callStateUi is CallStateUi.Connecting) mutableList.plus(AlertMessage.AutomaticRecordingMessage) else mutableList.minus(AlertMessage.AutomaticRecordingMessage)
            value = newList
        }.launchIn(scope)
    }

    private fun MutableStateFlow<Set<AlertMessage>>.sendCustomMessages(call: CallUI, scope: CoroutineScope) {
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion {
                val mutableList = value.toMutableSet()
                emit(mutableList.filterNot { it is AlertMessage.CustomMessage }.toSet())
            }
            .launchIn(scope)

        call
            .floatingMessages
            .distinctUntilChanged()
            .onEach { floatingMessage ->
                val mutableList = value.toMutableSet()
                val newList =
                    if (floatingMessage != null) {
                        val customAlertMessage = floatingMessage.toCustomAlertMessage()
                        mutableList.plus(customAlertMessage)
                    } else {
                        mutableList.filterNot { it is AlertMessage.CustomMessage }.toSet()
                    }
                emit(newList)
            }.launchIn(scope)
    }
}
