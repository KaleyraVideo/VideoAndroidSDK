package com.kaleyra.video_sdk.call.stream.utils

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal fun StreamUi.isRemoteScreenShare(): Boolean = !isMine && video?.isScreenShare == true

internal fun StreamUi.isLocalScreenShare(): Boolean = isMine && video?.isScreenShare == true

internal fun StreamUi.isMyCameraStream(): Boolean = isMine && video != null && !video.isScreenShare

internal fun StreamUi.isRemoteCameraStream(): Boolean =
    !isMine && video != null && !video.isScreenShare