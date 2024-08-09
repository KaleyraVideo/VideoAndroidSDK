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
internal fun PinAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pin: Boolean = false,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val text = if (pin) stringResource(id = R.string.kaleyra_call_sheet_unpin) else stringResource(id = R.string.kaleyra_call_sheet_pin)
    CallAction(
        modifier = modifier,
        icon = if (pin) painterResource(id = R.drawable.ic_kaleyra_call_sheet_unpin) else painterResource(id = R.drawable.ic_kaleyra_call_sheet_pin),
        contentDescription = text,
        enabled = enabled,
        label = if (label) text else null,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PinActionPreview() {
    KaleyraTheme {
        Surface {
            PinAction(onClick = {})
        }
    }
}