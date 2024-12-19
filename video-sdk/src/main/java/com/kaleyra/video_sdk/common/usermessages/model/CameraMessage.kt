package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Represents a message related to enabling or disabling the camera.
 *
 * This sealed class defines two possible messages:
 * - [Enabled]: Indicates that the camera should be enabled.
 * - [Disabled]: Indicates that the camera should be disabled.
 */
sealed class CameraMessage : UserMessage() {

    /**
     * A message indicating that the camera should be enabled.
     */
    data object Enabled : CameraMessage()

    /**
     * A message indicating that the camera should be disabled.
     */
    data object Disabled : CameraMessage()
}