package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
fun ThermalWarningSnackbar(
    onDismissClick: (() -> Unit)? = null,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_strings_info_call_thermal_warning_message),
        iconTint = KaleyraTheme.colors.warning,
        onDismissClick = onDismissClick
    )
}

@MultiConfigPreview
@Composable
fun ThermalWarningSnackbarPreview() = KaleyraTheme {

    Surface {
        Column {
            ThermalWarningSnackbar {}
            Spacer(Modifier.size(8.dp))
            ThermalWarningSnackbar()
        }
    }
}