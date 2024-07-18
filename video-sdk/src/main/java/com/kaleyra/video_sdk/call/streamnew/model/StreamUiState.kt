package com.kaleyra.video_sdk.call.streamnew.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * StreamUiState is a data class representing the UI state related to video streams.
 *
 * @property streams A list of all available StreamUi objects (representing individual video streams)
 * @property pinnedStreams A list of StreamUi objects that are currently pinned (prominently displayed)
 * @property fullscreenStream The StreamUi object that is currently in fullscreen mode (if any).
 * @constructor
 */
@Immutable
internal data class StreamUiState(
    val preview: StreamPreview? = null,
    val streams: ImmutableList<StreamUi> = ImmutableList(),
    val pinnedStreams: ImmutableList<StreamUi> = ImmutableList(),
    val fullscreenStream: StreamUi? = null
): UiState