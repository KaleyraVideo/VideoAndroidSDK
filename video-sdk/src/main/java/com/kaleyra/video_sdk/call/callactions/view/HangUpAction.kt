package com.kaleyra.video_sdk.call.callactions.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.theme.KaleyraTheme

val HangUpActionMultiplier = 1
val HangUpActionExtendedMultiplier = 2

val HangUpActionWidth = CallActionDefaults.MinButtonSize
val HangUpActionExtendedWidth = CallActionDefaults.MinButtonSize * HangUpActionExtendedMultiplier + SheetItemsSpacing * (HangUpActionExtendedMultiplier - 1)
//+ SheetItemsSpacing

@Composable
internal fun HangUpAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    extended: Boolean = false,
    label: Boolean = false
) {
    CallAction(
        modifier = if (extended) modifier.width(HangUpActionExtendedWidth) else modifier.width(
            HangUpActionWidth
        ),
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_hang_up),
        contentDescription = stringResource(id = R.string.kaleyra_call_sheet_hang_up),
        buttonColor = KaleyraTheme.colors.negativeContainer,
        buttonContentColor = KaleyraTheme.colors.onNegativeContainer,
        disabledButtonColor = KaleyraTheme.colors.negativeContainer.copy(alpha = 0.38f),
        disabledButtonContentColor = KaleyraTheme.colors.onNegativeContainer.copy(alpha = 0.38f),
        enabled = enabled,
        label = if (label) stringResource(R.string.kaleyra_call_sheet_hang_up_action) else null,
        onClick = onClick,
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HangUpActionPreview() {
    KaleyraTheme {
        HangUpAction(label = true, onClick = {})
    }
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun DisabledHangUpActionPreview() {
    KaleyraTheme {
        HangUpAction(label = true, onClick = {}, enabled = false)
    }
}