package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun PinScreenshareSnackbarM3(
    userDisplayName: String,
    onPinClicked: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_stream_screenshare_received, userDisplayName),
        onDismissClick = {},
        actionConfig = UserMessageSnackbarActionConfig(action = TextRef.StringResource(R.string.kaleyra_user_message_pin), iconResource = R.drawable.ic_kaleyra_stream_pin, onActionClick = onPinClicked)
    )
}

@MultiConfigPreview
@Composable
internal fun PinScreenshareSnackbarPreviewM3() {
    KaleyraTheme {
        PinScreenshareSnackbarM3("admin", {})
    }
}
