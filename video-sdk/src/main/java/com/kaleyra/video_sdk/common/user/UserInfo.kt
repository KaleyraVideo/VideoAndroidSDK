package com.kaleyra.video_sdk.common.user

import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

data class UserInfo(
    val userId: String,
    val username: String,
    val image: ImmutableUri
)