package com.kaleyra.video_sdk.call.screen.view.vcallscreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.FullScreenMessageText
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel

internal const val InputMessageDragHandleTag = "InputMessageDragHandleTag"

@Composable
internal fun InputMessageHandle(
    callActionsViewModel: CallActionsViewModel = viewModel(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    streamViewModel: StreamViewModel = viewModel(factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val showHandle by remember {
        derivedStateOf { snackbarHostState.currentSnackbarData == null }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (showHandle) {
            CallBottomSheetDefaults.HDragHandle(Modifier.testTag(InputMessageDragHandleTag))
        }
        InputMessageHost(
            callActionsViewModel = callActionsViewModel,
            streamViewModel = streamViewModel,
            snackbarHostState = snackbarHostState,
            micMessage = { enabled -> MicMessageText(enabled) },
            cameraMessage = { enabled -> CameraMessageText(enabled) },
            fullscreenMessage = { enabled -> FullScreenMessageText(enabled) },
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}