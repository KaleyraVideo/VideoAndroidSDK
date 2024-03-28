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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
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
                    stringResource(id = R.string.kaleyra_participants_component_you, stream.username)
                } else stream.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    when {
                        stream.video?.isScreenShare == true -> R.string.kaleyra_participants_component_screenshare
                        admin -> R.string.kaleyra_participants_component_admin
                        else -> R.string.kaleyra_participants_component_participant
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
                content = { Icon(disableMicPainterFor(stream.audio), disableMicTextFor(stream.audio)) }
            )
        } else {
            IconButton(
                onClick = { if (stream.audio != null) onMuteStreamClick(stream.id, !stream.audio.isMutedForYou) else Unit },
                content = { Icon(mutePainterFor(stream.audio), muteTextFor(stream.audio)) }
            )
        }

        if (!enableAdminSheet || stream.mine) {
            IconButton(
                onClick = { onPinStreamClick(stream.id, !pinned) },
                content = { Icon(pinnedPainterFor(pinned), pinnedTextFor(pinned)) }
            )
        } else {
            IconButton(
                onClick = onMoreClick,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_more),
                        contentDescription = stringResource(id = R.string.kaleyra_participants_component_show_more_actions)
                    )
                }
            )
        }
    }
}