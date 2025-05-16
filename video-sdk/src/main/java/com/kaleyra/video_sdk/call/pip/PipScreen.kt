package com.kaleyra.video_sdk.call.pip

import android.content.Intent
import android.util.Rational
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaleyra.video_common_ui.notification.signature.SignDocumentViewVisibilityObserver
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.pip.view.PipRecordingComponent
import com.kaleyra.video_sdk.call.pip.view.PipStreamComponent

@Composable
internal fun PipScreen(
    onPipAspectRatio: (Rational) -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    if (!isTesting) {
        DisposableEffect(context) {
            context.sendBroadcast(Intent(context, CallUiPipVisibilityObserver::class.java).apply {
                action = CallUiPipVisibilityObserver.ACTION_CALL_UI_PIP_DISPLAYED
            })
            onDispose {
                context.sendBroadcast(Intent(context, CallUiPipVisibilityObserver::class.java).apply {
                    action = CallUiPipVisibilityObserver.ACTION_CALL_UI_PIP_NOT_DISPLAYED
                })
            }
        }
    }

    Box(modifier) {
        PipStreamComponent(
            onPipAspectRatio = onPipAspectRatio
        )

        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            PipRecordingComponent(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = CallAppBarViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
                )
            )
            CallInfoComponent(isPipMode = true)
        }
    }
}