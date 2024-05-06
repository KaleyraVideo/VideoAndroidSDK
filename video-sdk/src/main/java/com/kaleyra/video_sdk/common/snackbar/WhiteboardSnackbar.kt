package com.kaleyra.video_sdk.common.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaleyra.video_sdk.R

@Composable
internal fun WhiteboardShowRequestSnackbar(userAdminDisplayName: String? = null) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = resources.getString(R.string.kaleyra_whiteboard),
        subtitle = userAdminDisplayName?.let { adminDisplayName ->
            resources.getString(R.string.kaleyra_whiteboard_show_request_by_admin_user_message).format(adminDisplayName)
        } ?: resources.getString(R.string.kaleyra_whiteboard_show_request_user_message
        )
    )
}

@Composable
internal fun WhiteboardHideRequestSnackbar(userAdminDisplayName: String? = null) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbar(
        title = resources.getString(R.string.kaleyra_whiteboard),
        subtitle = userAdminDisplayName?.let { adminDisplayName ->
            resources.getString(R.string.kaleyra_whiteboard_hide_request_by_admin_user_message).format(adminDisplayName)
        } ?: resources.getString(R.string.kaleyra_whiteboard_hide_request_user_message
        )
    )
}