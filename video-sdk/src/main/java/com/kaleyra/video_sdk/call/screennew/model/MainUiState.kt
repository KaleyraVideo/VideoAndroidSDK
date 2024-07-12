package com.kaleyra.video_sdk.call.screennew.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
data class MainUiState(val isCallEnded: Boolean = false) : UiState