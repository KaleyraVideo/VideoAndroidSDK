package com.kaleyra.video_sdk.call.appbar.view

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.button.BackIconButton
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.extensions.ModifierExtensions.pulse
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Stable
internal object CallAppBarDefaults {

    val Height = 40.dp

}

internal val RecordingDotTag = "RecordingDotTag"

@Composable
internal fun CallAppBarComponent(
    modifier: Modifier = Modifier,
    viewModel: CallAppBarViewModel = viewModel(factory = CallAppBarViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CallAppBarComponent(
        modifier = modifier,
        title = uiState.title,
        appIcon = uiState.appIconUri,
        automaticRecording = uiState.automaticRecording,
        recordingStateUi = uiState.recordingStateUi,
        callStateUi = uiState.callStateUi,
        participantCount = uiState.participantCount,
        onParticipantClick = onParticipantClick,
        onBackPressed = onBackPressed)
}

@Composable
internal fun CallAppBarComponent(
    modifier: Modifier = Modifier,
    title: String?,
    appIcon: Uri,
    automaticRecording: Boolean,
    recordingStateUi: RecordingStateUi,
    callStateUi: CallStateUi,
    participantCount: Int,
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    var hasConnectedOnce by remember { mutableStateOf(false) }
    if (callStateUi is CallStateUi.Connected) hasConnectedOnce = true

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier
            .fillMaxWidth()
            .height(CallAppBarDefaults.Height)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = {
                when {
                    (callStateUi is CallStateUi.Disconnected && hasConnectedOnce)
                        || callStateUi is CallStateUi.Disconnecting ->
                            return@Box

                    automaticRecording
                        && recordingStateUi == RecordingStateUi.Stopped ->
                            Title(
                                recording = automaticRecording ||
                                    recordingStateUi == RecordingStateUi.Started,
                                pulseRecording = false,
                                title = stringResource(id = R.string.kaleyra_rec)
                            )

                    else -> Title(
                        recording = automaticRecording ||
                            recordingStateUi == RecordingStateUi.Started,
                        pulseRecording = recordingStateUi == RecordingStateUi.Started,
                        title = title ?: automaticRecording.takeIf { it }?.let { stringResource(id = R.string.kaleyra_rec) } ?: "")
                }
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackIconButton(
                    modifier = Modifier.size(40.dp),
                    iconTint = LocalContentColor.current,
                    highlightShape = RoundedCornerShape(50.dp, 0.dp, 0.dp, 50.dp),
                    onClick = onBackPressed
                )
                AsyncImage(
                    model = appIcon,
                    contentDescription = stringResource(id = R.string.kaleyra_app_icon),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(22.dp)
                )
            }

            CallParticipantsButton(
                participantCount = participantCount,
                onClick = onParticipantClick
            )
        }
    }
}

@Composable
private fun Title(recording: Boolean, pulseRecording: Boolean, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(visible = recording) {
            Icon(
                painterResource(id = R.drawable.ic_kaleyra_recording_dot_new),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(18.dp)
                    .pulse(enabled = pulseRecording)
                    .testTag(RecordingDotTag)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun CallParticipantsButton(
    participantCount: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    androidx.compose.material3.IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .highlightOnFocus(
                interactionSource,
                shape = RoundedCornerShape(0.dp, 50.dp, 50.dp, 0.dp)
            )
            .padding(end = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "$participantCount",
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_new),
                contentDescription = stringResource(id = R.string.kaleyra_show_participants_descr),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@MultiConfigPreview
@Composable
internal fun CallAppBarComponentPreview() = KaleyraTheme {
    Column {
        CallAppBarComponent(
            appIcon = Uri.EMPTY,
            title = "09:56",
            participantCount = 2,
            automaticRecording = true,
            recordingStateUi = RecordingStateUi.Started,
            callStateUi = CallStateUi.Connected,
            onParticipantClick = {},
            onBackPressed = {}
        )

        Spacer(modifier = Modifier.size(16.dp))

        CallAppBarComponent(
            appIcon = Uri.EMPTY,
            title = "09:56",
            participantCount = 2,
            automaticRecording = true,
            recordingStateUi = RecordingStateUi.Started,
            callStateUi = CallStateUi.Connecting,
            onParticipantClick = {},
            onBackPressed = {}
        )
    }
}