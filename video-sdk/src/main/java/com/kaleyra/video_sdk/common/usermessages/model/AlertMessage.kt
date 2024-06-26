package com.kaleyra.video_sdk.common.usermessages.model

import androidx.compose.runtime.Stable

/**
 * Alert Message
 * @property id String user message identifier
 * @property action UserMessage.Action optional action associated with user message
 */
@Stable
sealed class AlertMessage: UserMessage() {

    /**
     * AutomaticRecordingMessage representing that the call will be recorder as soon as it is connected
     */
    data object AutomaticRecordingMessage: AlertMessage()

    /**
     * WaitingForOtherParticipantsMessage representing the state in which the call is connected and other participant will join
     */
    data object WaitingForOtherParticipantsMessage: AlertMessage()

    /**
     * LeftAloneMessage representing the state in which the call is still connected but no other participants are in call
     */
    data object LeftAloneMessage: AlertMessage()
}

