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

package com.kaleyra.video_sdk.call.callactions.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi

/**
 * CallAction representation of an action triggered by the Call UI
 * @property isEnabled Boolean
 */
@Stable
sealed interface CallAction {

    /**
     * True if the action is enabled, false otherwise
     */
    val isEnabled: Boolean

    /**
     * Representation of toggleable Call Action
     * @property isToggled Boolean
     */
    sealed interface Toggleable : CallAction {
        /**
         * True if the toggleable action is toggled, false otherwise
         */
        val isToggled: Boolean
    }

    /**
     * Camera Call Action
     * @property isEnabled Boolean enabled flag
     * @property isToggled Boolean toggled flag
     * @constructor
     */
    data class Camera(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    /**
     * Microphone Call Action
     * @property isEnabled Boolean enabled flag
     * @property isToggled Boolean enabled flag
     * @constructor
     */
    data class Microphone(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    /**
     * Screen Share Call Action
     * @property isEnabled Boolean enabled flag
     * @property isToggled Boolean toggled flag
     * @constructor
     */
    data class ScreenShare(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    /**
     * Virtual Background Call Action
     * @property isEnabled Boolean enabled flag
     * @property isToggled Boolean toggled flag
     * @constructor
     */
    data class VirtualBackground(
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : Toggleable

    /**
     * Switch Camera Call Action
     * @property isEnabled Boolean enabled flag
     * @constructor
     */
    data class SwitchCamera(override val isEnabled: Boolean = true) : CallAction

    /**
     * HangUp Call Action
     * @property isEnabled Boolean enabled  flag
     * @constructor
     */
    data class HangUp(override val isEnabled: Boolean = true) : CallAction

    /**
     * Chat Call Action
     * @property isEnabled Boolean enabled flag
     * @constructor
     */
    data class Chat(override val isEnabled: Boolean = true) : CallAction

    /**
     * Whiteboard Call Action
     * @property isEnabled Boolean enabled flag
     * @constructor
     */
    data class Whiteboard(override val isEnabled: Boolean = true) : CallAction

    /**
     * File Share Call Action
     * @property isEnabled Boolean enabled flag
     * @constructor
     */
    data class FileShare(override val isEnabled: Boolean = true) : CallAction

    /**
     * Audio Call Action
     * @property isEnabled Boolean enabled flag
     * @property device AudioDeviceUi current audio device
     * @constructor
     */
    data class Audio(override val isEnabled: Boolean = true, val device: AudioDeviceUi = AudioDeviceUi.Muted) :
        CallAction
}
