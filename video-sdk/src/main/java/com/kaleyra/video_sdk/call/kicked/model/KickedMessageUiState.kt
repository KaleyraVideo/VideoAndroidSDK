package com.kaleyra.video_sdk.call.kicked.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
sealed class KickedMessageUiState: UiState {

    @Immutable
    data object Hidden: KickedMessageUiState()

    @Immutable
    data class Display(val adminName: String? = null): KickedMessageUiState()
}