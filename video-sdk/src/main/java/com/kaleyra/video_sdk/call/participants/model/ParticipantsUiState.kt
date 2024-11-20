package com.kaleyra.video_sdk.call.participants.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
internal data class ParticipantsUiState(
    val invitedParticipants: ImmutableList<String> = ImmutableList(),
    val adminsStreamsIds: ImmutableList<String> = ImmutableList(),
    val isLocalParticipantAdmin: Boolean = false,
    val participantCount: Int = 0
): UiState