package com.kaleyra.video_sdk.call.stream.utils

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi

/**
 * Checks if the [StreamUi] has any video content associated with it.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream has a non-null [VideoUi] object, `false` otherwise.
 */
internal fun StreamUi.hasVideo(): Boolean = video != null

/**
 * Checks if the [StreamUi] has any video content associated with it and is enabled.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream has an enabled [VideoUi] object, `false` otherwise.
 */
internal fun StreamUi.isVideoEnabled(): Boolean = video != null && video.isEnabled


/**
 * Checks if the [StreamUi] has audio level greater than 0f.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream has an [AudioUi] object with a level greater than 0f, `false` otherwise.
 */
internal fun StreamUi.isAudioLevelAboveZero(): Boolean = (audio?.level ?: 0f) > 0f


/**
 * Checks if the [StreamUi] represents a remote user's screen share.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream is not owned by the current user and the video is a screen share; `false` otherwise.
 */
internal fun StreamUi.isRemoteScreenShare(): Boolean = !isMine && video != null && video.isScreenShare

/**
 * Checks if the [StreamUi] represents the current user's screen share.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream is owned by the current user and the video is a screen share; `false` otherwise.
 */
internal fun StreamUi.isLocalScreenShare(): Boolean = isMine && video != null && video.isScreenShare

/**
 * Checks if the [StreamUi] represents the current user's camera stream.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream is owned by the current user and the video is not a screen share; `false` otherwise.
 */
internal fun StreamUi.isMyCameraStream(): Boolean = isMine && video != null && !video.isScreenShare

/**
 * Checks if the [StreamUi] represents a remote user's camera stream.
 *
 * @receiver The [StreamUi] instance to check.
 * @return `true` if the stream is not owned by the current user and the video is not a screen share; `false` otherwise.
 */
internal fun StreamUi.isRemoteCameraStream(): Boolean =
    !isMine && video != null && !video.isScreenShare