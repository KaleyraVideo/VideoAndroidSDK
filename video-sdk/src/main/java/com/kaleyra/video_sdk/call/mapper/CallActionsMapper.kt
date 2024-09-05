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

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasAudio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.hasVirtualBackground
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal object CallActionsMapper {

    fun CallUI.isFileSharingSupported(): Flow<Boolean> {
        return this.actions.map { actions -> actions.any { action -> action is CallUI.Action.FileShare } }
    }

    fun CallUI.toCallActions(companyId: Flow<String>): Flow<List<CallActionUI>> =
        combine(
            actions,
            hasVirtualBackground(),
            isAudioOnly(),
            hasAudio(),
            isGroupCall(companyId)
        ) { actions, hasVirtualBackground, isAudioOnly, hasAudio, isGroupCall ->
            val result = mutableListOf<CallActionUI>()

            val hasMicrophone = actions.any { action -> action is CallUI.Action.ToggleMicrophone && hasAudio }
            val hasCamera = actions.any { action -> action is CallUI.Action.ToggleCamera && !isAudioOnly }
            val switchCamera = actions.any { action -> action is CallUI.Action.SwitchCamera && !isAudioOnly }
            val hangUp = actions.any { action -> action is CallUI.Action.HangUp }
            val audio = actions.any { action -> action is CallUI.Action.Audio }
            val chat = actions.any { action -> action is CallUI.Action.OpenChat.Full && !isGroupCall }
            val fileShare = actions.any { action -> action is CallUI.Action.FileShare }
            val screenShare = actions.any { action -> action is CallUI.Action.ScreenShare }
            val whiteboard = actions.any { action -> action is CallUI.Action.OpenWhiteboard.Full }

            if (hangUp) result += HangUpAction()
            if (hasMicrophone) result += MicAction()
            if (hasCamera) result += CameraAction()
            if (switchCamera) result += FlipCameraAction()
            if (hasVirtualBackground) result += VirtualBackgroundAction()
            if (audio) result += AudioAction()
            if (fileShare) result += FileShareAction()
            if (screenShare) result += ScreenShareAction()
            if (chat) result += ChatAction()
            if (whiteboard) result += WhiteboardAction()

            result
        }.distinctUntilChanged()
}