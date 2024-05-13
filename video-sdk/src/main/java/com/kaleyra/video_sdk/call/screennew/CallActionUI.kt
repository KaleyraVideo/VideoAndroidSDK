package com.kaleyra.video_sdk.call.screennew

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import java.util.UUID

@Stable
interface CallActionUI {
    val id: String
    val isEnabled: Boolean
}

@Stable
interface ToggleableCallAction : CallActionUI {
    val isToggled: Boolean
}

@Stable
interface InputCallAction : ToggleableCallAction {
    val state: State

    enum class State {
        Ok,
        Warning,
        Error
    }
}

@Stable
interface NotifiableCallAction: CallActionUI {
    val notificationCount: Int
}

@Immutable
data class HangUpAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class FlipCameraAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class AudioAction(
    override val id: String = UUID.randomUUID().toString(),
    val audioDevice: AudioDeviceUi = AudioDeviceUi.LoudSpeaker,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class ChatAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class FileShareAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class WhiteboardAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class VirtualBackgroundAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class MicAction(
    override val id: String = UUID.randomUUID().toString(),
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false,
) : InputCallAction

@Immutable
data class CameraAction(
    override val id: String = UUID.randomUUID().toString(),
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : InputCallAction

@Immutable
data class ScreenShareAction(
    override val id: String = UUID.randomUUID().toString(),
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : ToggleableCallAction