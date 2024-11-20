package com.kaleyra.video_sdk.call.bottomsheet.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi

private const val MIC_ACTION_ID = "mic_action_id"

private const val CAMERA_ACTION_ID = "camera_action_id"

private const val FLIP_CAMERA_ACTION_ID = "flip_camera_action_id"

private const val CHAT_ACTION_ID = "chat_action_id"

private const val WHITEBOARD_ACTION_ID = "whiteboard_action_id"

private const val AUDIO_ACTION_ID = "audio_action_id"

private const val FILE_SHARE_ACTION_ID = "file_share_action_id"

private const val SCREEN_SHARE_ACTION_ID = "screen_share_action_id"

private const val VIRTUAL_BACKGROUND_ACTION_ID = "virtual_background_action_id"

private const val HANG_UP_ACTION_ID = "hang_up_action_id"

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
    override val id: String = HANG_UP_ACTION_ID,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class FlipCameraAction(
    override val id: String = FLIP_CAMERA_ACTION_ID,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class AudioAction(
    override val id: String = AUDIO_ACTION_ID,
    val audioDevice: AudioDeviceUi = AudioDeviceUi.Muted,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class ChatAction(
    override val id: String = CHAT_ACTION_ID,
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class FileShareAction(
    override val id: String = FILE_SHARE_ACTION_ID,
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class WhiteboardAction(
    override val id: String = WHITEBOARD_ACTION_ID,
    override val isEnabled: Boolean = true,
    override val notificationCount: Int = 0
) : NotifiableCallAction

@Immutable
data class VirtualBackgroundAction(
    override val id: String = VIRTUAL_BACKGROUND_ACTION_ID,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false,
) : ToggleableCallAction

@Immutable
data class MicAction(
    override val id: String = MIC_ACTION_ID,
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false,
) : InputCallAction

@Immutable
data class CameraAction(
    override val id: String = CAMERA_ACTION_ID,
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean = true,
    override val isToggled: Boolean = false
) : InputCallAction


@Immutable
sealed class ScreenShareAction : ToggleableCallAction {

    @Immutable
    data class UserChoice(
        override val id: String = SCREEN_SHARE_ACTION_ID,
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : ScreenShareAction()

    @Immutable
    data class App(
        override val id: String = SCREEN_SHARE_ACTION_ID,
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : ScreenShareAction()

    @Immutable
    data class WholeDevice(
        override val id: String = SCREEN_SHARE_ACTION_ID,
        override val isEnabled: Boolean = true,
        override val isToggled: Boolean = false
    ) : ScreenShareAction()
}