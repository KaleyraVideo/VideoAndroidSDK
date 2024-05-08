package com.kaleyra.video_sdk.call.whiteboard.model

import androidx.compose.runtime.Stable

@Stable
sealed class WhiteboardRequest(open val username: String? = null) {

    data class Show(override val username: String? = null): WhiteboardRequest(username)

    data class Hide(override val username: String? = null): WhiteboardRequest(username)
}
