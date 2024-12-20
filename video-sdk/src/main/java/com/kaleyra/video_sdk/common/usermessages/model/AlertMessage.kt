package com.kaleyra.video_sdk.common.usermessages.model

import androidx.compose.runtime.Stable
import java.lang.ref.WeakReference

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

    /**
     * Custom message representing a message that can be customized in its own display body and clickable action
     * @property body String message body
     * @property button Button
     * @constructor
     */
    data class CustomMessage(
        val body: String,
        val button: Button? = null,
    ): AlertMessage() {

        /**
         * Class representing the custom message action
         * @property text String? optional button text
         * @property icon Int? optional button icon
         * @property action (Function0<Unit>)? optional button action
         * @constructor
         */
        data class Button(val text: String? = null, val icon: Int? = null, val action: (() -> Unit)?)
    }
}
