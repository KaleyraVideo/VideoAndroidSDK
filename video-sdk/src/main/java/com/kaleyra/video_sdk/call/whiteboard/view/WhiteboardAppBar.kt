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

package com.kaleyra.video_sdk.call.whiteboard.view

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun WhiteboardAppBar(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestCollaborationViewModelConfiguration, WhiteboardView(LocalContext.current))
    ),
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.uploadMediaFile(uri)
    }

    WhiteboardAppBar(
        isFileSharingSupported = uiState.isFileSharingSupported && !uiState.isLoading && !uiState.isOffline,
        onBackPressed = onBackPressed,
        onUploadClick = { launcher.launch("image/*") },
        modifier = modifier
    )
}

@Composable
internal fun WhiteboardAppBar(
    isFileSharingSupported: Boolean,
    onBackPressed: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComponentAppBar(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_whiteboard),
        actions = {
            if (isFileSharingSupported) {
                androidx.compose.material3.IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = onUploadClick,
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_image),
                        contentDescription = stringResource(id = R.string.kaleyra_upload_file))
                }
            } else {
                Spacer(Modifier.width(56.dp))
            }
        },
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardAppBarTest() {
    KaleyraM3Theme {
        WhiteboardAppBar(
            isFileSharingSupported = true,
            onBackPressed = {},
            onUploadClick = {}
        )
    }
}
