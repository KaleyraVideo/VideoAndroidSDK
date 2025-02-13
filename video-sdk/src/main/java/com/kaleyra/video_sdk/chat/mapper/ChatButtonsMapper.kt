/*
q * Copyright 2023 Kaleyra @ https://www.kaleyra.com
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

package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction

internal object ChatButtonsMapper {

    internal fun Set<ChatUI.Button>.mapToChatActions(
        call: (
            Call.Type,
            Long?,
            Call.Recording.Type?) -> Unit
    ): Set<ChatAction> {
        val actionsSet = mutableSetOf<ChatAction>()
        val actions = this@mapToChatActions.filterIsInstance<ChatUI.Button.Call>()
        actions.firstOrNull { !it.callType.hasVideo() }?.also { action ->
            actionsSet.add(ChatAction.AudioCall {
                call(action.callType, action.maxDuration, action.recordingType)
            })
        }
        actions.firstOrNull { it.callType.hasVideo() && !it.callType.isVideoEnabled() }?.also { action ->
            actionsSet.add(ChatAction.AudioUpgradableCall {
                call(action.callType, action.maxDuration, action.recordingType)
            })
        }
        actions.firstOrNull { it.callType.isVideoEnabled() }?.also { action ->
            actionsSet.add(ChatAction.VideoCall {
                call(action.callType, action.maxDuration, action.recordingType)
            })
        }
        return actionsSet
    }
}