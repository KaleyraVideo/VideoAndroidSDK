package com.kaleyra.video_sdk.call.participantspanel.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.contentColorFor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.avatar.view.Avatar

@Composable
internal fun ParticipantItem(
    stream: StreamUi,
    pinned: Boolean,
    admin: Boolean,
    enableAdminSheet: Boolean,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onDisableMicClick: (streamId: String, disable: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Avatar(
            uri = stream.avatar,
            contentDescription = stringResource(id = R.string.kaleyra_avatar),
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
            size = 28.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (stream.mine) {
                    stringResource(id = R.string.kaleyra_participants_panel_you, stream.username)
                } else stream.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    when {
                        stream.video?.isScreenShare == true -> R.string.kaleyra_participants_panel_screenshare
                        admin -> R.string.kaleyra_participants_panel_admin
                        else -> R.string.kaleyra_participants_panel_participant
                    }
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }

        if (enableAdminSheet || stream.mine) {
            IconButton(
                onClick = { if (stream.audio != null) onDisableMicClick(stream.id, !stream.audio.isEnabled) else Unit },
                content = { Icon(disableMicPainter(stream.audio), disableMicText(stream.audio)) }
            )
        } else {
            IconButton(
                onClick = { if (stream.audio != null) onMuteStreamClick(stream.id, !stream.audio.isMutedForYou) else Unit },
                content = { Icon(muteForYouPainter(stream.audio), muteForYouText(stream.audio)) }
            )
        }

        if (!enableAdminSheet || stream.mine) {
            IconButton(
                onClick = { onPinStreamClick(stream.id, !pinned) },
                content = {
                    Icon(
                        painter = painterResource(id = if (pinned) R.drawable.ic_kaleyra_participant_panel_unpin else R.drawable.ic_kaleyra_participant_panel_pin),
                        contentDescription = stringResource(id = if (pinned) R.string.kaleyra_participants_panel_unpin else R.string.kaleyra_participants_panel_pin)
                    )
                }
            )
        } else {
            IconButton(
                onClick = onMoreClick,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_more),
                        contentDescription = stringResource(id = R.string.kaleyra_participants_panel_show_more_actions)
                    )
                }
            )
        }
    }
}

@Composable
private fun disableMicPainter(streamAudio: AudioUi?): Painter =
    painterResource(id = if (streamAudio?.isEnabled == true) R.drawable.ic_kaleyra_participant_panel_mic_on else R.drawable.ic_kaleyra_participant_panel_mic_off)

@Composable
private fun disableMicText(streamAudio: AudioUi?): String =
    stringResource(id = if (streamAudio?.isEnabled == true) R.string.kaleyra_participants_panel_disable_microphone else R.string.kaleyra_participants_panel_enable_microphone)

@Composable
private fun muteForYouPainter(streamAudio: AudioUi?): Painter =
    painterResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.drawable.ic_kaleyra_participant_panel_speaker_off else R.drawable.ic_kaleyra_participant_panel_speaker_on)

@Composable
private fun muteForYouText(streamAudio: AudioUi?): String =
    stringResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.string.kaleyra_participants_panel_unmute_for_you else R.string.kaleyra_participants_panel_mute_for_you)