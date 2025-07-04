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

import androidx.annotation.ColorInt
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
import java.util.Comparator
import java.util.Objects
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
    callUIButtonsProvider: CallUIButtonsProvider = DefaultCallUIButtonsProvider(call.type, call.state, actions, scope),
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
             * Defines a collection of [Comparator] implementations for sorting [CallUI.Button] elements.
             * This sealed class allows for a restricted set of known comparators to be used within the system.
             */
            sealed class Comparators {

                /**
                 * A default comparator for [CallUI.Button]s.
                 *
                 * This comparator sorts buttons based on a predefined order specified by the `all` list.
                 * Buttons appearing earlier in the `all` list will be considered "less than" buttons
                 * appearing later in the list. If a button is not found in the `all` list, its behavior
                 * in the comparison will be based on `indexOf` returning -1, potentially leading to
                 * it being sorted towards the beginning or end depending on the other button's position.
                 */
                data object Default : Comparator<CallUI.Button>, Comparators() {
                    override fun compare(button1: Button?, button2: Button?) = when {
                        button1 is Custom && button2 is Settings -> -1
                        button1 is Settings && button2 is Custom -> 1
                        button1 is Custom -> 1
                        button2 is Custom -> -1
                        else -> all.indexOf(button1) - all.indexOf(button2)
                    }
                }
            }

            /**
             * A set of all Call Buttons
             */
            val all: Set<Button> by lazy {
                linkedSetOf(
                    HangUp,
                    Microphone,
                    Camera,
                    FlipCamera,
                    FileShare,
                    ScreenShare(),
                    Zoom,
                    Volume,
                    FlashLight,
                    Chat,
                    Participants,
                    Whiteboard,
                    Signature,
                    Settings,
                )
            }

            /**
             * Default set of Call Buttons for audio calls
             */
            val audioCall: Set<Button> by lazy {
                linkedSetOf(
                    HangUp,
                    Microphone,
                    Volume,
                    Participants,
                    Settings
                )
            }

            /**
             * Default set of Call Buttons for video calls
             */
            val videoCall: Set<Button> by lazy {
                linkedSetOf(
                    HangUp,
                    Microphone,
                    Camera,
                    FlipCamera,
                    Volume,
                    Participants,
                    Settings,
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
        data class ScreenShare(val onTap: ScreenShareTapAction = ScreenShareTapAction.AskUser) : Button() {

            /**
             * Class representing the action invoked once the ScreenShare button is tapped
             */
            sealed class ScreenShareTapAction {

                /**
                 * ScreenShare RecordAppOnly tap action is a class used to represent the action that will capture only the current application
                 */
                data object RecordAppOnly : ScreenShareTapAction()

                /**
                 * ScreenShare RecordEntireScreen tap action is a class used to represent the action that will capture entire screen
                 * and all the applications that are displayed
                 */
                data object RecordEntireScreen : ScreenShareTapAction()

                /**
                 * ScreenShare AskUser tap action is a class used to represent the action that will capture only app screens
                 */
                data object AskUser : ScreenShareTapAction()
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is ScreenShare) return false
                return true
            }

            override fun hashCode() = Objects.hash(this::class.java.name)
        }

        /**
         * Camera Effects button
         */
        @Deprecated(
            message = "CameraEffects button has been deprecated since version v.4.10.0 and will be removed in a further release.\n" +
                "Camera Effects are now included in settings UI that can be obtained by adding CallUI.Button.Settings.",
            replaceWith = ReplaceWith("CallUi.Button.Settings")
        )
        data object CameraEffects : Button()

        /**
         * Audio output button
         */
        @Deprecated(
            message = "AudioOutput button has been deprecated since version v.4.10.0 and will be removed in a further release.\n" +
                "Audio Outputs are now included in settings UI that can be obtained by adding CallUI.Button.Settings.",
            replaceWith = ReplaceWith("CallUi.Button.Settings")
        )
        data object AudioOutput : Button()

        /**
         * Audio output button
         */
        data object Settings : Button()

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
         * Signature button
         */
        data object Signature : Button()

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
             * @property text String Custom Button text
             * @property action Function0<Unit> Custom Button click action
             * @property badgeValue Int Custom Button badge value
             * @property isEnabled Boolean Custom Button enable state representation, true to flag the button as enabled, false otherwise
             * @property accessibilityLabel String? Optional Custom Button accessibility label
             * @property appearance Appearance? Optional Custom Button appearance configuration
             * @constructor
             */
            class Configuration(
                icon: Int,
                text: String,
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
                    result = 31 * result + text.hashCode()
                    result = 31 * result + badgeValue
                    result = 31 * result + isEnabled.hashCode()
                    result = 31 * result + (accessibilityLabel?.hashCode() ?: 0)
                    result = 31 * result + (appearance?.hashCode() ?: 0)
                    return result
                }

                /**
                 * Custom Call Button Appearance configuration
                 * @property background Int Custom Button background color represented as integer value
                 * @property content Int Custom Button tint color, to be used for tinting icon and text, represented as integer value
                 * @constructor
                 */
                class Appearance(
                    @ColorInt
                    background: Int,
                    @ColorInt
                    content: Int
                ) {
                    internal var onButtonUpdated: (() -> Unit)? = null

                    var background = background
                        set(value) {
                            field = value; onButtonUpdated?.invoke()
                        }
                    var content = content
                        set(value) {
                            field = value; onButtonUpdated?.invoke()
                        }

                    override fun equals(other: Any?): Boolean {
                        if (this === other) return true
                        if (other !is Appearance) return false

                        if (background != other.background) return false
                        if (content != other.content) return false

                        return true
                    }

                    override fun hashCode(): Int {
                        var result = background
                        result = 31 * result + content
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
    @Deprecated(
        message = "Action is deprecated and it will be removed in a further release. Please update using CallUI.Button.",
        replaceWith = ReplaceWith("CallUI.Button")
    )
    sealed class Action {

        /**
         * @suppress
         */
        companion object {

            /**
             * A set of all tools
             */
            @Deprecated(
                message = "`all` actions are deprecated and will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Collections.all")
            )
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
            @Deprecated(
                message = "`default` actions are deprecated and will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Collections.audioCall")
            )
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
        @Deprecated(
            message = "ChangeVolume action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Volume")
        )
        data object ChangeVolume : Action()

        /**
         * Toggle camera action
         */
        @Deprecated(
            message = "ToggleCamera action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Camera")
        )
        data object ToggleCamera : Action()

        /**
         * Toggle microphone action
         */
        @Deprecated(
            message = "Microphone action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Microphone")
        )
        data object ToggleMicrophone : Action()

        /**
         * Switch camera action
         */
        @Deprecated(
            message = "SwitchCamera action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.FlipCamera")
        )
        data object SwitchCamera : Action()

        /**
         * HangUp action
         */
        @Deprecated(
            message = "HangUp action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.HangUp")
        )
        data object HangUp : Action()

        /**
         * File Share open action
         */
        @Deprecated(
            message = "FileShare action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.FileShare")
        )
        data object FileShare : Action()

        /**
         * Screen share request action
         */
        @Deprecated(
            message = "ScreenShare action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.ScreenShare()")
        )
        sealed class ScreenShare : Action() {

            /**
             * User will be prompted to select in-app screensharing or whole device screensharing
             */
            @Deprecated(
                message = "ScreenShare Action is deprecated and it will be removed in a further release.",
                replaceWith = ReplaceWith("CallUI.Button.Screenshare()")
            )
            companion object : ScreenShare()

            /**
             * Screensharing will capture only app screens
             */
            @Deprecated(
                message = "ScreenShare UserChoice Action is deprecated and it will be removed in a further release.",
                replaceWith = ReplaceWith("CallUI.Button.Screenshare(onTap: ScreenShareTapAction.AskUser)")
            )
            data object UserChoice : ScreenShare()

            /**
             * Screensharing will capture only app screens
             */
            @Deprecated(
                message = "ScreenShare App Action is deprecated and it will be removed in a further release.",
                replaceWith = ReplaceWith("CallUI.Button.Screenshare(onTap: ScreenShareTapAction.RecordAppOnly)")
            )
            data object App : ScreenShare()

            /**
             * Screensharing will capture all device's screens
             */
            @Deprecated(
                message = "WholeDevice App Action is deprecated and it will be removed in a further release.",
                replaceWith = ReplaceWith("CallUI.Button.Screenshare(onTap: ScreenShareTapAction.RecordEntireScreen)")
            )
            data object WholeDevice : ScreenShare()
        }

        /**
         * Camera Effects action
         */
        @Deprecated(
            message = "CameraEffects action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.CameraEffects")
        )
        data object CameraEffects : Action()

        /**
         * Audio switches displaying request
         */
        @Deprecated(
            message = "Audio action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.AudioOutput")
        )
        data object Audio : Action()

        /**
         * Change zoom action
         */
        @Deprecated(
            message = "ChangeZoom action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Zoom")
        )
        data object ChangeZoom : Action()

        /**
         * Toggle flashlight action
         */
        @Deprecated(
            message = "ToggleFlashLight action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.FlashLight")
        )
        data object ToggleFlashlight : Action()

        /**
         * Show participants action
         */
        @Deprecated(
            message = "ShowParticipants action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Participants")
        )
        data object ShowParticipants : Action()

        /**
         * Open chat action
         */
        @Deprecated(
            message = "OpenChat action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Chat")
        )
        sealed class OpenChat : Action() {
            /**
             * Open chat action with view only
             */
            @Deprecated(
                message = "OpenChat action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Chat")
            )
            data object ViewOnly : OpenChat()

            /**
             * Open chat action with view and send messages capabilities
             */
            @Deprecated(
                message = "OpenChat action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Chat")
            )
            data object Full : OpenChat()
        }

        /**
         * Open whiteboard action
         */
        @Deprecated(
            message = "OpenWhiteboard action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
            replaceWith = ReplaceWith("CallUI.Button.Whiteboard")
        )
        sealed class OpenWhiteboard : Action() {
            /**
             * Open whiteboard action with view only
             */
            @Deprecated(
                message = "OpenWhiteboard action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Whiteboard")
            )
            data object ViewOnly : OpenWhiteboard()

            /**
             * Open whiteboard action with view and interaction capability
             */
            @Deprecated(
                message = "OpenWhiteboard action has been deprecated and it will be removed in a further release. Please update using CallUI.Button.",
                replaceWith = ReplaceWith("CallUI.Button.Whiteboard")
            )
            data object Full : OpenWhiteboard()
        }
    }
}
