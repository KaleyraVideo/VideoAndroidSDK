package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState

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
    data class More(
        override val id: String = "moreItemId",
        val users: List<UserPreview>,
    ) : StreamItem
}