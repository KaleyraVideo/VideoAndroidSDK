package com.kaleyra.video_sdk.call.callactionnew

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import com.kaleyra.video_sdk.theme.KaleyraTheme

// TODO add label
@Composable
internal fun CameraAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    warning: Boolean = false,
    error: Boolean = false
) {
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = if (checked) R.drawable.ic_kaleyra_call_sheet_enable_camera else R.drawable.ic_kaleyra_call_sheet_disable_camera),
        contentDescription = stringResource(id = if (checked) R.string.kaleyra_call_sheet_description_enable_camera else R.string.kaleyra_call_sheet_description_disable_camera),
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        badgeText = if (warning || error) "!" else null,
        badgeBackgroundColor = if (error) MaterialTheme.colorScheme.error else KaleyraTheme.colors.warning,
        badgeContentColor = if (error) MaterialTheme.colorScheme.onError else KaleyraTheme.colors.onWarning
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionPreview() {
    KaleyraM3Theme {
        Surface {
            CameraAction(false, {})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionWarningPreview() {
    KaleyraM3Theme {
        Surface {
            CameraAction(false, {}, warning = true)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionErrorPreview() {
    KaleyraM3Theme {
        Surface {
            CameraAction(false, {}, error = true)
        }
    }
}