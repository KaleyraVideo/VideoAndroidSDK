package com.kaleyra.video_sdk.call.screennew

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi

interface CallActionUI {
    val isEnabled: Boolean
}

interface ToggleableCallAction : CallActionUI {
    val isToggled: Boolean
}

interface InputCallAction : ToggleableCallAction {
    val state: State

    enum class State {
        Ok,
        Warning,
        Error
    }
}

@Immutable
data class HangUpAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class FlipCameraAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class AudioAction(
    val audioDevice: AudioDeviceUi = AudioDeviceUi.LoudSpeaker,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class ChatAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class FileShareAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class WhiteboardAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class VirtualBackgroundAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class MicAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : InputCallAction

@Immutable
data class CameraAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : InputCallAction

@Immutable
data class ScreenShareAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : InputCallAction