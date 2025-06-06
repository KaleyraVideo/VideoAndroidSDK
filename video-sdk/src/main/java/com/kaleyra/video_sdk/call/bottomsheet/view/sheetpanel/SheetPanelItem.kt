package com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.NotifiableCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SignatureAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.view.CallActionBadgeCount
import com.kaleyra.video_sdk.call.callactions.view.audioPainterFor
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SheetPanelItem(
    callAction: CallActionUI,
    modifier: Modifier = Modifier
) {
    val contentColor = LocalContentColor.current
    val disabledContentColor = LocalContentColor.current.copy(alpha = 0.38f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterFor(callAction),
            contentDescription = null,
            tint = if (callAction.isEnabled) contentColor else disabledContentColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textFor(callAction),
            color = if (callAction.isEnabled) contentColor else disabledContentColor,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        if (callAction is NotifiableCallAction && callAction.notificationCount != 0) {
            CallActionBadgeCount(count = callAction.notificationCount)
        }
    }
}

@Composable
private fun painterFor(callAction: CallActionUI) =
    when (callAction) {
        is ScreenShareAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_screen_share)
        is FlipCameraAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_flip_camera)
        is AudioAction -> audioPainterFor(callAction.audioDevice)
        is ChatAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_chat)
        is FileShareAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_file_share)
        is WhiteboardAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_whiteboard)
        is MicAction ->
            if (callAction.isToggled) painterResource(id = R.drawable.ic_kaleyra_mic_off)
            else painterResource(id = R.drawable.ic_kaleyra_mic_on)

        is CameraAction ->
            if (callAction.isToggled) painterResource(id = R.drawable.ic_kaleyra_camera_off)
            else painterResource(id = R.drawable.ic_kaleyra_camera_on)

        is SignatureAction -> painterResource(id = R.drawable.ic_kaleyra_signature)
        is HangUpAction -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_hang_up)
        is CustomAction -> painterResource(id = callAction.icon)
        else -> painterResource(id = R.drawable.ic_kaleyra_call_sheet_virtual_background)
    }

@Composable
private fun textFor(callAction: CallActionUI) =
    when (callAction) {
        is ScreenShareAction -> {
            stringResource(
                id = if (!callAction.isToggled) R.string.kaleyra_call_sheet_screen_share else R.string.kaleyra_call_sheet_description_stop_screen_share
            )
        }

        is FlipCameraAction -> stringResource(id = R.string.kaleyra_call_sheet_flip_camera)
        is AudioAction -> stringResource(id = R.string.kaleyra_call_sheet_audio)
        is ChatAction -> stringResource(id = R.string.kaleyra_call_sheet_chat)
        is FileShareAction -> stringResource(id = R.string.kaleyra_call_sheet_file_share)
        is WhiteboardAction -> stringResource(id = R.string.kaleyra_call_sheet_whiteboard)
        is VirtualBackgroundAction -> stringResource(id = R.string.kaleyra_call_sheet_virtual_background)
        is MicAction ->
            if (callAction.isToggled) stringResource(R.string.kaleyra_call_action_mic_unmute)
            else stringResource(R.string.kaleyra_call_action_mic_mute)

        is CameraAction ->
            if (callAction.isToggled) stringResource(R.string.kaleyra_call_sheet_enable)
            else stringResource(R.string.kaleyra_call_sheet_disable)

        is HangUpAction -> stringResource(id = R.string.kaleyra_call_sheet_hang_up_action)
        is SignatureAction -> stringResource(id = R.string.kaleyra_signature_sign)
        is CustomAction -> callAction.buttonTexts.text ?: ""
        else -> ""
    }

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelScreenShareItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = ScreenShareAction.UserChoice())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelScreenShareItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = ScreenShareAction.UserChoice(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFlipCameraItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = FlipCameraAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFlipCameraItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = FlipCameraAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelAudioItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = AudioAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelAudioItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = AudioAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFileShareItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = FileShareAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFileShareItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = FileShareAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelChatItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = ChatAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelChatItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = ChatAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelWhiteboardItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = WhiteboardAction(notificationCount = 2))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelWhiteboardItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = WhiteboardAction(notificationCount = 2, isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelVirtualBackgroundItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = VirtualBackgroundAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelVirtualBackgroundItemDisabledPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = VirtualBackgroundAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelCustomItemPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelItem(callAction = CustomAction(icon = R.drawable.kaleyra_icon_reply, buttonTexts = CustomCallAction.ButtonTexts("Custom", "Custom")))
        }
    }
}
