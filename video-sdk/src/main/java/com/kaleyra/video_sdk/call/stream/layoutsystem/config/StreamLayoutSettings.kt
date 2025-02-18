package com.kaleyra.video_sdk.call.stream.layoutsystem.config

/**
 * `StreamLayoutSettings` defines the settings that influence the behavior of a stream layout.
 *
 * This data class holds various configuration options that can affect how streams are
 * displayed and managed within a layout.
 *
 * @property isGroupCall Indicates whether the stream layout is being used in a group call scenario.
 *                       This setting might affect how streams are prioritized or displayed,
 *                       as group calls often have different layout requirements than one-on-one calls.
 *                       Defaults to `false`.
 * @property defaultCameraIsBack Indicates whether the default camera should be the back-facing camera.
 *                               This setting can be used to determine the initial camera selection
 *                               of the local user.
 *                               Defaults to `false`, meaning the front-facing camera is the default.
 */
data class StreamLayoutSettings(
    val isGroupCall: Boolean = false,
    val defaultCameraIsBack: Boolean = false
)