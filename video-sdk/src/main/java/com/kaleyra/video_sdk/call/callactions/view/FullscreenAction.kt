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
internal fun FullscreenAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullscreen: Boolean = false,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val text = if (fullscreen) stringResource(id = R.string.kaleyra_call_sheet_fullscreen_off) else stringResource(id = R.string.kaleyra_call_sheet_fullscreen_on)
    CallAction(
        modifier = modifier,
        icon = if (fullscreen) painterResource(id = R.drawable.ic_kaleyra_stream_fullscreen_action_off) else painterResource(id = R.drawable.ic_kaleyra_stream_fullscreen_action_on),
        contentDescription = text,
        enabled = enabled,
        label = if (label) text else null,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FullscreenOnActionPreview() {
    KaleyraTheme {
        Surface {
            FullscreenAction(onClick = {})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FullscreenOffActionPreview() {
    KaleyraTheme {
        Surface {
            FullscreenAction(fullscreen = true, onClick = {})
        }
    }
}