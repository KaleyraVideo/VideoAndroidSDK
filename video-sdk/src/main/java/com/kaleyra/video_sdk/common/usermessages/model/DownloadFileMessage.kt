package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Represent a message related to a new downloadable file
 *
 * This sealed class defines following messages:
 * - [New]: Indicates that a new file is ready to be downloaded
 */
sealed class DownloadFileMessage() : UserMessage() {

    abstract val downloadId: String
    abstract val sender: String

    /**
     * A message that indicates that a new file is ready to be downloaded
     * @property downloadId The downloadable file identifier
     * @property sender The sender that sent the downloadable file
     */
    data class New(override val downloadId: String, override val sender: String) : DownloadFileMessage()
}
