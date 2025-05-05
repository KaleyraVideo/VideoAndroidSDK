package com.kaleyra.video_sdk.call.bottomsheet.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.InputCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SignatureAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.view.AudioAction
import com.kaleyra.video_sdk.call.callactions.view.CameraAction
import com.kaleyra.video_sdk.call.callactions.view.ChatAction
import com.kaleyra.video_sdk.call.callactions.view.FileShareAction
import com.kaleyra.video_sdk.call.callactions.view.FlipCameraAction
import com.kaleyra.video_sdk.call.callactions.view.HangUpAction
import com.kaleyra.video_sdk.call.callactions.view.MicAction
import com.kaleyra.video_sdk.call.callactions.view.ScreenShareAction
import com.kaleyra.video_sdk.call.callactions.view.SignDocumentAction
import com.kaleyra.video_sdk.call.callactions.view.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.callactions.view.WhiteboardAction
import com.kaleyra.video_sdk.call.screen.model.InputPermissions

@OptIn(ExperimentalPermissionsApi::class)
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
    onSignatureClick: () -> Unit,
    onVirtualBackgroundToggle: (Boolean) -> Unit,
    inputPermissions: InputPermissions = InputPermissions()
) {
    when(val ca = callAction) {
        is HangUpAction -> {
            HangUpAction(
                label = label,
                enabled = ca.isEnabled,
                onClick = onHangUpClick,
                extended = extended,
                modifier = modifier
            )
        }
        is MicAction -> {
            val shouldShowPermissionWarning = with(inputPermissions.micPermission) { this != null && !status.isGranted && status.shouldShowRationale }
            val shouldShowPermissionError = inputPermissions.wasMicPermissionAsked && with(inputPermissions.micPermission) { this != null && !status.isGranted && !status.shouldShowRationale }

            val shouldToggleAction = shouldShowPermissionWarning || shouldShowPermissionError || ca.state == InputCallAction.State.Warning || ca.state == InputCallAction.State.Error
            MicAction(
                checked = ca.isToggled || shouldToggleAction,
                enabled = ca.isEnabled,
                label = label,
                warning = shouldShowPermissionWarning || ca.state == InputCallAction.State.Warning,
                error = shouldShowPermissionError || ca.state == InputCallAction.State.Error,
                onCheckedChange = onMicToggle,
                modifier = modifier
            )
        }
        is CameraAction -> {
            val shouldShowPermissionWarning = with(inputPermissions.cameraPermission) { this != null && !status.isGranted && status.shouldShowRationale }
            val shouldShowPermissionError = inputPermissions.wasCameraPermissionAsked && with(inputPermissions.cameraPermission) { this != null && !status.isGranted && !status.shouldShowRationale }

            val shouldToggleAction = shouldShowPermissionWarning || shouldShowPermissionError || ca.state == InputCallAction.State.Warning || ca.state == InputCallAction.State.Error
            CameraAction(
                checked = ca.isToggled || shouldToggleAction,
                enabled = ca.isEnabled,
                label = label,
                warning = shouldShowPermissionWarning || ca.state == InputCallAction.State.Warning,
                error = shouldShowPermissionError || ca.state == InputCallAction.State.Error,
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
                badgeCount = ca.notificationCount.takeIf { it != 0 } ?: 0,
                onClick = onChatClick,
                modifier = modifier
            )
        }
        is FileShareAction -> {
            FileShareAction(
                label = label,
                enabled = ca.isEnabled,
                badgeCount = ca.notificationCount.takeIf { it != 0 } ?: 0,
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
                badgeCount = ca.notificationCount.takeIf { it != 0 } ?: 0,
                onClick = onWhiteboardClick,
                modifier = modifier
            )
        }
        is SignatureAction -> {
            SignDocumentAction(
                label = label,
                enabled = ca.isEnabled,
                badgeCount = ca.notificationCount.takeIf { it != 0 } ?: 0,
                onClick = onSignatureClick,
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
        is CustomAction -> {
            com.kaleyra.video_sdk.call.callactions.view.CustomAction(
                label = label,
                enabled = ca.isEnabled,
                badgeCount = ca.notificationCount.takeIf { it != 0 } ?: 0,
                icon = ca.icon,
                buttonTexts = ca.buttonTexts,
                buttonColors = ca.buttonColors,
                onClick = ca.onClick,
            )
        }
    }
}