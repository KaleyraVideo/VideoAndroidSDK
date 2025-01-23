package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Represents the UI state for displaying a collection of streams.
 *
 * This class holds information about the active stream preview (if any), a list of
 * stream items to be displayed in the UI, and whether the maximum number of pinned streams
 * has been reached.
 *
 * @property preview The currently active stream preview, or `null` if no preview is active.
 * @property streamItems An immutable list of [StreamItem] objects representing the streams
 * to be displayed.
 * @property hasReachedMaxPinnedStreams `true` if the maximum number of pinned streams has
 * been reached, `false` otherwise.
 */
@Immutable
internal data class StreamUiState(
    val preview: StreamPreview? = null,
    val streamItems: ImmutableList<StreamItem> = ImmutableList(),
    val hasReachedMaxPinnedStreams: Boolean = false
): UiState