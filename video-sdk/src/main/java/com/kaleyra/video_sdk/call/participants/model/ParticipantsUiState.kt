package com.kaleyra.video_sdk.call.participants.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Represents the UI state of the participants in a call or video conference.
 *
 * This data class encapsulates all the information needed to display and manage the
 * participants in the user interface, including their roles, the streams they are
 * associated with, and the overall layout of the streams.
 *
 * @property invitedParticipants A list of user IDs representing participants who have been
 *                              invited to the call but may not have joined yet.
 * @property adminsStreamIds A list of stream IDs that belong to participants with admin
 *                          privileges.
 * @property pinnedStreamIds A list of stream IDs that are currently pinned in the layout.
 *                           Pinned streams are typically displayed more prominently.
 * @property isLocalParticipantAdmin Indicates whether the local participant (the current user)
 *                                 has admin privileges.
 * @property joinedParticipantCount The total number of participants who joined in the call.
 * @property hasReachedMaxPinnedStreams Indicates whether the maximum number of streams that
 *                                     can be pinned has been reached.
 * @property streamsLayout The current layout mode for the streams, such as automatic or
 *                         manual.
 * @property streams A list of `StreamUi` objects representing the streams of the participants.
 *                  This list contains all the streams that are currently active in the call.
 */
@Immutable
internal data class ParticipantsUiState(
    val invitedParticipants: ImmutableList<String> = ImmutableList(),
    val adminsStreamIds: ImmutableList<String> = ImmutableList(),
    val pinnedStreamIds: ImmutableList<String> = ImmutableList(),
    val isLocalParticipantAdmin: Boolean = false,
    val joinedParticipantCount: Int = 0,
    val hasReachedMaxPinnedStreams: Boolean = false,
    val streamsLayout: StreamsLayout = StreamsLayout.Auto,
    val streams: ImmutableList<StreamUi> = ImmutableList()
): UiState