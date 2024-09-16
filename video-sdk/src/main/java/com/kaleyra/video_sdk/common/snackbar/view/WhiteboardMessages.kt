package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R

@Composable
internal fun WhiteboardAdminOpenSnackbar(
    userName: String?,
    onDismissClick: () -> Unit,
) {
    UserMessageInfoSnackbarM3(
        message = userName?.let {
            stringResource(R.string.kaleyra_whiteboard_show_request_by_admin_user_message).format(userName)
        } ?: stringResource(R.string.kaleyra_whiteboard_show_request_user_message),
        onDismissClick = onDismissClick
    )
}

@Composable
internal fun WhiteboardAdminCloseSnackbar(
    userName: String?,
    onDismissClick: () -> Unit,
) {
    UserMessageInfoSnackbarM3(
        message = userName?.let {
            stringResource(R.string.kaleyra_whiteboard_hide_request_by_admin_user_message).format(userName)
        } ?: stringResource(R.string.kaleyra_whiteboard_hide_request_user_message),
        onDismissClick = onDismissClick
    )
}