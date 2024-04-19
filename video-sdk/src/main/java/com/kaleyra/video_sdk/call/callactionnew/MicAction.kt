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
internal fun MicAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = if (checked) R.drawable.ic_kaleyra_call_sheet_enable_microphone else R.drawable.ic_kaleyra_call_sheet_disable_microphone),
        contentDescription = stringResource(id = if (checked) R.string.kaleyra_call_sheet_description_enable_microphone else R.string.kaleyra_call_sheet_description_disable_microphone),
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MicActionPreview() {
    KaleyraM3Theme {
        Surface {
            MicAction(false, {})
        }
    }
}