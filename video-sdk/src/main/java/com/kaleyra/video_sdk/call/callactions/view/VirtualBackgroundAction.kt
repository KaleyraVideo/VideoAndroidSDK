package com.kaleyra.video_sdk.call.callactions.view

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun VirtualBackgroundAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: Boolean = false
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_virtual_background)
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_virtual_background),
        enabled = enabled,
        contentDescription = text,
        buttonText = text,
        label = if (label) text else null,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun VirtualBackgroundActionPreview() {
    KaleyraM3Theme {
        Surface {
            VirtualBackgroundAction(false, {})
        }
    }
}