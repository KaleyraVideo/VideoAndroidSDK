package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Represents the UI state for displaying a collection of streams.
 *
 * This class holds information about the active stream preview (if any) and a list of
 * stream items to be displayed in the UI.
 *
 * @property preview The currently active stream preview, or `null` if no preview is active.
 * @property streamItems An immutable list of [StreamItem] objects representing the streams
 * to be displayed.
 */
@Immutable
internal data class StreamUiState(
    val preview: StreamPreview? = null,
    val streamItems: ImmutableList<StreamItem> = ImmutableList(),
): UiState