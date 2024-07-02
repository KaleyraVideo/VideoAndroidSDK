package com.kaleyra.video_sdk.common.usermessages.model

import androidx.compose.runtime.Stable
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

@Stable
data class StackedSnackbarUiState(val alertMessages: ImmutableList<AlertMessage> = ImmutableList()) : UiState