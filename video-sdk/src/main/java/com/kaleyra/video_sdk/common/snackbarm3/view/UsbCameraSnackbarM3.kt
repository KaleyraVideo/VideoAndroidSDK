/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.common.snackbarm3.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun UsbConnectedSnackbarM3(
    externalCameraName: String = "",
    onDismissClick: ()-> Unit
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = if (externalCameraName.isBlank()) resources.getString(R.string.kaleyra_generic_external_camera_connected) else
            resources.getString(R.string.kaleyra_external_camera_connected, externalCameraName),
        onDismissClick = onDismissClick,
    )
}

@Composable
internal fun UsbDisconnectedSnackbarM3(
    onDismissClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(message = resources.getString(R.string.kaleyra_external_camera_disconnected), onDismissClick)
}

@Composable
internal fun UsbNotSupportedSnackbarM3(
    onDismissClick: ()-> Unit
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(message = resources.getString(R.string.kaleyra_external_camera_unsupported), onDismissClick)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbConnectedSnackbarPreviewM3() {
    KaleyraM3Theme {
        UsbConnectedSnackbarM3(externalCameraName = "name", {})
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbDisconnectedSnackbarPreviewM3() {
    KaleyraM3Theme {
        UsbDisconnectedSnackbarM3({})
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UsbNotSupportedSnackbarPreviewM3() {
    KaleyraM3Theme {
        UsbNotSupportedSnackbarM3({})
    }
}
