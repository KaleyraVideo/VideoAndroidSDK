package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Represents the UI state for displaying a collection of streams, including screen share status and automatic stream management.
 *
 * This class holds information about the active stream preview (if any), a list of
 * stream items to be displayed in the UI, whether the maximum number of pinned streams
 * has been reached, whether screen sharing is currently active, and whether the stream items are being managed automatically.
 *
 * @property preview The currently active stream preview, or `null` if no preview is active.
 * @property streamItems An immutable list of [StreamItem] objects representing the streams
 * to be displayed. Defaults to an empty list.
 * @property hasReachedMaxPinnedStreams `true` if the maximum number of pinned streams has
 * been reached, `false` otherwise.
 * @property isScreenShareActive `true` if screen sharing is currently active, `false` otherwise.
 * When in auto mode, the system may automatically populate, update, filter, or sort the `streamItems`.
 */
@Immutable
internal data class StreamUiState(
    val preview: StreamPreview? = null,
    val streamItems: ImmutableList<StreamItem> = ImmutableList(),
    val hasReachedMaxPinnedStreams: Boolean = false,
    val isScreenShareActive: Boolean = false,
) : UiState