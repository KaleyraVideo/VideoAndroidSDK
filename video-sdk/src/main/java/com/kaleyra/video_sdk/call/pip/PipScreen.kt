package com.kaleyra.video_sdk.call.pip

import android.util.Rational
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.pip.view.PipRecordingComponent
import com.kaleyra.video_sdk.call.pip.view.PipStreamComponent

@Composable
internal fun PipScreen(
    onPipAspectRatio: (Rational) -> Unit,
    modifier: Modifier = Modifier
) {
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