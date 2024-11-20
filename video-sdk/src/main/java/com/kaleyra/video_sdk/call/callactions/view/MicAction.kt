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

@Composable
internal fun MicAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: Boolean = false,
    enabled: Boolean = true,
    warning: Boolean = false,
    error: Boolean = false,
) {
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = if (checked) R.drawable.ic_kaleyra_call_sheet_enable_microphone else R.drawable.ic_kaleyra_call_sheet_disable_microphone),
        contentDescription = stringResource(id = if (checked) R.string.kaleyra_call_sheet_description_enable_microphone else R.string.kaleyra_call_sheet_description_disable_microphone),
        checked = checked,
        onCheckedChange = onCheckedChange,
        label = if (label) stringResource(R.string.kaleyra_call_sheet_microphone) else null,
        enabled = enabled,
        badgePainter = when {
            warning -> painterResource(R.drawable.ic_kaleyra_call_sheet_warning)
            error -> painterResource(R.drawable.ic_kaleyra_call_sheet_error)
            else -> null
        },
        badgeDescription =  when {
            warning -> stringResource(R.string.kaleyra_call_sheet_description_mic_warning)
            error -> stringResource(R.string.kaleyra_call_sheet_description_mic_error)
            else -> null
        },
        badgeBackgroundColor = if (warning) KaleyraTheme.colors.warning else MaterialTheme.colorScheme.error,
        badgeContentColor = if (warning) KaleyraTheme.colors.onWarning else MaterialTheme.colorScheme.onError
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MicActionPreview() {
    KaleyraTheme {
        Surface {
            MicAction(false, {})
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MicActionWarningPreview() {
    KaleyraTheme {
        Surface {
            MicAction(false, {}, warning = true)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MicActionErrorPreview() {
    KaleyraTheme {
        Surface {
            MicAction(false, {}, error = true)
        }
    }
}