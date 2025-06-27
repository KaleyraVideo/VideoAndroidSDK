package com.kaleyra.video_sdk.call.voice_settings.view

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.voice_settings.model.ModalVoiceSettingsUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun ModalVoiceSettingsContent(
    items: ImmutableList<ModalVoiceSettingsUi>,
    audioOutputUiState: AudioOutputUiState,
    onItemClick: (ModalVoiceSettingsUi) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items = items.value, key = { it.name }) {
            val interactionSource = remember { MutableInteractionSource() }
            ModalVoiceSettingsItem(
                modalVoiceSettingsUi = it,
                audioOutputUiState = audioOutputUiState,
                onModalVoiceSettingsItemClicked = onItemClick,
                modifier = Modifier
                    .clickable(
                        onClickLabel = clickLabelFor(it),
                        role = Role.Button,
                        onClick = { onItemClick(it) },
                        indication = LocalIndication.current,
                        interactionSource = interactionSource
                    )
                    .highlightOnFocus(interactionSource)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp,  vertical = 8.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ModalVoiceSettingsContentPreview() {
    KaleyraTheme {
        Surface {
            ModalVoiceSettingsContent(
                items = ImmutableList(listOf(ModalVoiceSettingsUi.CallSoundsSwitch, ModalVoiceSettingsUi.AudioDeviceSelection)),
                audioOutputUiState = AudioOutputUiState(
                    audioDeviceList = ImmutableList(listOf(AudioDeviceUi.Bluetooth(id = "bt", name = "bluetooth", connectionState = BluetoothDeviceState.Connected, batteryLevel = 100 ))),
                    playingDeviceId = "bt"
                ),
                onItemClick = { }
            )
        }
    }
}