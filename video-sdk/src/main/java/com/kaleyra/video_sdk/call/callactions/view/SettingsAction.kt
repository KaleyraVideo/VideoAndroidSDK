package com.kaleyra.video_sdk.call.callactions.view

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SettingsAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val icon = painterResource(R.drawable.ic_kaleyra_settings)
    val text = stringResource(id = R.string.kaleyra_strings_action_settings)
    CallAction(
        modifier = modifier,
        icon = icon,
        contentDescription = text,
        enabled = enabled,
        buttonText = text,
        label = if (label) text else null,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SettingsActionPreview() {
    KaleyraTheme {
        Surface {
            SettingsAction({})
        }
    }
}