package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

@Immutable
internal data class StreamPreview(
    val isGroupCall: Boolean = false,
    val video: VideoUi? = null,
    val username: String? = null,
    val avatar: ImmutableUri? = null
)