package com.kaleyra.video_sdk.common.usermessages.model

import java.util.UUID

/**
 * A user message representing the request that an admin user sent to the client
 * to force display the Whiteboard
 * @property username String? the admin username
 * @property id String the user message id
 * @constructor
 */
data class WhiteboardShowRequestMessage(
    val username: String? = null,
    override val id: String = UUID.randomUUID().toString()
): UserMessage