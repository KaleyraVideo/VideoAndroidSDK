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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map

internal object CallActionsMapper {

    fun CallUI.isFileSharingSupported(): Flow<Boolean> {
        return this.buttons.map { actions -> actions.any { action -> action is CallUI.Button.FileShare } }
    }

    fun CallUI.toCallActions(): Flow<List<CallActionUI>> =
        combine(
            buttons,
            hasVirtualBackground(),
            isAudioOnly(),
            hasAudio()
        ) { actions, hasVirtualBackground, isAudioOnly, hasAudio ->
            actions.mapNotNull { action ->
                when {
                    action is CallUI.Button.Microphone -> MicAction(isEnabled = hasAudio)
                    action is CallUI.Button.Camera -> CameraAction(isEnabled = !isAudioOnly)
                    action is CallUI.Button.FlipCamera -> FlipCameraAction(isEnabled = !isAudioOnly)
                    action is CallUI.Button.HangUp -> HangUpAction()
                    action is CallUI.Button.AudioOutput -> AudioAction()
                    action is CallUI.Button.Chat -> ChatAction()
                    action is CallUI.Button.FileShare -> FileShareAction()
                    action is CallUI.Button.ScreenShare.UserChoice -> ScreenShareAction.UserChoice()
                    action is CallUI.Button.ScreenShare.App -> ScreenShareAction.App()
                    action is CallUI.Button.ScreenShare.WholeDevice -> ScreenShareAction.WholeDevice()
                    action is CallUI.Button.Whiteboard -> WhiteboardAction()
                    action is CallUI.Button.CameraEffects -> VirtualBackgroundAction(isEnabled = hasVirtualBackground && !isAudioOnly)
                    action is CallUI.Button.Custom -> action.toUI()
                    else -> null
                }
            }
        }.distinctUntilChanged()
}

private fun CallUI.Button.Custom.toUI(): CustomCallAction {
    return CustomAction(
        id = id,
        icon = config.icon,
        isEnabled = config.isEnabled,
        notificationCount = config.badgeValue,
        buttonTexts = CustomCallAction.ButtonTexts(config.text, config.accessibilityLabel),
        buttonColors = config.appearance?.let {
            CustomCallAction.ButtonsColors(
                buttonColor = it.background,
                buttonContentColor = it.tint,
                disabledButtonColor = Color(it.background).copy(alpha = 0.38f).toArgb(),
                disabledButtonContentColor = Color(it.tint).copy(alpha = 0.38f).toArgb()
            )
        },
        onClick = config.action
    )
}
