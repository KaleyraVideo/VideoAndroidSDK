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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.snackbarm3.view.AudioOutputGenericFailureSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.AudioOutputInSystemCallFailureSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.CameraRestrictionSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.MutedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.PinScreenshareSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingEndedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingErrorSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.RecordingStartedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.StackedSnackbarHost
import com.kaleyra.video_sdk.common.snackbarm3.view.UsbConnectedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.UsbDisconnectedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.UsbNotSupportedSnackbarM3
import com.kaleyra.video_sdk.common.snackbarm3.view.rememberStackedSnackbarHostState
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider.userMessage
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.concurrent.timer
import kotlin.random.Random

@Composable
internal fun StackedUserMessageSnackbarHandler(
    modifier: Modifier = Modifier,
    userMessage: Flow<UserMessage> = CallUserMessagesProvider.userMessage,
    alertMessages: Flow<List<AlertMessage>> = CallUserMessagesProvider.alertMessages,
    onActionCLick: (UserMessage.Action) -> Unit
) {
    val snackbarHostState = rememberStackedSnackbarHostState(LocalAccessibilityManager.current!!)
    val userMessages by userMessage.collectAsStateWithLifecycle(initialValue = null)
    val alertMessages by alertMessages.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(snackbarHostState) {
        snapshotFlow { userMessages }
            .filterNotNull()
            .buffer()
            .collect { userMessage -> snackbarHostState.addUserMessage(userMessage, true) }
    }

    LaunchedEffect(snackbarHostState) {
        snapshotFlow { alertMessages }
            .filterNotNull()
            .buffer()
            .collect { alertMessages -> snackbarHostState.addAlertMessages(alertMessages) }
    }

    StackedSnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState,
        onActionClick = onActionCLick
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
        PinScreenshareMessage("User")
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
        StackedUserMessageSnackbarHandler(
            userMessage = postMessage,
            alertMessages = postAlerts,
            onActionCLick = {})
    }
}