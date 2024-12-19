package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardRequestMessage

@Composable
internal fun StackedSnackbar(
    snackbarData: ImmutableList<UserMessage>,
    modifier: Modifier = Modifier,
    onDismissClick: (UserMessage) -> Unit,
    onActionClick: (UserMessage) -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.then(modifier)
    ) {
        items(items = snackbarData.value, key = { it.id }) {
            val userMessage = it
            val dismiss = { onDismissClick(userMessage) }
            when (userMessage) {
                is RecordingMessage.Started -> RecordingStartedSnackbarM3(onDismissClick = dismiss)
                is RecordingMessage.Stopped -> RecordingEndedSnackbarM3(onDismissClick = dismiss)
                is RecordingMessage.Failed -> RecordingErrorSnackbarM3(onDismissClick = dismiss)
                is UsbCameraMessage.Connected -> UsbConnectedSnackbarM3(userMessage.name, onDismissClick = dismiss)
                is UsbCameraMessage.Disconnected -> UsbDisconnectedSnackbarM3(onDismissClick = dismiss)
                is UsbCameraMessage.NotSupported -> UsbNotSupportedSnackbarM3(onDismissClick = dismiss)
                is CameraRestrictionMessage -> CameraRestrictionSnackbarM3(onDismissClick = dismiss)
                is AudioConnectionFailureMessage.Generic -> AudioOutputGenericFailureSnackbarM3(onDismissClick = dismiss)
                is AudioConnectionFailureMessage.InSystemCall -> AudioOutputInSystemCallFailureSnackbarM3(onDismissClick = dismiss)
                is MutedMessage -> MutedSnackbarM3(userMessage.admin)
                is PinScreenshareMessage -> PinScreenshareSnackbarM3(userDisplayName = userMessage.userDisplayName, onPinClicked = {
                    dismiss()
                    onActionClick(userMessage)
                })
                is WhiteboardRequestMessage.WhiteboardHideRequestMessage -> WhiteboardAdminCloseSnackbar(userMessage.username, onDismissClick = dismiss)
                is WhiteboardRequestMessage.WhiteboardShowRequestMessage -> WhiteboardAdminOpenSnackbar(userMessage.username, onDismissClick = dismiss)

                AlertMessage.AutomaticRecordingMessage -> AutomaticRecordingSnackbarM3()
                AlertMessage.LeftAloneMessage -> LeftAloneSnackbarM3()
                AlertMessage.WaitingForOtherParticipantsMessage -> WaitingForOtherParticipantsSnackbarM3()
                is AlertMessage.CustomMessage -> CustomMessageSnackbarM3(it as AlertMessage.CustomMessage)
                else -> {}
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
