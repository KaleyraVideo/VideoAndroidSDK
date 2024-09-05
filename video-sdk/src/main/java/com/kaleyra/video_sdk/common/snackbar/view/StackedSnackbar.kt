package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StackedSnackbar(
    snackbarData: ImmutableList<UserMessage>,
    modifier: Modifier = Modifier,
    onDismissClick: (UserMessage) -> Unit,
    onActionClick: (UserMessage) -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier)) {

        items(items = snackbarData.value, key = { it.hashCode() }) {
            Box(
                modifier = Modifier
                    .animateItemPlacement()
                    .wrapContentSize(),
                content = {
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

                        AlertMessage.AutomaticRecordingMessage -> AutomaticRecordingSnackbarM3()
                        AlertMessage.LeftAloneMessage -> LeftAloneSnackbarM3()
                        AlertMessage.WaitingForOtherParticipantsMessage -> WaitingForOtherParticipantsSnackbarM3()
                        is WhiteboardHideRequestMessage -> TODO()
                        is WhiteboardShowRequestMessage -> TODO()
                    }
                }
            )
        }
    }
}

