package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo

@Immutable
internal data class StreamPreview(
    val video: VideoUi? = null,
    val audio: AudioUi? = null,
    val userInfos: ImmutableList<UserInfo> = ImmutableList(),
    val isStartingWithVideo: Boolean = false
)