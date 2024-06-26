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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorState
import com.kaleyra.video_sdk.call.whiteboard.view.TextEditorValue
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardContent
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardModalBottomSheetContent
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardOfflineContent
import com.kaleyra.video_sdk.call.whiteboard.view.rememberTextEditorState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardComponent(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestCollaborationViewModelConfiguration, WhiteboardView(LocalContext.current))
    ),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(initialValue = null)

    WhiteboardComponent(
        uiState = uiState,
        editorSheetState = sheetState,
        textEditorState = textEditorState,
        userMessage = userMessage,
        onReloadClick = viewModel::onReloadClick,
        onTextDismissed = viewModel::onTextDismissed,
        onTextConfirmed = viewModel::onTextConfirmed,
        onWhiteboardClosed = viewModel::onWhiteboardClosed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardComponent(
    uiState: WhiteboardUiState,
    editorSheetState: ModalBottomSheetState,
    textEditorState: TextEditorState,
    userMessage: UserMessage? = null,
    onReloadClick: () -> Unit,
    onTextDismissed: () -> Unit,
    onTextConfirmed: (String) -> Unit,
    onWhiteboardClosed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowTextEditor by rememberUpdatedState(newValue = uiState.text != null)
    LaunchedEffect(shouldShowTextEditor, editorSheetState) {
        if (shouldShowTextEditor) {
            textEditorState.type(TextFieldValue(uiState.text ?: ""))
            editorSheetState.show()
        } else {
            editorSheetState.hide()
            textEditorState.clearState()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onWhiteboardClosed()
        }
    }

    when {
        textEditorState.currentValue != TextEditorValue.Empty -> BackHandler(onBack = { if (textEditorState.cancel()) onTextDismissed() })
        editorSheetState.currentValue != ModalBottomSheetValue.Hidden -> BackHandler(onBack = onTextDismissed)
    }

    ModalBottomSheetLayout(
        sheetState = editorSheetState,
        modifier = modifier.statusBarsPadding(),
        sheetContent = {
            WhiteboardModalBottomSheetContent(
                textEditorState = textEditorState,
                onTextDismissed = onTextDismissed,
                onTextConfirmed = onTextConfirmed,
                modifier = Modifier
                    .navigationBarsPadding()
                    // Disable gestures on the modal bottom sheet
                    .pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
            )
        },
        content = {
            Box {
                Column {
                    val contentModifier = Modifier
                        .weight(1f)
                        .background(color = colorResource(id = R.color.kaleyra_color_loading_whiteboard_background))
                    when {
                        uiState.isOffline -> {
                            WhiteboardOfflineContent(
                                loading = uiState.isLoading,
                                onReloadClick = onReloadClick,
                                modifier = contentModifier
                            )
                        }

                        uiState.whiteboardView != null -> {
                            WhiteboardContent(
                                whiteboardView = uiState.whiteboardView,
                                loading = uiState.isLoading,
                                upload = uiState.upload,
                                modifier = contentModifier
                            )
                        }
                    }

                    NavigationBarsSpacer()
                }

                UserMessageSnackbarHandler(userMessage = userMessage)
            }
        },
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardComponentPreview() {
    WhiteboardComponentPreview(
        uiState = WhiteboardUiState(
            isLoading = true,
            upload = WhiteboardUploadUi.Uploading(.7f)
        )
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardScreenOfflinePreview() {
    WhiteboardComponentPreview(uiState = WhiteboardUiState(isOffline = true))
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WhiteboardComponentPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardComponent(
                uiState = uiState,
                editorSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
                textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty),
                onReloadClick = {},
                onTextDismissed = {},
                onTextConfirmed = {},
                onWhiteboardClosed = {}
            )
        }
    }
}