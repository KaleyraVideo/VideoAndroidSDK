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

val AnswerActionMultiplier = 2
val AnswerActionExtendedMultiplier = 3

val AnswerActionWidth = CallActionDefaults.MinButtonSize * AnswerActionMultiplier + SheetItemsSpacing
val AnswerActionExtendedWidth = CallActionDefaults.MinButtonSize * AnswerActionExtendedMultiplier + SheetItemsSpacing * (AnswerActionExtendedMultiplier - 1)
//+ SheetItemsSpacing * (AnswerActionExtendedMultiplier - 1)

@Composable
internal fun AnswerAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extended: Boolean = false
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_answer)
    CallAction(
        modifier = if (extended) modifier.width(AnswerActionExtendedWidth) else modifier.width(
            AnswerActionWidth
        ),
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_answer),
        contentDescription = text,
        buttonText = text,
        buttonColor = KaleyraTheme.colors.positiveContainer,
        buttonContentColor = KaleyraTheme.colors.onPositiveContainer,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AnswerActionPreview() {
    KaleyraTheme {
        AnswerAction({})
    }
}