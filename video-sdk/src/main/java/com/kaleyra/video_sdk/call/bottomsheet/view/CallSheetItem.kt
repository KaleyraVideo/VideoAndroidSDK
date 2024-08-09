package com.kaleyra.video_sdk.call.bottomsheet.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.callactions.view.AudioAction
import com.kaleyra.video_sdk.call.callactions.view.CameraAction
import com.kaleyra.video_sdk.call.callactions.view.ChatAction
import com.kaleyra.video_sdk.call.callactions.view.FileShareAction
import com.kaleyra.video_sdk.call.callactions.view.FlipCameraAction
import com.kaleyra.video_sdk.call.callactions.view.HangUpAction
import com.kaleyra.video_sdk.call.callactions.view.MicAction
import com.kaleyra.video_sdk.call.callactions.view.ScreenShareAction
import com.kaleyra.video_sdk.call.callactions.view.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.callactions.view.WhiteboardAction
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.InputCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction

@Composable
internal fun CallSheetItem(
    modifier: Modifier = Modifier,
    callAction: CallActionUI,
    label: Boolean,
    extended: Boolean,
    onHangUpClick: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onCameraToggle: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundToggle: (Boolean) -> Unit
) {
    when(val ca = callAction) {
        is HangUpAction -> {
            HangUpAction(
                enabled = ca.isEnabled,
                onClick = onHangUpClick,
                extended = extended,
                modifier = modifier
            )
        }
        is MicAction -> {
            MicAction(
                checked = ca.isToggled,
                enabled = ca.isEnabled,
                warning = ca.state == InputCallAction.State.Warning,
                error = ca.state == InputCallAction.State.Error,
                onCheckedChange = onMicToggle,
                modifier = modifier
            )
        }
        is CameraAction -> {
            CameraAction(
                checked = ca.isToggled,
                enabled = ca.isEnabled,
                warning = ca.state == InputCallAction.State.Warning,
                error = ca.state == InputCallAction.State.Error,
                onCheckedChange = onCameraToggle,
                modifier = modifier
            )
        }
        is FlipCameraAction -> {
            FlipCameraAction(
                label = label,
                enabled = ca.isEnabled,
                onClick = onFlipCameraClick,
                modifier = modifier
            )
        }
        is AudioAction -> {
            AudioAction(
                audioDevice = ca.audioDevice,
                label = label,
                enabled = ca.isEnabled,
                onClick = onAudioClick,
                modifier = modifier
            )
        }
        is ChatAction -> {
            ChatAction(
                label = label,
                enabled = ca.isEnabled,
                badgeText = ca.notificationCount.takeIf { it != 0 }?.toString(),
                onClick = onChatClick,
                modifier = modifier
            )
        }
        is FileShareAction -> {
            FileShareAction(
                label = label,
                enabled = ca.isEnabled,
                badgeText = ca.notificationCount.takeIf { it != 0 }?.toString(),
                onClick = onFileShareClick,
                modifier = modifier
            )
        }
        is ScreenShareAction -> {
            ScreenShareAction(
                label = label,
                enabled = ca.isEnabled,
                checked = ca.isToggled,
                onCheckedChange = onScreenShareToggle,
                modifier = modifier
            )
        }
        is WhiteboardAction -> {
            WhiteboardAction(
                label = label,
                enabled = ca.isEnabled,
                badgeText = ca.notificationCount.takeIf { it != 0 }?.toString(),
                onClick = onWhiteboardClick,
                modifier = modifier
            )
        }
        is VirtualBackgroundAction -> {
            VirtualBackgroundAction(
                label = label,
                enabled = ca.isEnabled,
                checked = ca.isToggled,
                onCheckedChange = onVirtualBackgroundToggle,
                modifier = modifier
            )
        }
    }
}