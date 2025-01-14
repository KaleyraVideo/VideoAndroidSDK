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

package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.common.CallUIFloatingMessagePresenter
import com.kaleyra.video_common_ui.common.FloatingMessagePresenter
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * Event representing the display mode set
 * @property id String
 * @property displayMode DisplayMode
 * @constructor
 */
data class DisplayModeEvent(val id: String, val displayMode: CallUI.DisplayMode)

/**
 *
 * @property call Call
 * @property activityClazz Class<*> the activity that will be used to display the call UI
 * @property actions MutableStateFlow<Set<Action>> The MutableStateFlow containing the set of actions
 * @constructor
 */
class CallUI(
    private val call: Call,
    val activityClazz: Class<*>,
    val actions: MutableStateFlow<Set<Action>> = MutableStateFlow(Action.default),
    val floatingMessagePresenter: FloatingMessagePresenter = CallUIFloatingMessagePresenter(isGlassesSDK = KaleyraVideo.isGlassesSdk, logger = KaleyraVideo.logger)
) : Call by call, FloatingMessagePresenter by floatingMessagePresenter {

    private val _displayModeEvent = MutableSharedFlow<DisplayModeEvent>(1, 1, BufferOverflow.DROP_OLDEST)

    /**
     * A property that returns true if the call is a link call.
     **/
    val isLink: Boolean get() = call is Call.Link
    
    /**
     * A property that indicates whether the user feedback is asked at the end of call.
     **/
    var withFeedback: Boolean = false

    /**
     * The display events successfully sent
     */
    val displayModeEvent: SharedFlow<DisplayModeEvent> = _displayModeEvent.asSharedFlow()

    /**
     * Set the activity display mode
     *
     * @param displayMode The DisplayMode
     * @return Boolean True if the activity was running and the display mode was successfully applied, false otherwise
     */
    fun setDisplayMode(displayMode: DisplayMode): Boolean {
        val context = ContextRetainer.context
        val isCallActivityRunning = context.isActivityRunning(activityClazz)
        // emit the value only if the call activity is currently running
        // to avoid the value is read from the shared flow when recreating the activity again
        // (e.g. tap the call notification when the call activity was previously destroyed)
        if (isCallActivityRunning) {
            _displayModeEvent.tryEmit(DisplayModeEvent(UUID.randomUUID().toString(), displayMode))
        }
        return isCallActivityRunning
    }

    /**
     * Show the call ui
     */
    fun show(): Boolean {
        val isInForeground = AppLifecycle.isInForeground.value
        if (isInForeground) {
            KaleyraUIProvider.startCallActivity(activityClazz)
        }
        return isInForeground
    }

    /**
     * DisplayMode representing the way call UI is displayed
     */
    sealed class DisplayMode {

        /**
         * PictureInPicture display mode
         */
        data object PictureInPicture : DisplayMode()

        /**
         * Foreground display mode
         */
        data object Foreground : DisplayMode()

        /**
         * Background display mode
         */
        data object Background : DisplayMode()
    }

    /**
     * Call Action triggered by the UI
     */
    sealed class Action {

        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            val all by lazy {
                setOf(
                    HangUp,
                    ToggleMicrophone,
                    ToggleCamera,
                    SwitchCamera,
                    CameraEffects,
                    Audio,
                    FileShare,
                    ScreenShare.UserChoice,
                    ChangeZoom,
                    ChangeVolume,
                    ToggleFlashlight,
                    OpenChat.ViewOnly,
                    OpenChat.Full,
                    ShowParticipants,
                    OpenWhiteboard.ViewOnly,
                    OpenWhiteboard.Full,
                )
            }

            /**
             * Default set of Call Actions
             */
            val default by lazy {
                setOf(
                    HangUp,
                    ToggleMicrophone,
                    ToggleCamera,
                    SwitchCamera,
                    Audio,
                    ChangeVolume,
                    ShowParticipants
                )
            }
        }

        /**
         * Change volume action
         */
        data object ChangeVolume : Action()

        /**
         * Toggle camera action
         */
        data object ToggleCamera : Action()

        /**
         * Toggle microphone action
         */
        data object ToggleMicrophone : Action()

        /**
         * Switch camera action
         */
        data object SwitchCamera : Action()

        /**
         * HangUp action
         */
        data object HangUp : Action()

        /**
         * File Share open action
         */
        data object FileShare : Action()

        /**
         * Screen share request action
         */
        sealed class ScreenShare : Action() {

            /**
             * User will be prompted to select in-app screensharing or whole device screensharing
             */
            @Deprecated(
                message = "Screenshare Action is deprecated and it will be removed in a further release.\n" +
                    "Please update using ScreenShare.UserChoice or ScreenShare.App or ScreenShare.WholeDevice.",
                replaceWith = ReplaceWith("UserChoice"))
            companion object : ScreenShare()

            /**
             * Screensharing will capture only app screens
             */
            data object UserChoice: ScreenShare()

            /**
             * Screensharing will capture only app screens
             */
            data object App: ScreenShare()

            /**
             * Screensharing will capture all device's screens
             */
            data object WholeDevice: ScreenShare()
        }

        /**
         * Camera Effects action
         */
        data object CameraEffects : Action()

        /**
         * Audio switches displaying request
         */
        data object Audio : Action()

        /**
         * Change zoom action
         */
        data object ChangeZoom : Action()

        /**
         * Toggle flashlight action
         */
        data object ToggleFlashlight : Action()

        /**
         * Show participants action
         */
        data object ShowParticipants : Action()

        /**
         * Open chat action
         */
        sealed class OpenChat : Action() {
            /**
             * Open chat action with view only
             */
            data object ViewOnly : OpenChat()

            /**
             * Open chat action with view and send messages capabilities
             */
            data object Full : OpenChat()
        }

        /**
         * Open whiteboard action
         */
        sealed class OpenWhiteboard : Action() {
            /**
             * Open whiteboard action with view only
             */
            data object ViewOnly : OpenWhiteboard()

            /**
             * Open whiteboard action with view and interaction capability
             */
            data object Full : OpenWhiteboard()
        }

        data class Custom(val config: Configuration): Action() {

            val id: String = UUID.randomUUID().toString()

            data class Configuration(
                val icon: Int,
                val text: String?,
                val action: () -> Unit,
                val badgeValue: Int = 0,
                val isEnabled: Boolean = true,
                val accessibilityLabel: String? = null,
                val appearance: Appearance? = null
            ) {
                data class Appearance(
                    val buttonColor: Int,
                    val buttonContentColor: Int
                )
            }
        }
    }
}
