package com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactionnew.CallActionBadge
import com.kaleyra.video_sdk.call.callactionnew.audioPainterFor
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.NotifiableCallAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun SheetPanelItem(
    callAction: CallActionUI,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterFor(callAction),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textFor(callAction),
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
        is FlipCameraAction -> stringResource(id = R.string.kaleyra_call_sheet_flip_camera)
        is AudioAction -> stringResource(id = R.string.kaleyra_call_sheet_audio)
        is ChatAction -> stringResource(id = R.string.kaleyra_call_sheet_chat)
        is FileShareAction -> stringResource(id = R.string.kaleyra_call_sheet_file_share)
        is WhiteboardAction -> stringResource(id = R.string.kaleyra_call_sheet_whiteboard)
        is VirtualBackgroundAction -> stringResource(id = R.string.kaleyra_call_sheet_virtual_background)
        else -> ""
    }

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelFlipCameraItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FlipCameraAction())
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelAudioItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = AudioAction())
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelChatItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = ChatAction(notificationCount = 2))
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelFileShareItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = FileShareAction(notificationCount = 2))
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelWhiteboardItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = WhiteboardAction(notificationCount = 2))
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelVirtualBackgroundItemPreview() {
    KaleyraM3Theme {
        Surface {
            SheetPanelItem(callAction = VirtualBackgroundAction())
        }
    }
}
