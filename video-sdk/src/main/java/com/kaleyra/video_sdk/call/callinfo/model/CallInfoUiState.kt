package com.kaleyra.video_sdk.call.callinfo.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
data class CallInfoUiState(val callStateUi: CallStateUi? = null, val displayNames: ImmutableList<String> = ImmutableList(), val displayState: TextRef? = null): UiState

