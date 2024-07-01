package com.kaleyra.video_sdk.call.whiteboard.model

import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
sealed class WhiteboardRequest(open val id: String, open val username: String? = null) {

    data class Show(
        override val username: String? = null,
        override val id: String = UUID.randomUUID().toString()
    ) : WhiteboardRequest(id, username)

    data class Hide(
        override val username: String? = null,
        override val id: String = UUID.randomUUID().toString()
    ) : WhiteboardRequest(id, username)
}
