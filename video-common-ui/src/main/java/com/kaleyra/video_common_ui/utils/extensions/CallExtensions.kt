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

package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.CallTypeExtensions.toCallButtons
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile

object CallExtensions {

    internal fun Call.hasUsbInput(): Boolean {
        val inputs = inputs.availableInputs.value
        return inputs.any { it is Input.Video.Camera.Usb }
    }

    internal fun Call.isMyInternalCameraEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val videos = me.streams.value.map { it.video.value }
        val video = videos.firstOrNull { it is Input.Video.Camera.Internal }
        return with(video?.enabled?.value) { this is Input.Enabled.Local || this is Input.Enabled.Both }
    }

    internal fun Call.isMyInternalCameraUsingFrontLens(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        val video = streams.map { it.video.value }.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        return video?.currentLens?.value?.isRear == false
    }

    internal fun Call.isMyScreenShareEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        return streams.firstOrNull { it.video.value is Input.Video.Screen.My || it.video.value is Input.Video.Application } != null
    }

    internal fun Call.isNotConnected(): Boolean = state.value !is Call.State.Connected

    internal fun isIncoming(state: Call.State, participants: CallParticipants) =
        state is Call.State.Disconnected && participants.let { it.creator() != it.me && it.creator() != null }

    internal fun isOutgoing(state: Call.State, participants: CallParticipants) =
        (state is Call.State.Disconnected || state is Call.State.Connecting) && participants.let { it.creator() == it.me }

    internal fun isOngoing(state: Call.State, participants: CallParticipants) =
        state is Call.State.Connecting || state is Call.State.Connected || participants.creator() == null

    internal fun Call.hasUsersWithCameraEnabled(): Boolean {
        val participants = participants.value.list
        val streams = participants.map { it.streams.value }.flatten()
        val videos = streams.map { it.video.value }
        return videos.any { it != null && with(it.enabled.value) { this is Input.Enabled.Remote || this is Input.Enabled.Both } && it is Input.Video.Camera }
    }

    internal fun Call.getMyInternalCamera() = inputs.availableInputs.value.firstOrNull { it is Input.Video.Camera.Internal }

    internal fun CallUI.shouldShowAsActivity(): Boolean {
        val context = ContextRetainer.context
        return (!context.isDND() && !context.isSilent()) || isOutgoing(state.value, participants.value)
    }

    internal fun CallUI.showOnAppResumed(coroutineScope: CoroutineScope) {
        AppLifecycle.isInForeground
            .dropWhile { !it }
            .take(1)
            .onEach { show() }
            .launchIn(coroutineScope)
    }

    suspend internal fun CallUI.toDownloadFiles(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): StateFlow<List<SharedFile>> =
        sharedFolder.files.map { files ->
            files.filterNot {
                it.sender.userId == participants.value.me?.userId
            }
        }.stateIn(scope)

    fun CallUI.Action.toCallUIButton(): CallUI.Button = when (this) {
        CallUI.Action.Audio -> CallUI.Button.AudioOutput
        CallUI.Action.CameraEffects -> CallUI.Button.CameraEffects
        CallUI.Action.ChangeVolume -> CallUI.Button.Volume
        CallUI.Action.ChangeZoom -> CallUI.Button.Zoom
        CallUI.Action.FileShare -> CallUI.Button.FileShare
        CallUI.Action.HangUp -> CallUI.Button.HangUp
        CallUI.Action.OpenChat.Full, CallUI.Action.OpenChat.ViewOnly -> CallUI.Button.Chat
        CallUI.Action.OpenWhiteboard.Full, CallUI.Action.OpenWhiteboard.ViewOnly -> CallUI.Button.Whiteboard
        CallUI.Action.ScreenShare.App -> CallUI.Button.ScreenShare(CallUI.Button.ScreenShare.ScreenShareTapAction.RecordAppOnly)
        CallUI.Action.ScreenShare,
        CallUI.Action.ScreenShare.UserChoice -> CallUI.Button.ScreenShare()

        CallUI.Action.ScreenShare.WholeDevice -> CallUI.Button.ScreenShare(CallUI.Button.ScreenShare.ScreenShareTapAction.RecordEntireScreen)
        CallUI.Action.ShowParticipants -> CallUI.Button.Participants
        CallUI.Action.SwitchCamera -> CallUI.Button.FlipCamera
        CallUI.Action.ToggleCamera -> CallUI.Button.Camera
        CallUI.Action.ToggleFlashlight -> CallUI.Button.FlashLight
        CallUI.Action.ToggleMicrophone -> CallUI.Button.Microphone
    }

    suspend internal fun CallUI.bindCallButtons(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        val addedButtons: MutableList<CallUI.Button> = mutableListOf()

        whiteboard.events
            .combine(state) { whiteboardEvent, callState -> whiteboardEvent to callState }
            .takeWhile { (_, callState) ->
                callState !is Call.State.Disconnected.Ended
            }
            .onEach { (whiteboardEvent, _) ->
                if (whiteboardEvent is Whiteboard.Event.Request.Hide) return@onEach
                if (CallUI.Button.Whiteboard !in addedButtons) addedButtons.add(CallUI.Button.Whiteboard)
                buttonsProvider
                    ?.invoke((type.value.toCallButtons(actions?.value)
                        + addedButtons)
                        .toMutableSet()
                    )
            }
            .launchIn(scope)

        sharedFolder.signDocuments
            .filter { it.isNotEmpty() }
            .onEach {
                if (CallUI.Button.Signature !in addedButtons) addedButtons.add(CallUI.Button.Signature)
                buttonsProvider
                    ?.invoke((
                        type.value.toCallButtons(actions?.value)
                            + addedButtons)
                        .toMutableSet()
                    )
            }.launchIn(scope)

        toDownloadFiles(scope)
            .combine(state) { downloadFiles, callState -> downloadFiles to callState }
            .takeWhile { (_, callState) ->
                callState !is Call.State.Disconnected.Ended
            }
            .filterNot {(downloadFiles, _) -> downloadFiles.isEmpty() }
            .onEach {
                if (CallUI.Button.FileShare !in addedButtons) addedButtons.add(CallUI.Button.FileShare)
                buttonsProvider?.invoke((
                        type.value.toCallButtons(actions?.value)
                            + addedButtons)
                        .toMutableSet()
                    )
            }
            .launchIn(scope)
    }

    fun CallUI.configureCallActivityShow(coroutineScope: CoroutineScope) {
        if (state.value is Call.State.Disconnected.Ended) return
        when {
            isLink -> showOnAppResumed(coroutineScope)
            shouldShowAsActivity() -> show()
        }
    }
}