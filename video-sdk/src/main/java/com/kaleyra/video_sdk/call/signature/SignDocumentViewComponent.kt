package com.kaleyra.video_sdk.call.signature

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.notification.signature.SignDocumentViewVisibilityObserver
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.signature.view.SignDocumentView
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SignDocumentViewComponent(
    signDocumentsViewModel: SignDocumentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SignDocumentsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onBackPressed: () -> Unit,
    onDocumentSigned: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit = { },
    isLargeScreen: Boolean = false,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    val signDocumentsUiState by signDocumentsViewModel.uiState.collectAsStateWithLifecycle()

    if (!isTesting && signDocumentsUiState.ongoingSignDocumentUi != null) {
        DisposableEffect(context) {
            context.sendBroadcast(Intent(context, SignDocumentViewVisibilityObserver::class.java).apply {
                putExtra(SignDocumentViewVisibilityObserver.SIGN_DOCUMENT_ID_DISPLAYED, signDocumentsUiState.ongoingSignDocumentUi!!.id)
                action = SignDocumentViewVisibilityObserver.ACTION_SIGN_VIEW_DISPLAYED
            })
            onDispose {
                context.sendBroadcast(Intent(context, SignDocumentViewVisibilityObserver::class.java).apply {
                    action = SignDocumentViewVisibilityObserver.ACTION_SIGN_VIEW_NOT_DISPLAYED
                })
            }
        }
    }

    var isDisplayingSignView by remember { mutableStateOf(false) }

    val signingDocumentUi = signDocumentsUiState.ongoingSignDocumentUi

    when {
        !isDisplayingSignView && signingDocumentUi == null -> isDisplayingSignView = false
        !isDisplayingSignView && signingDocumentUi != null -> isDisplayingSignView = true
        isDisplayingSignView && signingDocumentUi == null -> {
            isDisplayingSignView = false
            onDocumentSigned()
        }
    }

    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column(Modifier.fillMaxSize()) {
            SignDocumentsAppBar(
                onBackPressed = {
                    signingDocumentUi?.let {
                        signDocumentsViewModel.cancelSign(signDocumentUi = it)
                    }
                    onBackPressed()
                },
                isLargeScreen = isLargeScreen,
                lazyGridState = rememberLazyGridState(),
                enableSearch = false,
                onSearch = {},
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (signingDocumentUi?.signView != null && !isTesting) SignDocumentView(
                    signView = signingDocumentUi.signView.value
                )

                if (!isLargeScreen) {
                    StackedUserMessageComponent(onActionClick = onUserMessageActionClick)
                }
            }

            NavigationBarsSpacer()
        }
    }
}

@Composable
@MultiConfigPreview
internal fun SignDocumentViewComponentPreview() {
    KaleyraTheme {
        SignDocumentViewComponent(
            onBackPressed = {},
            onUserMessageActionClick = {},
            onDocumentSigned = {},
            isLargeScreen = false,
            isTesting = true
        )
    }
}