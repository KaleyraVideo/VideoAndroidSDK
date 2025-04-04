package com.kaleyra.video_sdk.call.appbar.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
data class CallAppBarUiState(
    val title: String? = null,
    val appIconUri: Uri = Uri.EMPTY,
    val automaticRecording: Boolean = false,
    val recordingStateUi: RecordingStateUi = RecordingStateUi.Stopped,
    val callStateUi: CallStateUi = CallStateUi.Disconnected,
    val participantCount: Int = 0): UiState
