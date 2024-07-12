package com.kaleyra.video_sdk.call.streamnew.view.items

import android.content.res.Configuration
import android.view.View
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.pointer.view.PointerStreamWrapper
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
import com.kaleyra.video_sdk.call.streamnew.model.core.ImmutableView
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.view.core.Stream
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.defaultStreamViewSettings
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

internal val StreamItemPadding = 8.dp

@Composable
internal fun StreamItem(
    stream: StreamUi,
    fullscreen: Boolean,
    pin: Boolean,
    modifier: Modifier = Modifier,
    statusIconsAlignment: Alignment = Alignment.BottomEnd
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        PointerStreamWrapper(
            streamView = stream.video?.view,
            pointerList = stream.video?.pointers
        ) { _ ->
            Stream(
                streamView = stream.video?.view?.defaultStreamViewSettings(),
                avatar = stream.avatar,
                username = stream.username,
                showStreamView = stream.video?.view != null && stream.video.isEnabled
            )
        }

        StatusIcons(
            streamAudioUi = stream.audio,
            fullscreen = fullscreen,
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
private fun StatusIcons(
    streamAudioUi: AudioUi?,
    fullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        when {
            streamAudioUi == null || !streamAudioUi.isEnabled -> MicDisabledIcon()
            streamAudioUi.isMutedForYou -> AudioMutedForYouIcon()
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
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .1f),
        contentColor = MaterialTheme.colorScheme.inverseSurface,
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
                style = MaterialTheme.typography.labelMedium
            )
        }
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
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .1f),
        contentColor = MaterialTheme.colorScheme.inverseSurface,
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
    KaleyraM3Theme {
        Surface {
            StreamItem(
                stream = StreamUi(
                    id = "id",
                    username = "Viola J. Allen",
                    video = VideoUi(id = "id", view = ImmutableView(View(LocalContext.current))),
                    audio = AudioUi(id = "id", isEnabled = true, isMutedForYou = true),
                ),
                fullscreen = true,
                pin = true
            )
        }
    }
}