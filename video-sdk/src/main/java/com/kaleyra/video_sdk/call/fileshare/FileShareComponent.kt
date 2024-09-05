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

package com.kaleyra.video_sdk.call.fileshare

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.moveToFront
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickActivity
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickBroadcastReceiver
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.video_sdk.call.fileshare.view.FileShareAppBar
import com.kaleyra.video_sdk.call.fileshare.view.FileShareContent
import com.kaleyra.video_sdk.call.fileshare.view.FileShareEmptyContent
import com.kaleyra.video_sdk.call.fileshare.view.FileShareFab
import com.kaleyra.video_sdk.call.fileshare.view.MaxFileSizeDialog
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme
import java.util.UUID

/**
 * Progress Indicator Tag
 */
const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun FileShareComponent(
    modifier: Modifier = Modifier,
    viewModel: FileShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FileShareViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration, filePickProvider = FilePickBroadcastReceiver)
    ),
    onDismiss: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val (showUnableToOpenFileSnackBar, setShowUnableToOpenSnackBar) = remember { mutableStateOf(false) }
    val (showCancelledFileSnackBar, setShowCancelledFileSnackBar) = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!isTesting) {
        DisposableEffect(context) {
            context.sendBroadcast(Intent(context, FileShareVisibilityObserver::class.java).apply {
                action = FileShareVisibilityObserver.ACTION_FILE_SHARE_DISPLAYED
            })
            onDispose {
                context.sendBroadcast(Intent(context, FileShareVisibilityObserver::class.java).apply {
                    action = FileShareVisibilityObserver.ACTION_FILE_SHARE_NOT_DISPLAYED
                })
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setOnFileSelected {
            activity.moveToFront()
        }
    }

    FileShareComponent(
        uiState = uiState,
        showUnableToOpenFileSnackBar = showUnableToOpenFileSnackBar,
        showCancelledFileSnackBar = showCancelledFileSnackBar,
        onFileOpenFailure = { doesFileExists ->
            if (doesFileExists) setShowUnableToOpenSnackBar(true)
            else setShowCancelledFileSnackBar(true)
        },
        onSnackBarShowed = {
            setShowUnableToOpenSnackBar(false)
            setShowCancelledFileSnackBar(false)
        },
        onAlertDialogDismiss = viewModel::dismissUploadLimit,
        onUpload = viewModel::upload,
        onDownload = viewModel::download,
        onShareCancel = viewModel::cancel,
        modifier = modifier,
        onUserMessageActionClick = onUserMessageActionClick,
        onBackPressed = onDismiss
    )
}

@Composable
internal fun FileShareComponent(
    uiState: FileShareUiState,
    showUnableToOpenFileSnackBar: Boolean = false,
    showCancelledFileSnackBar: Boolean = false,
    onFileOpenFailure: ((doesFileExists: Boolean) -> Unit)? = null,
    onSnackBarShowed: (() -> Unit)? = null,
    onAlertDialogDismiss: (() -> Unit)? = null,
    onBackPressed: (() -> Unit)? = null,
    onUserMessageActionClick: (UserMessage) -> Unit,
    onUpload: (Uri) -> Unit,
    onDownload: (String) -> Unit,
    onShareCancel: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current

    val showFilePicker = remember { { FilePickActivity.show(context) } }

    val unableToOpenFileText = stringResource(id = R.string.kaleyra_fileshare_impossible_open_file)
    val fileCancelledText = stringResource(id = R.string.kaleyra_fileshare_file_cancelled)

    val onItemClick = remember {
        { file: SharedFileUi ->
            if (file.state is SharedFileUi.State.Success) {
                val uri = if (file.isMine) file.uri.value else file.state.uri.value
                context.tryToOpenFile(uri) { onFileOpenFailure?.invoke(it) }
            }
        }
    }

    if (showUnableToOpenFileSnackBar) {
        LaunchedEffect(unableToOpenFileText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = unableToOpenFileText)
            onSnackBarShowed?.invoke()
        }
    }

    if (showCancelledFileSnackBar) {
        LaunchedEffect(fileCancelledText, snackBarHostState) {
            snackBarHostState.showSnackbar(message = fileCancelledText)
            onSnackBarShowed?.invoke()
        }
    }

    if (uiState.showFileSizeLimit) {
        MaxFileSizeDialog(onDismiss = { onAlertDialogDismiss?.invoke() })
    }

    val lazyGridState = rememberLazyGridState()

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        FileShareAppBar(onBackPressed = { onBackPressed?.invoke() }, lazyGridState = lazyGridState)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.sharedFiles.count() < 1) {
                FileShareEmptyContent(modifier = Modifier.matchParentSize())
            } else {
                FileShareContent(
                    modifier = Modifier.matchParentSize(),
                    items = uiState.sharedFiles,
                    onItemClick = onItemClick,
                    onItemActionClick = {
                        when (it.state) {
                            SharedFileUi.State.Available -> onDownload(it.id)
                            SharedFileUi.State.Pending, is SharedFileUi.State.InProgress -> onShareCancel(it.id)
                            is SharedFileUi.State.Success -> onItemClick(it)
                            SharedFileUi.State.Error -> if (it.isMine) onUpload(it.uri.value) else onDownload(it.id)
                            else -> Unit
                        }
                    },
                    lazyGridState = lazyGridState
                )
            }

            StackedUserMessageComponent(
                onActionClick = onUserMessageActionClick
            )

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            )

            FileShareFab(
                collapsed = uiState.sharedFiles.count() > 0,
                onClick = showFilePicker,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareComponentPreview() {
    KaleyraTheme {
        Surface {
            FileShareComponent(
                uiState = FileShareUiState(
                    sharedFiles = ImmutableList((0..20).map { mockUploadSharedFile.copy(id = UUID.randomUUID().toString()) })
                ),
                onUpload = {},
                onUserMessageActionClick = {},
                onDownload = {},
                onShareCancel = {}
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareComponentEmptyPreview() {
    KaleyraTheme {
        Surface {
            FileShareComponent(
                uiState = FileShareUiState(),
                onUpload = {},
                onUserMessageActionClick = {},
                onDownload = {},
                onShareCancel = {}
            )
        }
    }
}
