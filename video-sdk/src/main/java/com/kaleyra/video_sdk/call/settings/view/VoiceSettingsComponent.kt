package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
internal fun VoiceSettingsComponent(
    audioOutputUiState: AudioOutputUiState,
    onDisableAllSoundsRequested: (disabled: Boolean) -> Unit,
    onChangeAudioOutputRequested: () -> Unit,
) {
    val currentAudioDeviceUi = audioOutputUiState.audioDeviceList.value.firstOrNull { it.id == audioOutputUiState.playingDeviceId }

    Column(modifier = Modifier.testTag(VoiceSettingsTag)) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.kaleyra_strings_info_voice_settings),
            style = MaterialTheme.typography.bodyLarge)
        Box(modifier = Modifier
            .border(
                width = SettingsGroupBorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(SettingsGroupRoundCorner)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = SettingsGroupHorizontalPadding, vertical = SettingsGroupVerticalPadding)
            ) {
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_muted),
                    text = stringResource(R.string.kaleyra_strings_action_disable_all_sounds),
                    testTag = AudioDeviceMutedOptionsTag,
                    isToggleable = true,
                    isSelected = !audioOutputUiState.areCallSoundsEnabled,
                    isEnabled = true,
                    onCheckedChange = { onDisableAllSoundsRequested.invoke(it) }
                )
                SettingsItemComponent(
                    iconPainter = currentAudioDeviceUi?.let { painterFor(it) } ?: painterResource(R.drawable.ic_kaleyra_loud_speaker),
                    text = stringResource(R.string.kaleyra_strings_action_voice_change_audio_output),
                    isSelected = false,
                    isEnabled = true,
                    onCheckedChange = { onChangeAudioOutputRequested.invoke() }
                )
            }
        }
    }
}