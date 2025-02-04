package com.kaleyra.video_sdk.call.participants.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
internal data class ParticipantsUiState(
    val invitedParticipants: ImmutableList<String> = ImmutableList(),
    val adminsStreamIds: ImmutableList<String> = ImmutableList(),
    val pinnedStreamIds: ImmutableList<String> = ImmutableList(),
    val isLocalParticipantAdmin: Boolean = false,
    val participantCount: Int = 0,
    val hasReachedMaxPinnedStreams: Boolean = false,
    val streamsLayout: StreamsLayout = StreamsLayout.Auto,
    val streams: ImmutableList<StreamUi> = ImmutableList()
): UiState