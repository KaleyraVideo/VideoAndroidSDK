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
import com.kaleyra.video_common_ui.call.CallUIButtonsProvider
import com.kaleyra.video_common_ui.call.CallUIFloatingMessagePresenter
import com.kaleyra.video_common_ui.call.DefaultCallUIButtonsProvider
import com.kaleyra.video_common_ui.call.FloatingMessagePresenter
import com.kaleyra.video_common_ui.common.UIButton
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.UUID

/**
 * Event representing the display mode set
 * @property id String
 * @property displayMode DisplayMode
 * @constructor
 */
@Deprecated(
    "DisplayModeEvent has been deprecated and it will be removed in a further release.\n",
    replaceWith = ReplaceWith("PresentationModeEvent()")
)
data class DisplayModeEvent(val id: String, val displayMode: CallUI.DisplayMode)

/**
 * Event representing the presentation mode set
 * @property id String
 * @property presentationMode PresentationMode
 * @constructor
 */
data class PresentationModeEvent(val id: String, val presentationMode: CallUI.PresentationMode)

/**
 * @property call Call
 * @property activityClazz Class<*> the activity that will be used to display the call UI
 * @property actions MutableStateFlow<Set<Action>> The MutableStateFlow containing the set of actions
 * @constructor
 */
class CallUI(
    private val call: Call,
    val activityClazz: Class<*>,
    @Deprecated(
        message = "Action mutable state flow is deprecated and it will be removed in a further release.\nPlease update CallUI Call Buttons via buttonsProvider callback.",
        replaceWith = ReplaceWith("buttonsProvider = { callButtons ->\ncallButtons\n}"))
    val actions: MutableStateFlow<Set<Action>>? = null,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    floatingMessagePresenter: FloatingMessagePresenter = CallUIFloatingMessagePresenter(isGlassesSDK = KaleyraVideo.isGlassesSdk, logger = KaleyraVideo.logger),
    callUIButtonsProvider: CallUIButtonsProvider = DefaultCallUIButtonsProvider(call.preferredType, call.state, actions, scope),
) : Call by call,
    FloatingMessagePresenter by floatingMessagePresenter,
    CallUIButtonsProvider by callUIButtonsProvider {

    private val _presentationModeEvent = MutableSharedFlow<PresentationModeEvent>(1, 1, BufferOverflow.DROP_OLDEST)

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
    @Deprecated(
        "DisplayModeEvent has been deprecated and it will be removed in a further release.\n" +
            "Please refer to the new parameter presentationModeEvent",
    )
    val displayModeEvent: SharedFlow<DisplayModeEvent> = _presentationModeEvent.map { presentationModeEvent ->
        when (presentationModeEvent.presentationMode) {
            PresentationMode.Background -> DisplayModeEvent(UUID.randomUUID().toString(), DisplayMode.Background)
            PresentationMode.Foreground -> DisplayModeEvent(UUID.randomUUID().toString(), DisplayMode.Foreground)
            PresentationMode.PictureInPicture -> DisplayModeEvent(UUID.randomUUID().toString(), DisplayMode.PictureInPicture)
        }
    }.shareIn(scope, SharingStarted.Eagerly)

    /**
     * The presentation mode events successfully sent
     */
    val presentationModeEvent: SharedFlow<PresentationModeEvent> = _presentationModeEvent.asSharedFlow()

    /**
     * Set the activity display mode
     *
     * @param displayMode The DisplayMode
     * @return Boolean True if the activity was running and the display mode was successfully applied, false otherwise
     */
    @Deprecated("setDisplayMode has been deprecated in favor of change(presentationMode: PresentationMode) function and it will be removed in a further release.",
        replaceWith = ReplaceWith("change(PresentationMode.Foreground)"))
    fun setDisplayMode(displayMode: DisplayMode) = when (displayMode) {
        DisplayMode.Background -> change(PresentationMode.Background)
        DisplayMode.Foreground -> change(PresentationMode.Foreground)
        DisplayMode.PictureInPicture -> change(PresentationMode.PictureInPicture)
    }

    fun change(presentationMode: PresentationMode): Boolean {
        val context = ContextRetainer.context
        val isCallActivityRunning = context.isActivityRunning(activityClazz)
        // emit the value only if the call activity is currently running
        // to avoid the value is read from the shared flow when recreating the activity again
        // (e.g. tap the call notification when the call activity was previously destroyed)
        if (isCallActivityRunning) {
            _presentationModeEvent.tryEmit(PresentationModeEvent(UUID.randomUUID().toString(), presentationMode))
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

    sealed class PresentationMode {

        /**
         * PictureInPicture display mode
         */
        data object PictureInPicture : PresentationMode()

        /**
         * Foreground display mode
         */
        data object Foreground : PresentationMode()

        /**
         * Background display mode
         */
        data object Background : PresentationMode()
    }

    /**
     * DisplayMode representing the way call UI is displayed
     */
    @Deprecated(
        "DisplayMode has been deprecated and it will be removed in a further release.\n" +
            "Please update the usage with PresentationMode", replaceWith = ReplaceWith("PresentationMode")
    )
    sealed class DisplayMode {

        /**
         * PictureInPicture display mode
         */
        @Deprecated(
            "DisplayMode.PictureInPicture has been deprecated and it will be removed in a further release.\n" +
                "Please update the usage with PresentationMode.PictureInPicture", replaceWith = ReplaceWith("PresentationMode.PictureInPicture")
        )
        data object PictureInPicture : DisplayMode()

        /**
         * Foreground display mode
         */
        @Deprecated(
            "DisplayMode.Foreground has been deprecated and it will be removed in a further release.\n" +
                "Please update the usage with PresentationMode.Foreground", replaceWith = ReplaceWith("PresentationMode.Foreground")
        )
        data object Foreground : DisplayMode()

        /**
         * Background display mode
         */
        @Deprecated(
            "DisplayMode.Background has been deprecated and it will be removed in a further release.\n" +
                "Please update the usage with PresentationMode.Background", replaceWith = ReplaceWith("PresentationMode.Background")
        )
        data object Background : DisplayMode()
    }

    /**
     * Call Button representing an action on the call UI
     */
    sealed class Button : UIButton {

        /**
         * Collections of Call Buttons
         */
        companion object Collections {

            /**
             * A set of all Call Buttons
             */
            val all: Set<Button> by lazy {
                setOf(
                    HangUp,
                    Microphone,
                    Camera,
                    FlipCamera,
                    CameraEffects,
                    AudioOutput,
                    FileShare,
                    ScreenShare.UserChoice,
                    Zoom,
                    Volume,
                    FlashLight,
                    Chat,
                    Participants,
                    Whiteboard,
                )
            }

            /**
             * Default set of Call Buttons for audio calls
             */
            val audioCall: Set<Button> by lazy {
                setOf(
                    HangUp,
                    Microphone,
                    AudioOutput,
                    Volume,
                    Participants
                )
            }

            /**
             * Default set of Call Buttons for video calls
             */
            val videoCall: Set<Button> by lazy {
                setOf(
                    HangUp,
                    Microphone,
                    Camera,
                    FlipCamera,
                    CameraEffects,
                    AudioOutput,
                    Volume,
                    Participants
                )
            }
        }

        /**
         * Change volume button
         */
        data object Volume : Button()

        /**
         * Toggle camera button
         */
        data object Camera : Button()

        /**
         * Toggle microphone button
         */
        data object Microphone : Button()

        /**
         * Switch camera button
         */
        data object FlipCamera : Button()

        /**
         * HangUp button
         */
        data object HangUp : Button()

        /**
         * File Share open button
         */
        data object FileShare : Button()

        /**
         * Screen share request button
         */
        sealed class ScreenShare : Button() {

            /**
             * Screensharing UserChoice button will capture only app screens
             */
            data object UserChoice : ScreenShare()

            /**
             * Screensharing App button will capture only app screens
             */
            data object App : ScreenShare()

            /**
             * Screensharing WholeDevice button will capture all device's screens
             */
            data object WholeDevice : ScreenShare()
        }

        /**
         * Camera Effects button
         */
        data object CameraEffects : Button()

        /**
         * Audio output button
         */
        data object AudioOutput : Button()

        /**
         * Change zoom button
         */
        data object Zoom : Button()

        /**
         * Toggle Flashlight button
         */
        data object FlashLight : Button()

        /**
         * Show participants button
         */
        data object Participants : Button()

        /**
         * Open chat button
         */
        data object Chat : Button()

        /**
         * Open whiteboard button
         */
        data object Whiteboard : Button()

        /**
         * Custom Call Button
         * @property config Configuration Custom Button's configuration object
         * @property id String Custom Button unique identifier
         * @constructor
         */
        class Custom(
            config: Configuration,
        ) : Button() {
            val id = UUID.randomUUID().toString()

            internal var onButtonUpdated: (() -> Unit)? = null
                set(value) {
                    field = value
                    config.onButtonUpdated = field
                }

            var config = config
                set(value) {
                    field = value; onButtonUpdated?.invoke()
                }

            /**
             * Custom Call Button Configuration
             * @property icon Int Custom Button icon resource identifier
             * @property text String? Optional Custom Button text
             * @property action Function0<Unit> Custom Button click action
             * @property badgeValue Int Custom Button badge value
             * @property isEnabled Boolean Custom Button enable state representation, true to flag the button as enabled, false otherwise
             * @property accessibilityLabel String? Optional Custom Button accessibility label
             * @property appearance Appearance? Optional Custom Button appearance configuration
             * @constructor
             */
            class Configuration(
                icon: Int,
                text: String? = null,
                action: () -> Unit,
                badgeValue: Int = 0,
                isEnabled: Boolean = true,
                accessibilityLabel: String? = null,
                appearance: Appearance? = null
            ) {
                internal var onButtonUpdated: (() -> Unit)? = null
                    set(value) {
                        field = value
                        appearance?.onButtonUpdated = field
                    }

                var icon = icon
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var text = text
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var action = action
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var badgeValue = badgeValue
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var isEnabled = isEnabled
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var accessibilityLabel = accessibilityLabel
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }
                var appearance = appearance
                    set(value) {
                        field = value; onButtonUpdated?.invoke()
                    }

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is Configuration) return false

                    if (icon != other.icon) return false
                    if (text != other.text) return false
                    // Comparing functions is tricky, consider using a unique identifier if possible
                    // if (action != other.action) return false
                    if (badgeValue != other.badgeValue) return false
                    if (isEnabled != other.isEnabled) return false
                    if (accessibilityLabel != other.accessibilityLabel) return false
                    if (appearance != other.appearance) return false

                    return true
                }

                override fun hashCode(): Int {
                    var result = icon
                    result = 31 * result + (text?.hashCode() ?: 0)
                    // result = 31 * result + action.hashCode() // Handle function hashCode carefully
                    result = 31 * result + badgeValue
                    result = 31 * result + isEnabled.hashCode()
                    result = 31 * result + (accessibilityLabel?.hashCode() ?: 0)
                    result = 31 * result + (appearance?.hashCode() ?: 0)
                    return result
                }

                /**
                 * Custom Call Button Appearance configuration
                 * @property background Int Custom Button background color represented as integer value
                 * @property tint Int Custom Button tint color, to be used for tinting icon and text, represented as integer value
                 * @constructor
                 */
                class Appearance(
                    background: Int,
                    tint: Int
                ) {
                    internal var onButtonUpdated: (() -> Unit)? = null

                    var background = background
                        set(value) {
                            field = value; onButtonUpdated?.invoke()
                        }
                    var tint = tint
                        set(value) {
                            field = value; onButtonUpdated?.invoke()
                        }

                    override fun equals(other: Any?): Boolean {
                        if (this === other) return true
                        if (other !is Appearance) return false

                        if (background != other.background) return false
                        if (tint != other.tint) return false

                        return true
                    }

                    override fun hashCode(): Int {
                        var result = background
                        result = 31 * result + tint
                        return result
                    }
                }
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Custom) return false

                if (id != other.id) return false
                if (config != other.config) return false

                return true
            }

            override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + config.hashCode()
                return result
            }
        }
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
            data object UserChoice : ScreenShare()

            /**
             * Screensharing will capture only app screens
             */
            data object App : ScreenShare()

            /**
             * Screensharing will capture all device's screens
             */
            data object WholeDevice : ScreenShare()
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
    }
}

