package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme


@Composable
fun NewFileDownloadSnackbar(
    sender: String,
    onDownloadClicked: () -> Unit,
) {
    UserMessageSnackbarM3(
        message = stringResource(
            com.kaleyra.video_common_ui.R.string.kaleyra_signature_notification_user_sending_file,
            sender
        ),
        actionConfig = UserMessageSnackbarActionConfig(
            action = TextRef.PlainText(stringResource(R.string.kaleyra_strings_action_download)),
            iconResource = R.drawable.ic_kaleyra_download_snackbar,
            onActionClick = onDownloadClicked
        )
    )
}