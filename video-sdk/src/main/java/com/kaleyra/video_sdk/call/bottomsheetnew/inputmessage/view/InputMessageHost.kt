package com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view

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
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.viewmodel.InputMessageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal const val InputMessageDuration = 1000L

@Composable
internal fun InputMessageHost(
    modifier: Modifier = Modifier,
    inputMessageViewModel: InputMessageViewModel = viewModel(factory = InputMessageViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    micMessage: @Composable (enabled: Boolean) -> Unit = { MicMessage(it) },
    cameraMessage: @Composable (enabled: Boolean) -> Unit = { CameraMessage(it) }
) {
    val inputMessage = inputMessageViewModel.inputMessage.collectAsStateWithLifecycle(initialValue = null)
    InputMessageHost(
        modifier = modifier,
        inputMessage = inputMessage.value,
        snackbarHostState = snackbarHostState,
        micMessage = micMessage,
        cameraMessage = cameraMessage)
}

@Composable
internal fun InputMessageHost(
    modifier: Modifier = Modifier,
    inputMessage: InputMessage?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    micMessage: @Composable (enabled: Boolean) -> Unit = { MicMessage(it) },
    cameraMessage: @Composable (enabled: Boolean) -> Unit = { CameraMessage(it) }
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
            }
        }
    )
}