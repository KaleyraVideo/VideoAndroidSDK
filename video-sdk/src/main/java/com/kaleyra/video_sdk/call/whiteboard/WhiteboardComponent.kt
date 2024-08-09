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
package com.kaleyra.video_sdk.call.whiteboard

import android.content.res.Configuration
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardAppBar
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardContent
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardOfflineContent
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.theme.KaleyraTheme

private val WhiteboardBackgroundColor = Color(0xFFF5F5F5)

@Composable
internal fun WhiteboardComponent(
    modifier: Modifier = Modifier,
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestCollaborationViewModelConfiguration, WhiteboardView(LocalContext.current))
    ),
    onDismiss: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.uploadMediaFile(uri)
    }

    WhiteboardComponent(
        modifier = modifier,
        uiState = uiState,
        onWhiteboardClosed = viewModel::onWhiteboardClosed,
        onReloadClick = viewModel::onReloadClick,
        onBackPressed = onDismiss,
        onUploadClick = { launcher.launch("image/*") },
        onUserMessageActionClick = onUserMessageActionClick
    )
}

@Composable
internal fun WhiteboardComponent(
    modifier: Modifier = Modifier,
    uiState: WhiteboardUiState,
    onWhiteboardClosed: () -> Unit,
    onReloadClick: () -> Unit,
    onBackPressed: () -> Unit,
    onUploadClick: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit
) {
    DisposableEffect(Unit) {
        onDispose(onWhiteboardClosed)
    }

    Surface {
        Column {
            WhiteboardAppBar(
                isFileSharingSupported = uiState.isFileSharingSupported && !uiState.isOffline && !uiState.isLoading,
                onBackPressed = onBackPressed,
                onUploadClick = { onUploadClick() },
                modifier = modifier
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(color = WhiteboardBackgroundColor)
            ) {
                when {
                    uiState.isOffline -> {
                        WhiteboardOfflineContent(
                            loading = uiState.isLoading,
                            onReloadClick = onReloadClick
                        )
                    }

                    uiState.whiteboardView != null -> {
                        WhiteboardContent(
                            whiteboardView = uiState.whiteboardView,
                            loading = uiState.isLoading,
                            upload = uiState.upload
                        )
                    }
                }

                StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
            }

            NavigationBarsSpacer()
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardComponentPreview() {
    KaleyraTheme {
        WhiteboardComponentPreview(
            uiState = WhiteboardUiState(
                whiteboardView = View(LocalContext.current),
                isLoading = true,
                upload = WhiteboardUploadUi.Uploading(.7f)
            )
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardScreenOfflinePreview() {
    WhiteboardComponentPreview(uiState = WhiteboardUiState(
        whiteboardView = View(LocalContext.current),
        isOffline = true)
    )
}

@Composable
private fun WhiteboardComponentPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardContent(
                whiteboardView = uiState.whiteboardView!!,
                loading = uiState.isLoading,
                upload = uiState.upload,
                modifier = Modifier
            )
        }
    }
}