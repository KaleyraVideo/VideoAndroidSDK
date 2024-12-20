package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Represents a message related to enabling or disabling the microphone.
 *
 * This sealed class defines two possible messages:
 * - [Enabled]: Indicates that the microphone should be enabled.
 * - [Disabled]: Indicates that the microphone should be disabled.
 */
sealed class MicMessage : UserMessage() {

    /**
     * A message indicating that the microphone should be enabled.
     */
    data object Enabled : MicMessage()

    /**
     * A message indicating that the microphone should be disabled.
     */
    data object Disabled : MicMessage()
}