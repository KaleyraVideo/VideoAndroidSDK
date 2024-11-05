package com.kaleyra.video_sdk.call.callactions.view

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
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
        badgePainter = when {
            warning -> painterResource(R.drawable.ic_kaleyra_call_sheet_warning)
            error -> painterResource(R.drawable.ic_kaleyra_call_sheet_error)
            else -> null
        },
        badgeDescription =  when {
            warning -> stringResource(R.string.kaleyra_call_sheet_description_camera_warning)
            error -> stringResource(R.string.kaleyra_call_sheet_description_camera_error)
            else -> null
        },
        badgeBackgroundColor = if (warning) KaleyraTheme.colors.warning else MaterialTheme.colorScheme.error,
        badgeContentColor = if (warning) KaleyraTheme.colors.onWarning else MaterialTheme.colorScheme.onError
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionPreview() {
    KaleyraTheme {
        Surface {
            CameraAction(false, {})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionWarningPreview() {
    KaleyraTheme {
        Surface {
            CameraAction(false, {}, warning = true)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CameraActionErrorPreview() {
    KaleyraTheme {
        Surface {
            CameraAction(false, {}, error = true)
        }
    }
}