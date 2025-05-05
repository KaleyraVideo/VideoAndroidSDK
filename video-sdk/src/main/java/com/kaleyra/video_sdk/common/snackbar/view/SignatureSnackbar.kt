package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme


@Composable
fun NewSignatureSnackbar(
    onSignClicked: () -> Unit,
) {
    UserMessageSnackbarM3(
        message = stringResource(R.string.kaleyra_signature_new_practice_available),
        actionConfig = UserMessageSnackbarActionConfig(
            action = TextRef.PlainText(stringResource(R.string.kaleyra_signature_sign)),
            iconResource = R.drawable.ic_kaleyra_signature,
            onActionClick = onSignClicked
        )
    )
}

@Composable
@MultiConfigPreview
fun NewSignatureSnackbarPreview() = KaleyraTheme {
    NewSignatureSnackbar {}
}