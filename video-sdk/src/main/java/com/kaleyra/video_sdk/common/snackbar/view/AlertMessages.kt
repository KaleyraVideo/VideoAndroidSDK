package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
fun AutomaticRecordingSnackbarM3() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_automatic_recording_disclaimer)
    )
}

@Composable
fun WaitingForOtherParticipantsSnackbarM3() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_call_waiting_for_other_participants)
    )
}

@Composable
fun LeftAloneSnackbarM3() {
    val resources = LocalContext.current.resources
    UserMessageInfoSnackbarM3(
        message = resources.getString(R.string.kaleyra_call_left_alone)
    )
}

@MultiConfigPreview
@Composable
internal fun AutomaticRecordingMessagePreviewM3() {
    KaleyraTheme {
        AutomaticRecordingSnackbarM3()
    }
}

@MultiConfigPreview
@Composable
internal fun WaitingForOtherParticipantsMessagePreviewM3() {
    KaleyraTheme {
        WaitingForOtherParticipantsSnackbarM3()
    }
}

@MultiConfigPreview
@Composable
internal fun LeftAloneMessagePreviewM3() {
    KaleyraTheme {
        LeftAloneSnackbarM3()
    }
}