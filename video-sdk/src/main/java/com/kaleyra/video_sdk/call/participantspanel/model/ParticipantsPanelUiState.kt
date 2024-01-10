package com.kaleyra.video_sdk.call.participantspanel.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Stable
internal sealed class StreamArrangement {
    data object Grid: StreamArrangement()
    data object Pin: StreamArrangement()
}

@Stable
internal data class ParticipantsPanelUiState(
    val invitedParticipants: ImmutableList<String> = ImmutableList(emptyList()),
    val inCallStreamUi: ImmutableList<StreamUi> = ImmutableList(emptyList()),
    val streamArrangement: StreamArrangement = StreamArrangement.Grid,
    val watermarkInfo: WatermarkInfo? = null,
    val adminUserId: String,
    val isLoggedUserAdmin: Boolean
) : UiState