package com.kaleyra.video_sdk.call.stream.layoutsystem.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

@Stable
internal sealed interface StreamItem {

    val id: String

    @Immutable
    data class Stream(
        override val id: String,
        val stream: StreamUi,
        val state: StreamItemState = StreamItemState.Standard,
    ) : StreamItem

    @Immutable
    data class MoreStreams(
        override val id: String = "moreItemId",
        val users: List<MoreStreamsUserPreview>,
    ) : StreamItem

    fun isFeatured(): Boolean {
        return (this as? Stream)?.state is StreamItemState.Featured
    }

    fun isPinned(): Boolean {
        return (this as? Stream)?.state == StreamItemState.Featured.Pinned
    }

    fun isFullscreen(): Boolean {
        return (this as? Stream)?.state == StreamItemState.Featured.Fullscreen
    }

    fun hasVideoEnabled(): Boolean {
        return (this as? Stream)?.stream?.video?.isEnabled == true
    }
}