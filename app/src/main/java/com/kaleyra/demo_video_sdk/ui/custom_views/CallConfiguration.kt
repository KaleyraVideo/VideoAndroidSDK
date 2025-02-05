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

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.kaleyra.demo_video_sdk.R
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_utils.ContextRetainer
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
                OpenChat,
                ShowParticipants,
                OpenWhiteboard,
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
    @Parcelize
    data object OpenChat : ConfigAction()

    @Serializable
    @Parcelize
    data object OpenWhiteboard : ConfigAction()

    @Serializable
    @Parcelize
    data class OpenUrl(val url: String) : ConfigAction()
}

fun Set<CallUI.Button>.mapToConfigActions(): Set<ConfigAction> {
    return mapNotNull { action ->
        when (action) {
            CallUI.Button.AudioOutput -> ConfigAction.Audio
            CallUI.Button.Volume -> ConfigAction.ChangeVolume
            CallUI.Button.Zoom -> ConfigAction.ChangeZoom
            CallUI.Button.FileShare -> ConfigAction.FileShare
            CallUI.Button.HangUp -> ConfigAction.HangUp
            CallUI.Button.Chat -> ConfigAction.OpenChat
            CallUI.Button.Whiteboard -> ConfigAction.OpenWhiteboard
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
            ConfigAction.OpenChat -> CallUI.Button.Chat
            ConfigAction.OpenWhiteboard -> CallUI.Button.Whiteboard
            ConfigAction.ScreenShare -> CallUI.Button.ScreenShare.UserChoice
            ConfigAction.ShowParticipants -> CallUI.Button.Participants
            ConfigAction.SwitchCamera -> CallUI.Button.FlipCamera
            ConfigAction.ToggleCamera -> CallUI.Button.Camera
            ConfigAction.ToggleFlashlight -> CallUI.Button.FlashLight
            ConfigAction.ToggleMicrophone -> CallUI.Button.Microphone
            ConfigAction.CameraEffects -> CallUI.Button.CameraEffects
            is ConfigAction.OpenUrl -> CallUI.Button.Custom(
                config = CallUI.Button.Custom.Configuration(
                    icon = R.drawable.common_full_open_on_phone,
                    text = ContextRetainer.context.getString(R.string.open_url_custom_button),
                    action = {
                        KaleyraVideo.conference.call.replayCache.firstOrNull()?.let { ongoingCall ->
                            var floatingMessage: FloatingMessage? = null
                            floatingMessage = FloatingMessage(
                                body = ContextRetainer.context.getString(R.string.open_url_floating_message_title),
                                button = FloatingMessage.Button(
                                    text = ContextRetainer.context.getString(R.string.open_url_floating_message_action),
                                    icon = R.drawable.common_full_open_on_phone,
                                    action = {
                                        floatingMessage?.dismiss()
                                        ContextRetainer.context.startActivity(
                                            Intent(Intent.ACTION_VIEW).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                setData(Uri.parse(action.url))
                                            }
                                        )
                                    }
                                )
                            )
                            ongoingCall.present(floatingMessage)
                        }
                    },
                    appearance = CallUI.Button.Custom.Configuration.Appearance(
                        background = ContextCompat.getColor(ContextRetainer.context, R.color.customButtonColor),
                        tint = Color.WHITE
                    )
                )
            )
        }
    }.toSet()
}