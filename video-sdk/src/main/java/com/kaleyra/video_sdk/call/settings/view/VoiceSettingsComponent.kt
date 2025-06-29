package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.view.painterFor

private val VoiceSettingsGroupBorderWidth = 1.dp
private val VoiceSettingsGroupRoundCorner = 11.dp
private val VoiceSettingsGroupTitleBottomPadding = 16.dp
private val VoiceSettingsGroupHorizontalStartPadding = 24.dp
private val VoiceSettingsGroupHorizontalEndPadding = 16.dp
private val VoiceSettingsGroupVerticalPadding = 8.dp

internal const val MuteAllSoundsSwitchTestTag = "AudioDeviceMutedOptionsTag"
internal const val ChangeAudioOutputTestTag = "ChangeAudioOutputTestTag"

@Composable
internal fun VoiceSettingsComponent(
    audioOutputUiState: AudioOutputUiState,
    onDisableAllSoundsRequested: (disabled: Boolean) -> Unit,
    onChangeAudioOutputRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentAudioDeviceUi = audioOutputUiState.audioDeviceList.value.firstOrNull { it.id == audioOutputUiState.playingDeviceId }

    Column(modifier = Modifier
        .testTag(VoiceSettingsTag)
        .then(modifier)) {
        Text(
            modifier = Modifier.padding(bottom = VoiceSettingsGroupTitleBottomPadding),
            text = stringResource(R.string.kaleyra_strings_info_voice_settings),
            style = MaterialTheme.typography.bodyLarge)
        Card(
            border = BorderStroke(VoiceSettingsGroupBorderWidth, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(VoiceSettingsGroupRoundCorner)
        ) {
            Column {
                SettingsItemComponent(
                    modifier = Modifier
                        .height(SettingsItemComponentHeight + VoiceSettingsGroupVerticalPadding + VoiceSettingsGroupVerticalPadding / 2)
                        .padding(start = VoiceSettingsGroupHorizontalStartPadding, end = VoiceSettingsGroupHorizontalEndPadding, top = VoiceSettingsGroupVerticalPadding, bottom = VoiceSettingsGroupVerticalPadding / 2),
                    iconPainter = painterResource(R.drawable.ic_kaleyra_muted),
                    text = stringResource(R.string.kaleyra_strings_action_disable_all_sounds),
                    testTag = MuteAllSoundsSwitchTestTag,
                    isToggleable = true,
                    isSelected = !audioOutputUiState.areCallSoundsEnabled,
                    isEnabled = true,
                    onCheckedChange = { onDisableAllSoundsRequested.invoke(it) },
                    highlightFocusShape = RoundedCornerShape(topStart = VoiceSettingsGroupRoundCorner, topEnd = VoiceSettingsGroupRoundCorner)
                )
                SettingsItemComponent(
                    modifier = Modifier
                        .height(SettingsItemComponentHeight + VoiceSettingsGroupVerticalPadding + VoiceSettingsGroupVerticalPadding / 2)
                        .padding(start = VoiceSettingsGroupHorizontalStartPadding, end = VoiceSettingsGroupHorizontalEndPadding, top = VoiceSettingsGroupVerticalPadding / 2, bottom = VoiceSettingsGroupVerticalPadding),
                    iconPainter = currentAudioDeviceUi?.let { painterFor(it) } ?: painterResource(R.drawable.ic_kaleyra_loud_speaker),
                    text = stringResource(R.string.kaleyra_strings_action_voice_change_audio_output),
                    testTag = ChangeAudioOutputTestTag,
                    isSelected = false,
                    isEnabled = true,
                    onCheckedChange = { onChangeAudioOutputRequested.invoke() },
                    highlightFocusShape = RoundedCornerShape(bottomStart = VoiceSettingsGroupRoundCorner, bottomEnd = VoiceSettingsGroupRoundCorner)
                )
            }
        }
    }
}