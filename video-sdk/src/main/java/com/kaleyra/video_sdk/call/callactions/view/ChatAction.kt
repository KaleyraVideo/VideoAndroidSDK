package com.kaleyra.video_sdk.call.callactions.view

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun ChatAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false,
    badgeCount: Int = 0
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_chat)
    CallAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_chat),
        contentDescription = text,
        buttonText = text,
        enabled = enabled,
        label = if (label) text else null,
        badgeCount = badgeCount,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatActionPreview() {
    KaleyraTheme {
        Surface {
            ChatAction({})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ChatActionBadgePreview() {
    KaleyraTheme {
        Surface {
            ChatAction({}, badgeCount = 1)
        }
    }
}