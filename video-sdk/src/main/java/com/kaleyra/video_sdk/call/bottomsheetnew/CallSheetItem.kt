package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.call.callactionnew.AudioAction
import com.kaleyra.video_sdk.call.callactionnew.CameraAction
import com.kaleyra.video_sdk.call.callactionnew.ChatAction
import com.kaleyra.video_sdk.call.callactionnew.FileShareAction
import com.kaleyra.video_sdk.call.callactionnew.FlipCameraAction
import com.kaleyra.video_sdk.call.callactionnew.HangUpAction
import com.kaleyra.video_sdk.call.callactionnew.MicAction
import com.kaleyra.video_sdk.call.callactionnew.ScreenShareAction
import com.kaleyra.video_sdk.call.callactionnew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.callactionnew.WhiteboardAction
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.CameraAction
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.HangUpAction
import com.kaleyra.video_sdk.call.screennew.InputCallAction
import com.kaleyra.video_sdk.call.screennew.MicAction
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

internal val SheetCallActionModifier = Modifier.animatePlacement()

@Composable
internal fun CallSheetItem(
    modifier: Modifier = Modifier,
    callAction: CallActionUI,
    label: Boolean,
    extended: Boolean,
    onHangUpClick: () -> Unit,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit
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
                onCheckedChange = onMicToggled,
                modifier = modifier
            )
        }
        is CameraAction -> {
            CameraAction(
                checked = ca.isToggled,
                enabled = ca.isEnabled,
                warning = ca.state == InputCallAction.State.Warning,
                error = ca.state == InputCallAction.State.Error,
                onCheckedChange = onCameraToggled,
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
                onClick = onVirtualBackgroundClick,
                modifier = modifier
            )
        }
    }
}