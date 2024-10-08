package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi

@Composable
internal fun pinnedPainterFor(pinned: Boolean): Painter =
    painterResource(id = if (pinned) R.drawable.ic_kaleyra_participants_component_unpin else R.drawable.ic_kaleyra_participants_component_pin)

@Composable
internal fun pinnedContentDescriptionFor(pinned: Boolean, username: String): String =
    stringResource(id = if (pinned) R.string.kaleyra_participants_component_unpin_stream_description else R.string.kaleyra_participants_component_pin_stream_description, username)

@Composable
internal fun pinnedTextFor(pinned: Boolean, username: String): String =
    stringResource(id = if (pinned) R.string.kaleyra_participants_component_unpin_stream else R.string.kaleyra_participants_component_pin_stream, username)

@Composable
internal fun disableMicPainterFor(streamAudio: AudioUi?): Painter =
    painterResource(id = if (streamAudio?.isEnabled == true) R.drawable.ic_kaleyra_participants_component_mic_on else R.drawable.ic_kaleyra_participants_component_mic_off)

@Composable
internal fun disableContentDescriptionFor(streamAudio: AudioUi?, username: String): String =
    stringResource(id = if (streamAudio?.isEnabled == true) R.string.kaleyra_participants_component_disable_microphone_description else R.string.kaleyra_participants_component_enable_microphone_description, username)

@Composable
internal fun disableTextFor(streamAudio: AudioUi?, username: String): String =
    stringResource(id = if (streamAudio?.isEnabled == true) R.string.kaleyra_participants_component_disable_microphone else R.string.kaleyra_participants_component_enable_microphone, username)

@Composable
internal fun mutePainterFor(streamAudio: AudioUi?): Painter =
    painterResource(id =
    if (streamAudio == null || streamAudio.isMutedForYou) R.drawable.ic_kaleyra_participants_component_speaker_off
    else R.drawable.ic_kaleyra_participants_component_speaker_on
    )

@Composable
internal fun muteContentDescriptionFor(streamAudio: AudioUi?, username: String): String =
    stringResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.string.kaleyra_participants_component_unmute_for_you_description else R.string.kaleyra_participants_component_mute_for_you_description, username)

@Composable
internal fun muteTextFor(streamAudio: AudioUi?, username: String): String =
    stringResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.string.kaleyra_participants_component_unmute_for_you else R.string.kaleyra_participants_component_mute_for_you, username)