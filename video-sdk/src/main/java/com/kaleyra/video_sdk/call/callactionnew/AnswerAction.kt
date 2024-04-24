package com.kaleyra.video_sdk.call.callactionnew

import android.content.res.Configuration
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.SheetContentItemSpacing
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import com.kaleyra.video_sdk.theme.KaleyraTheme

val AnswerButtonWidth = CallActionDefaults.minButtonSize * 2 + SheetContentItemSpacing
val AnswerButtonExtendedWidth = CallActionDefaults.minButtonSize * 3 + SheetContentItemSpacing * 2

@Composable
internal fun AnswerAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extended: Boolean = false
) {
    val text = stringResource(id = R.string.kaleyra_call_sheet_answer)
    CallAction(
        modifier = if (extended) modifier.width(AnswerButtonExtendedWidth) else modifier.width(AnswerButtonWidth),
        icon = painterResource(id = R.drawable.ic_kaleyra_call_sheet_answer),
        contentDescription = text,
        buttonText = text,
        buttonColor = KaleyraTheme.colors.answer,
        buttonContentColor = KaleyraTheme.colors.onAnswer,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AnswerActionPreview() {
    KaleyraM3Theme {
        AnswerAction({})
    }
}