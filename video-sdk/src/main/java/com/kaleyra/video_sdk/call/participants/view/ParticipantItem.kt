package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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

internal val ParticipantItemAvatarSize = 28.dp

@Composable
internal fun ParticipantItem(
    stream: StreamUi,
    pinned: Boolean,
    isAdminStream: Boolean,
    amIAdmin: Boolean,
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
            text = stream.username[0].toString(),
            uri = stream.avatar,
            contentDescription = stringResource(id = R.string.kaleyra_avatar),
            backgroundColor = MaterialTheme.colorScheme.primary,
            size = ParticipantItemAvatarSize
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
                        isAdminStream -> R.string.kaleyra_participants_component_admin
                        else -> R.string.kaleyra_participants_component_participant
                    }
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }

        if (amIAdmin || stream.mine) {
            IconButton(
                onClick = { if (stream.audio != null) onDisableMicClick(stream.id, stream.audio.isEnabled) else Unit },
                content = { Icon(disableMicPainterFor(stream.audio), disableMicTextFor(stream.audio)) }
            )
        } else {
            IconButton(
                onClick = { if (stream.audio != null) onMuteStreamClick(stream.id, !stream.audio.isMutedForYou) else Unit },
                content = { Icon(mutePainterFor(stream.audio), muteTextFor(stream.audio)) }
            )
        }

        if (!amIAdmin || stream.mine) {
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