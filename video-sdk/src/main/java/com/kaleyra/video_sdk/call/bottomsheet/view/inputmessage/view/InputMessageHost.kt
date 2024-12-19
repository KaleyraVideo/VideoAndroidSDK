package com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.usermessages.model.CameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.FullScreenMessage
import com.kaleyra.video_sdk.common.usermessages.model.MicMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

internal const val InputMessageDuration = 1000L

@Composable
internal fun InputMessageHost(
    modifier: Modifier = Modifier,
    callActionsViewModel: CallActionsViewModel = viewModel(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    streamViewModel: StreamViewModel = viewModel(factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    micMessage: @Composable (enabled: Boolean) -> Unit = { MicMessage(it) },
    cameraMessage: @Composable (enabled: Boolean) -> Unit = { CameraMessage(it) },
    fullscreenMessage: @Composable (enabled: Boolean) -> Unit = { FullScreenMessage(it) }
) {
    val inputMessage = merge(callActionsViewModel.userMessage, streamViewModel.userMessage).collectAsStateWithLifecycle(initialValue = null)
    InputMessageHost(
        modifier = modifier,
        inputMessage = inputMessage.value,
        snackbarHostState = snackbarHostState,
        micMessage = micMessage,
        cameraMessage = cameraMessage,
        fullscreenMessage = fullscreenMessage
    )
}

@Composable
internal fun InputMessageHost(
    modifier: Modifier = Modifier,
    inputMessage: UserMessage?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    micMessage: @Composable (enabled: Boolean) -> Unit = { MicMessage(it) },
    cameraMessage: @Composable (enabled: Boolean) -> Unit = { CameraMessage(it) },
    fullscreenMessage: @Composable (enabled: Boolean) -> Unit = { FullScreenMessage(it) }
) {
    val updatedState by rememberUpdatedState(inputMessage)
    LaunchedEffect(Unit) {
        snapshotFlow { updatedState }
            .filterNotNull()
            .collectLatest {
                snackbarHostState.currentSnackbarData?.dismiss()
                launch { snackbarHostState.showSnackbar(message = it.javaClass.name) }
                delay(InputMessageDuration)
                snackbarHostState.currentSnackbarData?.dismiss()
            }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        snackbar = { data ->
            when (data.visuals.message) {
                MicMessage.Enabled.javaClass.name -> micMessage(true)
                MicMessage.Disabled.javaClass.name -> micMessage(false)
                CameraMessage.Enabled.javaClass.name -> cameraMessage(true)
                CameraMessage.Disabled.javaClass.name -> cameraMessage(false)
                FullScreenMessage.Enabled.javaClass.name -> fullscreenMessage(true)
                FullScreenMessage.Disabled.javaClass.name -> fullscreenMessage(false)
            }
        }
    )
}