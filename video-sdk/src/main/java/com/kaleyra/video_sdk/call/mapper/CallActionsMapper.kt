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
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.mapper.InputMapper.hasAudio
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioOnly
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.hasVirtualBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal object CallActionsMapper {

    fun CallUI.isFileSharingSupported(): Flow<Boolean> {
        return this.actions.map { actions -> actions.any { action -> action is CallUI.Action.FileShare } }
    }

    fun CallUI.toCallActions(): Flow<List<CallActionUI>> =
        combine(
            actions,
            hasVirtualBackground(),
            isAudioOnly(),
            hasAudio()
        ) { actions, hasVirtualBackground, isAudioOnly, hasAudio ->
            actions.mapNotNull { action ->
                when {
                    action is CallUI.Action.ToggleMicrophone && hasAudio -> MicAction()
                    action is CallUI.Action.ToggleCamera && !isAudioOnly -> CameraAction()
                    action is CallUI.Action.SwitchCamera && !isAudioOnly -> FlipCameraAction()
                    action is CallUI.Action.HangUp -> HangUpAction()
                    action is CallUI.Action.Audio -> AudioAction()
                    action is CallUI.Action.OpenChat.Full -> ChatAction()
                    action is CallUI.Action.FileShare -> FileShareAction()
                    action is CallUI.Action.ScreenShare.Companion || action is CallUI.Action.ScreenShare.UserChoice -> ScreenShareAction.UserChoice()
                    action is CallUI.Action.ScreenShare.App -> ScreenShareAction.App()
                    action is CallUI.Action.ScreenShare.WholeDevice -> ScreenShareAction.WholeDevice()
                    action is CallUI.Action.OpenWhiteboard.Full -> WhiteboardAction()
                    action is CallUI.Action.CameraEffects && hasVirtualBackground -> VirtualBackgroundAction()
                    action is CallUI.Action.Custom -> action.toUI()
                    else -> null
                }
            }
        }.distinctUntilChanged()
}

private fun CallUI.Action.Custom.toUI(): CustomCallAction {
    return CustomAction(
        id = id,
        icon = config.icon,
        buttonTexts = CustomCallAction.ButtonTexts(config.text, config.accessibilityLabel),
        buttonColors = config.appearance?.let {
            CustomCallAction.ButtonsColors(
                buttonColor = it.buttonColor,
                buttonContentColor = it.buttonContentColor
            )
        },
        onClick = config.action
    )
}
