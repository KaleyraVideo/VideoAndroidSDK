package com.kaleyra.video_sdk.call.callactionnew

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun ChatAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false,
    badgeText: String? = null
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_chat)
    CallAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_chat),
        contentDescription = text,
        buttonText = text,
        enabled = enabled,
        label = if (label) text else null,
        badgeText = badgeText,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatActionPreview() {
    KaleyraM3Theme {
        Surface {
            ChatAction({})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatActionBadgePreview() {
    KaleyraM3Theme {
        Surface {
            ChatAction({}, badgeText = "1")
        }
    }
}