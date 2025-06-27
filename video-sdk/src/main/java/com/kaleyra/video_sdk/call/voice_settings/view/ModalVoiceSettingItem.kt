/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.voice_settings.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.view.painterFor
import com.kaleyra.video_sdk.call.voice_settings.model.ModalVoiceSettingsUi

internal const val ModalVoiceSettingsDisableCallSoundsTag = "ModalVoiceSettingsDisableCallSoundsTag"

@Composable
internal fun ModalVoiceSettingsItem(
    modalVoiceSettingsUi: ModalVoiceSettingsUi,
    audioOutputUiState: AudioOutputUiState,
    onModalVoiceSettingsItemClicked: (ModalVoiceSettingsUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(48.dp).padding(end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterFor(modalVoiceSettingsUi, audioOutputUiState),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = textFor(modalVoiceSettingsUi),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (modalVoiceSettingsUi == ModalVoiceSettingsUi.CallSoundsSwitch) {
            Switch(
                modifier = Modifier.testTag(ModalVoiceSettingsDisableCallSoundsTag),
                checked = !audioOutputUiState.areCallSoundsEnabled,
                onCheckedChange = {
                    onModalVoiceSettingsItemClicked(modalVoiceSettingsUi)
                },
                enabled = true,
            )
        }
    }
}

@Composable
internal fun clickLabelFor(voiceSettingsUi: ModalVoiceSettingsUi) = textFor(voiceSettingsUi = voiceSettingsUi)

@Composable
private fun textFor(voiceSettingsUi: ModalVoiceSettingsUi) =
    stringResource(
        id = when (voiceSettingsUi) {
            ModalVoiceSettingsUi.CallSoundsSwitch -> R.string.kaleyra_strings_action_disable_all_sounds
            ModalVoiceSettingsUi.AudioDeviceSelection -> R.string.kaleyra_strings_action_voice_change_audio_output
        }
    )

@Composable
private fun painterFor(voiceSettingsUi: ModalVoiceSettingsUi, audioOutputUiState: AudioOutputUiState) =
    when (voiceSettingsUi) {
        ModalVoiceSettingsUi.CallSoundsSwitch -> painterResource(R.drawable.ic_kaleyra_muted)
        ModalVoiceSettingsUi.AudioDeviceSelection -> {
            val currentAudioDeviceUi = audioOutputUiState.audioDeviceList.value.firstOrNull { it.id == audioOutputUiState.playingDeviceId }
            if (currentAudioDeviceUi == null) painterResource(R.drawable.ic_kaleyra_muted)
            else painterFor(currentAudioDeviceUi)
        }
    }
