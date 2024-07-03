package com.kaleyra.video_sdk.call.appbar.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
data class CallAppBarUiState(val title: String? = null, val logo: Logo = Logo(), val recording: Boolean = false, val participantCount: Int = 0): UiState
