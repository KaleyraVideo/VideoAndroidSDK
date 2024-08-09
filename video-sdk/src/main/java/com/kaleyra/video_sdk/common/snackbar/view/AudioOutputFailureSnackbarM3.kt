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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun AudioOutputGenericFailureSnackbarM3(
    onDismissClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_generic_audio_routing_error),
        onDismissClick = onDismissClick
    )
}

@Composable
internal fun AudioOutputInSystemCallFailureSnackbarM3(
    onDismissClick: () -> Unit,
) {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_already_in_system_call_audio_routing_error),
        onDismissClick = onDismissClick,
    )
}

@MultiConfigPreview
@Composable
internal fun AudioOutputGenericFailureSnackbarPreviewM3() {
    KaleyraTheme {
        AudioOutputGenericFailureSnackbarM3({})
    }
}

@MultiConfigPreview
@Composable
internal fun AudioOutputInSystemCallFailureSnackbarPreviewM3() {
    KaleyraTheme {
        AudioOutputInSystemCallFailureSnackbarM3({})
    }
}
