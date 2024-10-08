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
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

internal object CallExtensions {

    fun Call.hasUsbInput(): Boolean {
        val inputs = inputs.availableInputs.value
        return inputs.any { it is Input.Video.Camera.Usb }
    }

    fun Call.isMyInternalCameraEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val videos = me.streams.value.map { it.video.value }
        val video = videos.firstOrNull { it is Input.Video.Camera.Internal }
        return with (video?.enabled?.value) { this is Input.Enabled.Local || this is Input.Enabled.Both }
    }

    fun Call.isMyInternalCameraUsingFrontLens(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        val video = streams.map { it.video.value }.filterIsInstance<Input.Video.Camera.Internal>().firstOrNull()
        return video?.currentLens?.value?.isRear == false
    }

    fun Call.isMyScreenShareEnabled(): Boolean {
        val me = participants.value.me ?: return false
        val streams = me.streams.value
        return streams.firstOrNull { it.video.value is Input.Video.Screen.My || it.video.value is Input.Video.Application } != null
    }

    fun Call.isNotConnected(): Boolean = state.value !is Call.State.Connected

    fun isIncoming(state: Call.State, participants: CallParticipants) =
        state is Call.State.Disconnected && participants.let { it.creator() != it.me && it.creator() != null }

    fun isOutgoing(state: Call.State, participants: CallParticipants) =
        (state is Call.State.Disconnected || state is Call.State.Connecting) && participants.let { it.creator() == it.me }

    fun isOngoing(state: Call.State, participants: CallParticipants) =
        state is Call.State.Connecting || state is Call.State.Connected || participants.creator() == null

    fun Call.hasUsersWithCameraEnabled(): Boolean {
        val participants = participants.value.list
        val streams = participants.map { it.streams.value }.flatten()
        val videos = streams.map { it.video.value }
        return videos.any { it != null && with (it.enabled.value) { this is Input.Enabled.Remote || this is Input.Enabled.Both } && it is Input.Video.Camera }
    }

    fun Call.getMyInternalCamera() = inputs.availableInputs.value.firstOrNull { it is Input.Video.Camera.Internal }

    fun CallUI.shouldShowAsActivity(): Boolean {
        val context = ContextRetainer.context
        return (!context.isDND() && !context.isSilent()) || isOutgoing(state.value, participants.value)
    }

    fun CallUI.showOnAppResumed(coroutineScope: CoroutineScope) {
        AppLifecycle.isInForeground
            .dropWhile { !it }
            .take(1)
            .onEach { show() }
            .launchIn(coroutineScope)
    }
}