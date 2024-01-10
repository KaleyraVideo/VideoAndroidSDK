@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.participantspanel.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaleyra.video.conversation.Conversation
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.call.participantspanel.model.StreamArrangement
import com.kaleyra.video_sdk.call.participantspanel.viewmodel.ParticipantsPanelViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
internal fun ParticipantsPanelDialog(
    viewModel: ParticipantsPanelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ParticipantsPanelViewModel.provideFactory(configure = ::requestConfiguration)
    ), onDismiss: () -> Unit) {
    var shouldDismiss by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = {
        shouldDismiss = true
    }, properties = DialogProperties(
        usePlatformDefaultWidth = true,
        decorFitsSystemWindows = true)) {

        val participantPanelUiState by viewModel.uiState.collectAsState()

        ParticipantsPanelContent(
            inCallStreamUi = participantPanelUiState.inCallStreamUi,
            invitedParticipants = participantPanelUiState.invitedParticipants,
            streamArrangement = StreamArrangement.Pin,
            adminUserId = participantPanelUiState.adminUserId,
            isLoggedUserAdmin = participantPanelUiState.isLoggedUserAdmin,
            onGridClicked = { viewModel.updateStreamArrangement(StreamArrangement.Grid) },
            onPinClicked = { viewModel.updateStreamArrangement(StreamArrangement.Pin) },
            onPin = { streamUi, pinned ->
                if (pinned) viewModel.pin(streamUi = streamUi)
                else viewModel.unpin(streamUi = streamUi)
            },
            onMute = { streamUi, muted ->
                if (muted) viewModel.mute(streamUi = streamUi)
                else viewModel.unmute(streamUi = streamUi)
            },
            onClose = {
                shouldDismiss = true
                onDismiss()
            }
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantsPanelDialogPreview() = KaleyraTheme {
    ParticipantsPanelDialog(
        onDismiss = {}
    )
}