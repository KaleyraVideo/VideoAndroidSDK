package com.kaleyra.video_sdk.call.stream.layoutsystem.config

/**
 * `StreamLayoutConstraints` defines the constraints for a stream layout.
 *
 * This data class holds the thresholds that determine how streams are displayed
 * in different layout modes, such as mosaic, featured, and thumbnail.
 *
 * @property mosaicStreamThreshold The maximum number of streams to display in a mosaic layout.
 *                                 If the number of available streams exceeds this threshold,
 *                                 a "MoreStreams" item might be displayed to indicate the
 *                                 presence of additional streams. A value of 0 means no streams
 *                                 will be displayed in mosaic mode.
 * @property featuredStreamThreshold The maximum number of streams that can be featured (e.g., pinned).
 *                                   If more streams are pinned than this threshold, some might
 *                                   not be displayed as featured. A value of 0 means no streams
 *                                   can be pinned.
 * @property thumbnailStreamThreshold The maximum number of non-featured streams to display when
 *                                    there are featured streams. This is used to limit the number
 *                                    of thumbnail streams shown alongside pinned streams. A value of 0
 *                                    means no thumbnail streams will be displayed.
 */
data class StreamLayoutConstraints(
    val mosaicStreamThreshold: Int = 0,
    val featuredStreamThreshold: Int = 0,
    val thumbnailStreamThreshold: Int = 0
)