package com.kaleyra.video_sdk.common.usermessages.model

import java.util.UUID

/**
 * A user message representing the request that an admin user sent to the client
 * to force display the Whiteboard
 * @property adminUserId String? the admin user id
 * @property id String the user message id
 * @constructor
 */
data class WhiteboardShowRequestMessage(
    val adminUserId: String? = null,
    override val id: String = UUID.randomUUID().toString()
): UserMessage