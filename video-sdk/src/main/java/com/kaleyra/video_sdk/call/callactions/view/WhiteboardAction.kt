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
internal fun WhiteboardAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false,
    badgeText: String? = null
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_whiteboard)
    CallAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_whiteboard),
        contentDescription = text,
        enabled = enabled,
        buttonText = text,
        label = if (label) text else null,
        badgeText = badgeText,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun WhiteboardActionPreview() {
    KaleyraTheme {
        Surface {
            WhiteboardAction({})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun WhiteboardActionBadgePreview() {
    KaleyraTheme {
        Surface {
            WhiteboardAction({}, badgeText = "1")
        }
    }
}