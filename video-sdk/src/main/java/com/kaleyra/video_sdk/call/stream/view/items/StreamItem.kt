package com.kaleyra.video_sdk.call.stream.view.items

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.mapper.VideoMapper.prettyPrint
import com.kaleyra.video_sdk.call.pointer.view.PointerStreamWrapper
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.utils.isSpeaking
import com.kaleyra.video_sdk.call.stream.utils.isVideoEnabled
import com.kaleyra.video_sdk.call.stream.view.audio.AudioVisualizer
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.defaultStreamViewSettings
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.extensions.DpExtensions.toPixel
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawRoundedCornerBorder
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.delay

internal val StreamItemPadding = 8.dp
internal val StreamItemSpeakingBorderWidth = 3.dp

internal val ZoomIconTestTag = "ZoomIconTestTag"
internal val StreamItemTag = "StreamItemTag"
internal val AudioVisualizerTag = "AudioLevelIconTag"
internal val AudioLevelBackgroundTag = "AudioLevelBackgroundTag"

private val SpeakingAnimationDuration = 500
private val StopSpeakingAnimationDelay = 1000
internal val StopSpeakingAudioAnimationDelay = 1500L

@Composable
internal fun StreamItem(
    stream: StreamUi,
    fullscreen: Boolean,
    pin: Boolean,
    modifier: Modifier = Modifier,
    statusIconsAlignment: Alignment = Alignment.BottomEnd,
    onClick: (() -> Unit)? = null
) {
    val isSpeaking = remember(stream) { stream.isSpeaking() }
    val isVideoEnabled = remember(stream) { stream.isVideoEnabled() }

    val borderColor = if (stream.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0f,
        animationSpec = tween(
            durationMillis = SpeakingAnimationDuration,
            delayMillis = if (isSpeaking) 0 else StopSpeakingAnimationDelay
        ),
        label = "animatedAudioLevelStrokeAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(StreamItemTag)
            .drawRoundedCornerBorder(
                width = if (isVideoEnabled) StreamItemSpeakingBorderWidth else 0.dp,
                color = borderColor,
                alpha = borderAlpha,
                cornerRadius = CornerRadius(StreamComponentDefaults.CornerRadius.toPixel),
            )
    ) {
        // Background when speaking
        AnimatedVisibility(
            visible = !isVideoEnabled && isSpeaking,
            enter = fadeIn(tween(SpeakingAnimationDuration)),
            exit = fadeOut(tween(SpeakingAnimationDuration, StopSpeakingAnimationDelay))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = borderColor.copy(alpha = .12f))
                    .testTag(AudioLevelBackgroundTag)
            )
        }

        PointerStreamWrapper(
            streamView = stream.video?.view,
            pointerList = stream.video?.pointers
        ) { _ ->
            Stream(
                streamView = stream.video?.view?.defaultStreamViewSettings(),
                userInfo = stream.userInfo,
                isMine = stream.isMine,
                isSpeaking = isSpeaking,
                showStreamView = stream.video?.view != null && isVideoEnabled,
                onClick = onClick
            )
        }

        StreamStatusIcons(
            streamAudioUi = stream.audio,
            streamVideoUi = stream.video,
            fullscreen = fullscreen,
            mine = stream.isMine,
            isSpeaking = isSpeaking,
            modifier = Modifier
                .align(statusIconsAlignment)
                .padding(StreamItemPadding)
        )

        UserLabel(
            username = if (stream.isMine) stringResource(id = R.string.kaleyra_stream_you) else stream.userInfo?.username ?: "",
            pin = pin,
            modifier = Modifier
                .padding(StreamItemPadding)
                .height(24.dp)
                .align(Alignment.BottomStart)
        )
    }
}

@Composable
internal fun StreamStatusIcons(
    streamAudioUi: AudioUi?,
    streamVideoUi: VideoUi?,
    fullscreen: Boolean,
    mine: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        streamVideoUi?.takeIf { it.isEnabled }?.zoomLevelUi?.prettyPrint()?.takeIf { it.isNotBlank() }?.let {
            ZoomIcon(text = it)
        }
        StreamAudioLevelIcon(isSpeaking = isSpeaking, mine = mine)

        when {
            streamAudioUi == null -> MicDisabledIcon()
            streamAudioUi.isMutedForYou && !mine -> AudioMutedForYouIcon()
            !streamAudioUi.isEnabled -> MicDisabledIcon()
        }
        if (fullscreen) FullscreenIcon()
    }
}

@Composable
private fun UserLabel(
    modifier: Modifier = Modifier,
    username: String,
    pin: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (pin) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_stream_pin),
                    contentDescription = stringResource(id = R.string.kaleyra_stream_pin),
                    modifier = Modifier
                        .padding(5.dp)
                        .offset(x = (-4).dp)
                )
            }
            Text(
                text = username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ZoomIcon(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        val zoomContentDescription = "${stringResource(R.string.kaleyra_call_sheet_zoom)} $text"
        Text(
            modifier = Modifier
                .semantics {
                    contentDescription = zoomContentDescription
                }
                .testTag(ZoomIconTestTag),
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun AudioMutedForYouIcon(modifier: Modifier = Modifier) {
    StreamStatusIcon(
        painter = painterResource(id = R.drawable.ic_kaleyra_stream_audio_muted_for_you),
        contentDescription = stringResource(id = R.string.kaleyra_stream_muted_for_you),
        modifier = modifier
    )
}

@Composable
private fun MicDisabledIcon(modifier: Modifier = Modifier) {
    StreamStatusIcon(
        painter = painterResource(id = R.drawable.ic_kaleyra_stream_audio_disabled),
        contentDescription = stringResource(id = R.string.kaleyra_stream_mic_disabled),
        modifier = modifier
    )
}

@Composable
private fun FullscreenIcon(modifier: Modifier = Modifier) {
    StreamStatusIcon(
        painter = painterResource(id = R.drawable.ic_kaleyra_stream_fullscreen_on),
        contentDescription = stringResource(id = R.string.kaleyra_stream_fullscreen),
        modifier = modifier
    )
}

@Composable
fun StreamAudioLevelIcon(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean,
    mine: Boolean
) {
    var showAudioVisualizer by remember { mutableStateOf(false) }

    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) delay(StopSpeakingAudioAnimationDelay)
        showAudioVisualizer = isSpeaking
    }

    val audioLevelContentDescription = stringResource(R.string.kaleyra_stream_audio_level)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    if (showAudioVisualizer) {
        Surface(
            modifier = modifier
                .size(24.dp)
                .semantics { contentDescription = audioLevelContentDescription }
                .testTag(AudioVisualizerTag),
            color = backgroundColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(4.dp),
        ) {
            AudioVisualizer(
                barWidth = 3.5.dp,
                barSpacing = 3.dp,
                barCount = 3,
                enable = isSpeaking
            )
        }
    }
}

@Composable
private fun StreamStatusIcon(
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.size(24.dp)
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.padding(3.dp)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun StreamItemPreview() {
    KaleyraTheme {
        Surface {
            StreamItem(
                stream = StreamUi(
                    id = "id",
                    userInfo = UserInfo("userId", "Viola J. Allen", ImmutableUri(Uri.EMPTY)),
                    video = VideoUi(id = "id", view = ImmutableView(VideoStreamView(LocalContext.current)), zoomLevelUi = VideoUi.ZoomLevelUi.`3x`),
                    audio = AudioUi(id = "id", isEnabled = true, isMutedForYou = true, isSpeaking = true),
                ),
                fullscreen = true,
                pin = true
            )
        }
    }
}
