package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.utils.isLocalScreenShare
import com.kaleyra.video_sdk.call.stream.view.core.SpeakingAnimationDuration
import com.kaleyra.video_sdk.call.stream.view.core.StopSpeakingStreamAnimationDelay
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawCircleBorder
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlin.math.roundToInt

private val AvatarStroke = 6.dp
private val SpeakingAvatarStroke = 2.dp

private val ParticipantItemAvatarSize = 40.dp + AvatarStroke
private val ParticipantItemHeight = 64.dp

internal val SpeakingParticipantStrokeAnimationDuration = SpeakingAnimationDuration
internal val StopSpeakingParticipantAnimationDelay = StopSpeakingStreamAnimationDelay

internal val SpeakingParticipantFontAnimationDuration = 100

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
    val avatarBackgroundColor = if (stream.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val isSpeaking = stream.audio?.isSpeaking == true

    val speakingAvatarStrokeAlpha by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0f,
        animationSpec = tween(
            durationMillis = SpeakingParticipantStrokeAnimationDuration,
            delayMillis = if (!isSpeaking) StopSpeakingParticipantAnimationDelay else 0),
        label = "animatedAudioLevelStrokeAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(ParticipantItemHeight)
    ) {
        Avatar(
            username = stream.userInfo?.username ?: "",
            uri = stream.userInfo?.image,
            size = ParticipantItemAvatarSize,
            backgroundColor = avatarBackgroundColor,
            contentColor = contentColorFor(avatarBackgroundColor),
            borderColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            borderWidth = AvatarStroke,
            modifier = Modifier
                .offset {
                    IntOffset(- AvatarStroke.toPx().roundToInt() / 2, 0)
                }
                .drawCircleBorder(
                    width = SpeakingAvatarStroke,
                    color = avatarBackgroundColor,
                    alpha = speakingAvatarStrokeAlpha
                )
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            val animatedWeightFraction by animateIntAsState(
                targetValue = if (isSpeaking) 1 else 0,
                animationSpec = tween(
                    durationMillis = SpeakingParticipantFontAnimationDuration,
                    delayMillis = if (isSpeaking) 0 else StopSpeakingParticipantAnimationDelay
                ),
                label = "FontWeightAnimation"
            )
            val interpolatedFontStyle = if (animatedWeightFraction == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge

            Text(
                text = if (stream.isMine) {
                    stringResource(
                        id = R.string.kaleyra_participants_component_you,
                        stream.userInfo?.username ?: ""
                    )
                } else stream.userInfo?.username ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = interpolatedFontStyle
            )
            Spacer(Modifier.height(4.dp))
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
                            disableContentDescriptionFor(stream.audio, stream.userInfo?.username ?: "")
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
                    content = { Icon(mutePainterFor(stream.audio), muteContentDescriptionFor(stream.audio, stream.userInfo?.username ?: "")) }
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
                    content = { Icon(pinnedPainterFor(isPinned), pinnedContentDescriptionFor(isPinned, stream.userInfo?.username ?: "")) }
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