package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Represent a message related to a signature document
 *
 * This sealed class defines following messages:
 * - [New]: Indicates that a new document to sign is available
 */
sealed class SignatureMessage : UserMessage() {

    abstract val signId: String

    /**
     * A message that indicates that a new document to sign is available
     */
    data class New(override val signId: String) : SignatureMessage()
}
