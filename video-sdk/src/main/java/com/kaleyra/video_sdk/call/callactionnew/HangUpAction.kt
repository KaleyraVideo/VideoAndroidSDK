package com.kaleyra.video_sdk.call.callactionnew

import android.content.res.Configuration
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import com.kaleyra.video_sdk.theme.KaleyraTheme

val HangUpActionExtendedMultiplier = 2

val HangUpActionWidth = CallActionDefaults.MinButtonSize
val HangUpActionExtendedWidth = CallActionDefaults.MinButtonSize * HangUpActionExtendedMultiplier
//+ SheetItemsSpacing

@Composable
internal fun HangUpAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    extended: Boolean = false
) {
    CallAction(
        modifier = if (extended) modifier.width(HangUpActionExtendedWidth) else modifier.width(HangUpActionWidth),
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_hang_up),
        contentDescription = stringResource(id = R.string.kaleyra_call_sheet_hang_up),
        buttonColor = KaleyraTheme.colors.hangUp,
        buttonContentColor = KaleyraTheme.colors.onHangUp,
        disabledButtonColor = KaleyraTheme.colors.hangUp.copy(.38f),
        disabledButtonContentColor = KaleyraTheme.colors.onHangUp.copy(.38f),
        enabled = enabled,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HangUpActionPreview() {
    KaleyraM3Theme {
        HangUpAction({})
    }
}