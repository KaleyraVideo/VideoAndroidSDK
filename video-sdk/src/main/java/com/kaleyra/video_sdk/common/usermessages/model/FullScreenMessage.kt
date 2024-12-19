package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Represents a message related to enabling or disabling full-screen mode.
 *
 * This sealed class defines two possible messages:
 * - [Enabled]: Indicates that full-screen mode should be enabled.
 * - [Disabled]: Indicates that full-screen mode should be disabled.
 */
sealed class FullScreenMessage : UserMessage() {

    /**
     * A message indicating that full-screen mode should be enabled.
     */
    data object Enabled : MicMessage()

    /**
     * A message indicating that full-screen mode should be disabled.
     */
    data object Disabled : MicMessage()
}