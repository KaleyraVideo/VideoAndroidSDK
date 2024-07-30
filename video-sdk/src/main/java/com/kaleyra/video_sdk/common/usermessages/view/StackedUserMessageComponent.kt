package com.kaleyra.video_sdk.common.usermessages.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.snackbarm3.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.timer
import kotlin.random.Random

@Composable
internal fun StackedUserMessageComponent(
    modifier: Modifier = Modifier,
    viewModel: UserMessagesViewModel = viewModel(factory = UserMessagesViewModel.provideFactory(LocalAccessibilityManager.current, ::requestCollaborationViewModelConfiguration)),
    onActionClick: (UserMessage) -> Unit
) {
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(ImmutableList())
    val alertMessages by viewModel.uiState.collectAsStateWithLifecycle()

    StackedUserMessageComponent(
        modifier = modifier,
        userMessages = userMessage,
        alertMessages = alertMessages.alertMessages,
        onActionClick = onActionClick,
        onDismissClick = { viewModel.dismiss(it) }
    )
}

@MultiConfigPreview
@Composable
fun StackedUserMessageSnackbarHandlerPreview() = KaleyraM3Theme {
    val messages = listOf(
        RecordingMessage.Started,
        RecordingMessage.Stopped,
        RecordingMessage.Failed,
        UsbCameraMessage.Connected("Generic external camera"),
        UsbCameraMessage.Disconnected,
        UsbCameraMessage.NotSupported,
        CameraRestrictionMessage(),
        AudioConnectionFailureMessage.Generic,
        AudioConnectionFailureMessage.InSystemCall,
        PinScreenshareMessage("id","User")
    )

    val alerts = listOf(
        AlertMessage.WaitingForOtherParticipantsMessage,
        AlertMessage.LeftAloneMessage,
        AlertMessage.AutomaticRecordingMessage
    )

    val postMessage: MutableStateFlow<UserMessage> = MutableStateFlow(RecordingMessage.Started)
    val postAlerts: MutableStateFlow<List<AlertMessage>> = MutableStateFlow(listOf())

    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(key1 = "userMessages") {
        if (isPreview) return@LaunchedEffect
        timer(period = 2000) {
            scope.launch {
                postMessage.emit(messages.get(Random.nextInt(messages.size)))
            }
        }
    }

    LaunchedEffect(key1 = "alertMessages") {
        if (isPreview) return@LaunchedEffect
        timer(period = 2500) {
            scope.launch {
                postAlerts.emit(alerts.subList(0, Random.nextInt(alerts.size+1)))
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(MaterialTheme.colorScheme.surface)) {
        StackedUserMessageComponent(
            onActionClick = {})
    }
}