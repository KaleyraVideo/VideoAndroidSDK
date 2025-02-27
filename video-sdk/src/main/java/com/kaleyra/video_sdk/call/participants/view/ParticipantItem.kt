package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.utils.isLocalScreenShare
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal val ParticipantItemAvatarSize = 40.dp

@Composable
internal fun ParticipantItem(
    stream: StreamUi,
    isPinned: Boolean,
    isAdminStream: Boolean,
    amIAdmin: Boolean,
    isPinLimitReached: Boolean,
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
            username = stream.username,
            uri = stream.avatar,
            size = ParticipantItemAvatarSize
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (stream.isMine) {
                    stringResource(
                        id = R.string.kaleyra_participants_component_you,
                        stream.username
                    )
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
                style = MaterialTheme.typography.bodySmall
            )
        }

        when {
            stream.audio == null -> Unit
            amIAdmin || stream.isMine -> {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    interactionSource = interactionSource,
                    modifier = Modifier.highlightOnFocus(interactionSource),
                    enabled = stream.video == null || !stream.video.isScreenShare,
                    onClick = {
                        onDisableMicClick(
                            stream.id,
                            stream.audio.isEnabled
                        )
                    },
                    content = {
                        Icon(
                            disableMicPainterFor(stream.audio),
                            disableContentDescriptionFor(stream.audio, stream.username)
                        )
                    }
                )
            }
            else -> {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    interactionSource = interactionSource,
                    modifier = Modifier.highlightOnFocus(interactionSource),
                    enabled = stream.video == null || !stream.video.isScreenShare,
                    onClick = {
                        onMuteStreamClick(
                            stream.id,
                            !stream.audio.isMutedForYou
                        )
                    },
                    content = { Icon(mutePainterFor(stream.audio), muteContentDescriptionFor(stream.audio, stream.username)) }
                )
            }
        }

        if (!stream.isLocalScreenShare()) {
            if (!amIAdmin || stream.isMine) {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    interactionSource = interactionSource,
                    modifier = Modifier.highlightOnFocus(interactionSource),
                    enabled = !isPinLimitReached || isPinned,
                    onClick = { onPinStreamClick(stream.id, !isPinned) },
                    content = { Icon(pinnedPainterFor(isPinned), pinnedContentDescriptionFor(isPinned, stream.username)) }
                )
            } else {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    interactionSource = interactionSource,
                    modifier = Modifier.highlightOnFocus(interactionSource),
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
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantItemPreview() {
    KaleyraTheme {
        Surface {
            ParticipantItem(
                stream = streamUiMock,
                isPinned = true,
                isAdminStream = true,
                amIAdmin = true,
                isPinLimitReached = false,
                onMuteStreamClick = { _, _ -> },
                onDisableMicClick = { _, _ -> },
                onPinStreamClick = { _, _ -> },
                onMoreClick = { }
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantItemAsAdminPreview() {
    KaleyraTheme {
        Surface {
            ParticipantItem(
                stream = streamUiMock.copy(audio = AudioUi(id = "", isMutedForYou = true, isEnabled = false)),
                isPinned = true,
                isAdminStream = false,
                amIAdmin = false,
                isPinLimitReached = false,
                onMuteStreamClick = { _, _ -> },
                onDisableMicClick = { _, _ -> },
                onPinStreamClick = { _, _ -> },
                onMoreClick = { }
            )
        }
    }
}