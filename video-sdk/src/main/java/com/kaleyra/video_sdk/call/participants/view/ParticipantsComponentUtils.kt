package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.streamnew.model.AudioUi

@Composable
internal fun pinnedPainterFor(pinned: Boolean): Painter =
    painterResource(id = if (pinned) R.drawable.ic_kaleyra_participants_component_unpin else R.drawable.ic_kaleyra_participants_component_pin)

@Composable
internal fun pinnedTextFor(pinned: Boolean): String =
    stringResource(id = if (pinned) R.string.kaleyra_participants_component_unpin_stream else R.string.kaleyra_participants_component_pin_stream)

@Composable
internal fun disableMicPainterFor(streamAudio: AudioUi?): Painter =
    painterResource(id = if (streamAudio?.isEnabled == true) R.drawable.ic_kaleyra_participants_component_mic_on else R.drawable.ic_kaleyra_participants_component_mic_off)

@Composable
internal fun disableMicTextFor(streamAudio: AudioUi?): String =
    stringResource(id = if (streamAudio?.isEnabled == true) R.string.kaleyra_participants_component_disable_microphone else R.string.kaleyra_participants_component_enable_microphone)

@Composable
internal fun mutePainterFor(streamAudio: AudioUi?): Painter =
    painterResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.drawable.ic_kaleyra_participants_component_speaker_off else R.drawable.ic_kaleyra_participants_component_speaker_on)

@Composable
internal fun muteTextFor(streamAudio: AudioUi?): String =
    stringResource(id = if (streamAudio == null || streamAudio.isMutedForYou) R.string.kaleyra_participants_component_unmute_for_you else R.string.kaleyra_participants_component_mute_for_you)