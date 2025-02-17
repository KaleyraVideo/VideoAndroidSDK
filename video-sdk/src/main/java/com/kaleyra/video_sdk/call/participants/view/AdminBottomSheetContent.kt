package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal val KickParticipantColor = Color(0xFFAE1300)

@Composable
internal fun AdminBottomSheetContent(
    stream: StreamUi,
    isStreamPinned: Boolean,
    isPinLimitReached: Boolean,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onKickParticipantClick: (streamId: String) -> Unit
) {
    LazyColumn( contentPadding = PaddingValues(vertical = 16.dp)) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Avatar(
                    username = stream.username,
                    size = 40.dp,
                    uri = stream.avatar,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stream.username,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
        }

        item {
            val interactionSource = remember { MutableInteractionSource() }
            AdminBottomSheetItem(
                interactionSource = interactionSource,
                modifier = Modifier.highlightOnFocus(interactionSource),
                text = pinnedTextFor(isStreamPinned, stream.username),
                painter = pinnedPainterFor(isStreamPinned),
                enabled = !isPinLimitReached || isStreamPinned,
                onClick = { onPinStreamClick(stream.id, !isStreamPinned)  }
            )
        }

        item {
            val interactionSource = remember { MutableInteractionSource() }
            AdminBottomSheetItem(
                interactionSource = interactionSource,
                modifier = Modifier.highlightOnFocus(interactionSource),
                text = muteTextFor(stream.audio, stream.username),
                painter = mutePainterFor(stream.audio),
                enabled = stream.video == null || !stream.video.isScreenShare,
                onClick = { if (stream.audio != null) onMuteStreamClick(stream.id, !stream.audio.isMutedForYou) else Unit }
            )
        }

        item {
            val interactionSource = remember { MutableInteractionSource() }
            AdminBottomSheetItem(
                modifier = Modifier.highlightOnFocus(interactionSource),
                text = stringResource(id = R.string.kaleyra_participants_component_remove_from_call),
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_kick),
                color = KickParticipantColor,
                onClick = { onKickParticipantClick(stream.id) }
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AdminBottomSheetContentPreview() {
    KaleyraTheme {
        Surface {
            AdminBottomSheetContent(
                stream = streamUiMock,
                isStreamPinned = false,
                isPinLimitReached = false,
                onMuteStreamClick = { _, _ -> },
                onPinStreamClick =  { _, _  -> },
                onKickParticipantClick = {}
            )
        }
    }
}