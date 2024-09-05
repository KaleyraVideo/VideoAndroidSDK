package com.kaleyra.video_sdk.common.snackbarlegacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R

@Composable
internal fun WhiteboardShowRequestSnackbar(userAdminDisplayName: String? = null) {
    UserMessageInfoSnackbar(
        title = stringResource(id = R.string.kaleyra_whiteboard),
        subtitle = userAdminDisplayName?.let { adminDisplayName ->
            stringResource(R.string.kaleyra_whiteboard_show_request_by_admin_user_message).format(adminDisplayName)
        } ?: stringResource(R.string.kaleyra_whiteboard_show_request_user_message
        )
    )
}

@Composable
internal fun WhiteboardHideRequestSnackbar(userAdminDisplayName: String? = null) {
    UserMessageInfoSnackbar(
        title = stringResource(R.string.kaleyra_whiteboard),
        subtitle = userAdminDisplayName?.let { adminDisplayName ->
            stringResource(R.string.kaleyra_whiteboard_hide_request_by_admin_user_message).format(adminDisplayName)
        } ?: stringResource(R.string.kaleyra_whiteboard_hide_request_user_message
        )
    )
}