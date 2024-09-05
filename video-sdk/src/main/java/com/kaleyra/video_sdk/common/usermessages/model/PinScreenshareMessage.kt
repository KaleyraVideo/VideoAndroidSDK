package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Pin Screenshare Message
 * @property streamId String screenshare stream id
 * @property userDisplayName String user that shared its screen
 * @constructor
 */
data class PinScreenshareMessage(
    val streamId: String,
    val userDisplayName: String
) : UserMessage()