package com.kaleyra.video_sdk.call.stream.view.items

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.defaultStreamViewSettings
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal val StreamItemPadding = 8.dp
internal val ZoomIconTestTag = "ZoomIconTestTag"

internal val StreamItemTag = "StreamItemTag"

@Composable
internal fun StreamItem(
    stream: StreamUi,
    fullscreen: Boolean,
    pin: Boolean,
    modifier: Modifier = Modifier,
    statusIconsAlignment: Alignment = Alignment.BottomEnd,
    onClick: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.testTag(StreamItemTag)
    ) {
        PointerStreamWrapper(
            streamView = stream.video?.view,
            pointerList = stream.video?.pointers
        ) { _ ->
            Stream(
                streamView = stream.video?.view?.defaultStreamViewSettings(),
                avatar = stream.avatar,
                username = stream.username,
                showStreamView = stream.video?.view != null && stream.video.isEnabled,
                onClick = onClick
            )
        }

        StreamStatusIcons(
            streamAudioUi = stream.audio,
            streamVideoUi = stream.video,
            fullscreen = fullscreen,
            mine = stream.isMine,
            modifier = Modifier
                .align(statusIconsAlignment)
                .padding(StreamItemPadding)
        )

        UserLabel(
            username = if (stream.isMine) stringResource(id = R.string.kaleyra_stream_you) else stream.username,
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
        when {
            streamAudioUi == null -> Unit
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
            modifier = Modifier.semantics {
                contentDescription = zoomContentDescription
            }.testTag(ZoomIconTestTag),
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
                    username = "Viola J. Allen",
                    video = VideoUi(id = "id", view = ImmutableView(VideoStreamView(LocalContext.current)), zoomLevelUi = VideoUi.ZoomLevelUi.`3x`),
                    audio = AudioUi(id = "id", isEnabled = true, isMutedForYou = true),
                ),
                fullscreen = true,
                pin = true
            )
        }
    }
}
