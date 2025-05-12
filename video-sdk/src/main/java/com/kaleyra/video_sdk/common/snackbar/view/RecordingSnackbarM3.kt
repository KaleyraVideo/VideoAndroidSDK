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

package com.kaleyra.video_sdk.common.snackbar.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
fun RecordingStartedSnackbarM3(
    onDismissClick: () -> Unit
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_recording_started_message),
        onDismissClick = onDismissClick,
    )
}

@Composable
fun RecordingEndedSnackbarM3(
    onDismissClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_recording_stopped_message),
        onDismissClick = onDismissClick,
    )
}

@Composable
fun RecordingErrorSnackbarM3(
    onDismissClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageErrorSnackbarM3(
        message = resources.getString(R.string.kaleyra_strings_info_recording_failed),
        onDismissClick = onDismissClick
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingStartedSnackbarPreviewM3() {
    KaleyraTheme {
        RecordingStartedSnackbarM3(onDismissClick =  {})
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingStoppedSnackbarPreviewM3() {
    KaleyraTheme {
        RecordingEndedSnackbarM3(onDismissClick = {})
    }
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun RecordingErrorSnackbarPreviewM3() {
    KaleyraTheme {
        RecordingErrorSnackbarM3({})
    }
}
