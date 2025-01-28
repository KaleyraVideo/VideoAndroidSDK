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

package com.kaleyra.demo_video_sdk.ui.custom_views

import android.os.Parcelable
import com.kaleyra.video_common_ui.CallUI
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class CallConfiguration(
    val actions: Set<ConfigAction> = CallUI.Button.all.mapToConfigActions(),
    val options: CallOptions = CallOptions()
) : Parcelable {

    @Serializable
    @Parcelize
    data class CallOptions(
        val recordingEnabled: Boolean = false,
        val feedbackEnabled: Boolean = false,
        val backCameraAsDefault: Boolean = false
    ) : Parcelable

    fun encode(): String = Json.encodeToString(this)

    companion object {
        fun decode(data: String): CallConfiguration = Json.decodeFromString(data)
    }
}

@Serializable
sealed class ConfigAction : Parcelable {

    companion object {

        val all by lazy {
            setOf(
                ToggleMicrophone,
                ToggleCamera,
                SwitchCamera,
                HangUp,
                FileShare,
                ScreenShare,
                Audio,
                ChangeZoom,
                ChangeVolume,
                ToggleFlashlight,
                OpenChat.ViewOnly,
                OpenChat.Full,
                ShowParticipants,
                OpenWhiteboard.ViewOnly,
                OpenWhiteboard.Full
            )
        }

        val default by lazy {
            setOf(
                ToggleMicrophone,
                ToggleCamera,
                SwitchCamera,
                HangUp,
                Audio,
                ChangeVolume,
                ShowParticipants
            )
        }
    }

    @Serializable
    @Parcelize
    data object ChangeVolume : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleCamera : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleMicrophone : ConfigAction()

    @Serializable
    @Parcelize
    data object SwitchCamera : ConfigAction()

    @Serializable
    @Parcelize
    data object CameraEffects : ConfigAction()

    @Serializable
    @Parcelize
    data object HangUp : ConfigAction()

    @Serializable
    @Parcelize
    data object FileShare : ConfigAction()

    @Serializable
    @Parcelize
    data object ScreenShare : ConfigAction()

    @Serializable
    @Parcelize
    data object Audio : ConfigAction()

    @Serializable
    @Parcelize
    data object ChangeZoom : ConfigAction()

    @Serializable
    @Parcelize
    data object ToggleFlashlight : ConfigAction()

    @Serializable
    @Parcelize
    data object ShowParticipants : ConfigAction()

    @Serializable
    sealed class OpenChat : ConfigAction() {

        @Serializable
        @Parcelize
        data object ViewOnly : OpenChat()

        @Serializable
        @Parcelize
        data object Full : OpenChat()
    }

    @Serializable
    sealed class OpenWhiteboard : ConfigAction() {
        @Serializable
        @Parcelize
        data object ViewOnly : OpenWhiteboard()

        @Serializable
        @Parcelize
        data object Full : OpenWhiteboard()
    }
}

fun Set<CallUI.Button>.mapToConfigActions(): Set<ConfigAction> {
    return mapNotNull { action ->
        when (action) {
            CallUI.Button.AudioOutput -> ConfigAction.Audio
            CallUI.Button.Volume -> ConfigAction.ChangeVolume
            CallUI.Button.Zoom -> ConfigAction.ChangeZoom
            CallUI.Button.FileShare -> ConfigAction.FileShare
            CallUI.Button.HangUp -> ConfigAction.HangUp
            CallUI.Button.Chat -> ConfigAction.OpenChat.Full
            CallUI.Button.Whiteboard -> ConfigAction.OpenWhiteboard.Full
            CallUI.Button.ScreenShare.UserChoice,
            CallUI.Button.ScreenShare.App,
            CallUI.Button.ScreenShare.WholeDevice -> ConfigAction.ScreenShare
            CallUI.Button.Participants -> ConfigAction.ShowParticipants
            CallUI.Button.FlipCamera -> ConfigAction.SwitchCamera
            CallUI.Button.Camera -> ConfigAction.ToggleCamera
            CallUI.Button.FlashLight -> ConfigAction.ToggleFlashlight
            CallUI.Button.Microphone -> ConfigAction.ToggleMicrophone
            CallUI.Button.CameraEffects -> ConfigAction.CameraEffects
            else -> null
        }
    }.toSet()
}

fun Set<ConfigAction>.mapToCallUIButtons(): Set<CallUI.Button> {
    return map { action ->
        when (action) {
            ConfigAction.Audio -> CallUI.Button.AudioOutput
            ConfigAction.ChangeVolume -> CallUI.Button.Volume
            ConfigAction.ChangeZoom -> CallUI.Button.Zoom
            ConfigAction.FileShare -> CallUI.Button.FileShare
            ConfigAction.HangUp -> CallUI.Button.HangUp
            ConfigAction.OpenChat.Full, ConfigAction.OpenChat.ViewOnly -> CallUI.Button.Chat
            ConfigAction.OpenWhiteboard.Full, ConfigAction.OpenWhiteboard.ViewOnly -> CallUI.Button.Whiteboard
            ConfigAction.ScreenShare -> CallUI.Button.ScreenShare.UserChoice
            ConfigAction.ShowParticipants -> CallUI.Button.Participants
            ConfigAction.SwitchCamera -> CallUI.Button.FlipCamera
            ConfigAction.ToggleCamera -> CallUI.Button.Camera
            ConfigAction.ToggleFlashlight -> CallUI.Button.FlashLight
            ConfigAction.ToggleMicrophone -> CallUI.Button.Microphone
            ConfigAction.CameraEffects -> CallUI.Button.CameraEffects
        }
    }.toSet()
}