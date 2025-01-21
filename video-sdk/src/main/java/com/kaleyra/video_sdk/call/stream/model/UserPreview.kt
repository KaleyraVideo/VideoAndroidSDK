package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

@Immutable
data class UserPreview(
    val username: String,
    val avatar: ImmutableUri?,
)