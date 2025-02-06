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
internal fun MoreAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_more),
        contentDescription = stringResource(id = if (checked) R.string.kaleyra_call_sheet_description_hide_actions else R.string.kaleyra_call_sheet_description_more_actions),
        badgeCount = badgeCount,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MoreActionPreview() {
    KaleyraTheme {
        Surface {
            MoreAction(checked = false, {})
        }
    }
}