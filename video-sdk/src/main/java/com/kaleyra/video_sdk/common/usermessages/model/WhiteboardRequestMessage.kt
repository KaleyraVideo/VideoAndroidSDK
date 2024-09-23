package com.kaleyra.video_sdk.common.usermessages.model

/**
 * A user message representing the request that an admin user sent to the client
 * to force open/close the Whiteboard
 * @property username? String the admin username
 * @property id String the user message id
 * @constructor
 */
sealed class WhiteboardRequestMessage(open val username: String? = null): UserMessage() {
    /**
     * A user message representing the request that an admin user sent to the client
     * to force open the Whiteboard
     * @constructor
     */
    data class WhiteboardShowRequestMessage(override val username: String?): WhiteboardRequestMessage(username)

    /**
     * A user message representing the request that an admin user sent to the client
     * to force close the Whiteboard
     * @constructor
     */
    data class WhiteboardHideRequestMessage(override val username: String?): WhiteboardRequestMessage(username)
}