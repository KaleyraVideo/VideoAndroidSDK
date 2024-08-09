package com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.kaleyra.video_sdk.call.callactions.view.CallActionBadge
import com.kaleyra.video_sdk.call.callactions.view.audioPainterFor
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.NotifiableCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

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
            CallActionBadge(text = "${callAction.notificationCount}")
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
        else -> ""
    }

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelScreenShareItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = ScreenShareAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelScreenShareItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = ScreenShareAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFlipCameraItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FlipCameraAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFlipCameraItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FlipCameraAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelAudioItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = AudioAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelAudioItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = AudioAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFileShareItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FileShareAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelFileShareItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FileShareAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelChatItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = ChatAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelChatItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = ChatAction(isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelWhiteboardItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = WhiteboardAction(notificationCount = 2))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelWhiteboardItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = WhiteboardAction(notificationCount = 2, isEnabled = false))
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelVirtualBackgroundItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = VirtualBackgroundAction())
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SheetPanelVirtualBackgroundItemDisabledPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = VirtualBackgroundAction(isEnabled = false))
        }
    }
}
