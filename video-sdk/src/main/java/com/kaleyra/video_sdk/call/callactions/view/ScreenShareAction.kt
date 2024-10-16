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
internal fun ScreenShareAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val labelText = stringResource(id = R.string.kaleyra_call_sheet_screen_share)
    val buttonText = stringResource(id = if (!checked) R.string.kaleyra_call_sheet_screen_share else R.string.kaleyra_call_sheet_description_stop_screen_share)
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_screen_share),
        contentDescription = buttonText,
        enabled = enabled,
        buttonText = buttonText,
        label = if (label) labelText else null,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ScreenShareActionPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareAction(false, {})
        }
    }
}