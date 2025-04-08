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
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.call.signature.view.SignDocumentView
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
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
    onDismiss: () -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit = { },
    isLargeScreen: Boolean = false,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    val signDocumentsUiState by signDocumentsViewModel.uiState.collectAsStateWithLifecycle()

    if (!isTesting) {
        DisposableEffect(context) {
            context.sendBroadcast(Intent(context, SignDocumentViewVisibilityObserver::class.java).apply {
                action = SignDocumentViewVisibilityObserver.ACTION_SIGN_VIEW_DISPLAYED
            })
            onDispose {
                context.sendBroadcast(Intent(context, SignDocumentViewVisibilityObserver::class.java).apply {
                    action = SignDocumentViewVisibilityObserver.ACTION_SIGN_VIEW_NOT_DISPLAYED
                })
            }
        }
    }

    var hasCompletedSign by remember { mutableStateOf(false) }

    val signingDocumentUi = signDocumentsUiState.ongoingSignDocumentUi

    when {
        !hasCompletedSign && signingDocumentUi == null -> hasCompletedSign = false
        !hasCompletedSign && signingDocumentUi != null -> hasCompletedSign = true
        hasCompletedSign && signingDocumentUi == null -> {
            hasCompletedSign = false
            onDismiss()
        }
    }

    if (signingDocumentUi != null || isTesting) {

        DisposableEffect(Unit) {
            onDispose {
                signingDocumentUi?.let { signDocumentsViewModel.cancelSign(signDocumentUi = it) }
                onDismiss()
            }
        }


        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Column(Modifier.fillMaxSize()) {
                SignDocumentsAppBar(
                    onBackPressed = {
                        signingDocumentUi?.let {
                            signDocumentsViewModel.cancelSign(signDocumentUi = it)
                        }
                        onDismiss()
                    },
                    isLargeScreen = isLargeScreen,
                    lazyGridState = rememberLazyGridState()
                )

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (signingDocumentUi?.signView != null) SignDocumentView(
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
}

@Composable
@MultiConfigPreview
internal fun SignDocumentViewComponentPreview() {
    KaleyraTheme {
        SignDocumentViewComponent(
            onDismiss = {},
            onUserMessageActionClick = {},
            isLargeScreen = false,
            isTesting = true
        )
    }
}