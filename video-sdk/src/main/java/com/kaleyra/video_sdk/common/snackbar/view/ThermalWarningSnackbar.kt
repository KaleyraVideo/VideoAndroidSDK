package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef

@Composable
fun ThermalWarningSnackbar(
    onDismissClick: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_strings_info_call_thermal_warning_message),
        onDismissClick = onDismissClick,
        actionConfig = UserMessageSnackbarActionConfig(
            action = TextRef.StringResource(R.string.kaleyra_strings_action_settings),
            iconResource = R.drawable.ic_kaleyra_settings,
            onActionClick = onSettingsClicked
        )
    )
}