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
package com.kaleyra.video_sdk.call.signature

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.notification.signature.SignDocumentsVisibilityObserver
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.fileshare.model.mockSignDocumentFile
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsContent
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsEmptyContent
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsEmptySearchContent
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SignDocumentsComponent(
    modifier: Modifier = Modifier,
    viewModel: SignDocumentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SignDocumentsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onSignDocumentSelected: (SignDocumentUi?) -> Unit,
    onDismiss: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit = { },
    isLargeScreen: Boolean = false,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!isTesting) {
        DisposableEffect(context) {
            context.sendBroadcast(Intent(context, SignDocumentsVisibilityObserver::class.java).apply {
                action = SignDocumentsVisibilityObserver.ACTION_SIGN_DOCUMENTS_DISPLAYED
            })
            onDispose {
                context.sendBroadcast(Intent(context, SignDocumentsVisibilityObserver::class.java).apply {
                    action = SignDocumentsVisibilityObserver.ACTION_SIGN_DOCUMENTS_NOT_DISPLAYED
                })
            }
        }
    }

    val userMessagesViewModel: UserMessagesViewModel = viewModel(factory = UserMessagesViewModel. provideFactory(LocalAccessibilityManager. current, ::requestCollaborationViewModelConfiguration))

    SignDocumentsComponent(
        modifier = modifier,
        uiState = uiState,
        userMessageComponent = {
            StackedUserMessageComponent(viewModel = userMessagesViewModel, onActionClick = onUserMessageActionClick)
        },
        onBackPressed = { onDismiss() },
        isLargeScreen = isLargeScreen,
        onItemClick = { clickedSignDocument ->
            if (clickedSignDocument.signState != SignDocumentUi.SignStateUi.Completed) {
                viewModel.signDocument(clickedSignDocument)
                onSignDocumentSelected(clickedSignDocument)
            }
        },
        lazyGridState = rememberLazyGridState(),
    )

    LaunchedEffect(Unit) {
        userMessagesViewModel.dismissMessages()
    }
}

@Composable
internal fun SignDocumentsComponent(
    modifier: Modifier = Modifier,
    uiState: SignDocumentUiState,
    userMessageComponent: @Composable () -> Unit = { },
    onItemClick: (SignDocumentUi) -> Unit,
    onBackPressed: () -> Unit,
    isLargeScreen: Boolean = false,
    lazyGridState: LazyGridState = rememberLazyGridState(),
) {
    DisposableEffect(Unit) {
        onDispose(onBackPressed)
    }

    var searchTerms by remember { mutableStateOf("") }

    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column(modifier) {
            SignDocumentsAppBar(
                onBackPressed = onBackPressed,
                isLargeScreen = isLargeScreen,
                lazyGridState = lazyGridState,
                enableSearch = true,
                onSearch = { text ->
                    searchTerms = text
                }
            )

            var filteredSignDocuments = uiState.signDocuments.value
            if (searchTerms.isNotEmpty()) {
                filteredSignDocuments = filteredSignDocuments.filter { it.name.contains(searchTerms, ignoreCase = true) || it.sender.contains(searchTerms, ignoreCase = true) }
            }
            filteredSignDocuments = filteredSignDocuments.sortedByDescending { it.creationTime }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when {

                    filteredSignDocuments.isEmpty() && searchTerms.isNotEmpty() -> {
                        SignDocumentsEmptySearchContent()
                    }

                    filteredSignDocuments.isEmpty() -> {
                        SignDocumentsEmptyContent()
                    }

                    else -> {
                        SignDocumentsContent(
                            items = ImmutableList(filteredSignDocuments),
                            onItemClick = onItemClick,
                            lazyGridState = lazyGridState,
                        )
                    }
                }

                if (!isLargeScreen) {
                    userMessageComponent()
                }
            }

            NavigationBarsSpacer()
        }
    }
}

@Composable
@MultiConfigPreview
internal fun SignDocumentComponentPreview() {
    KaleyraTheme {
        SignDocumentComponentPreview(
            uiState = SignDocumentUiState(
                signDocuments = ImmutableList(listOf(mockSignDocumentFile, mockSignDocumentFile.copy(id = "id2"), mockSignDocumentFile.copy(id = "id3"))),
            )
        )
    }
}

@Composable
@MultiConfigPreview
internal fun SignDocumentEmptyComponentPreview() {
    KaleyraTheme {
        SignDocumentComponentPreview(
            uiState = SignDocumentUiState(
                signDocuments = ImmutableList(listOf()),
            )
        )
    }
}



@Composable
private fun SignDocumentComponentPreview(uiState: SignDocumentUiState) {
    KaleyraTheme {
        Surface {
            SignDocumentsComponent(
                uiState = uiState,
                userMessageComponent = {},
                onBackPressed = {},
                isLargeScreen = false,
                onItemClick = {},
                lazyGridState = rememberLazyGridState()
            )
        }
    }
}