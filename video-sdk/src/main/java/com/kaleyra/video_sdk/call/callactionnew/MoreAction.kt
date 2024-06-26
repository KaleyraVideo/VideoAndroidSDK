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
internal fun MoreAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    CallToggleAction(
        modifier = modifier,
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_more),
        contentDescription = stringResource(id = if (checked) R.string.kaleyra_call_sheet_description_hide_actions else R.string.kaleyra_call_sheet_description_more_actions),
        badgeText = badgeText,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MoreActionPreview() {
    KaleyraM3Theme {
        Surface {
            MoreAction(checked = false, {})
        }
    }
}