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

package com.kaleyra.video_sdk.call.audiooutput.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.audiooutput.model.isConnectedOrPlaying
import com.kaleyra.video_sdk.call.audiooutput.model.isConnecting
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun AudioOutputItem(
    audioDevice: AudioDeviceUi,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterFor(audioDevice),
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.Center) {
            val bodyLargeStyle = MaterialTheme.typography.bodyLarge
            Text(
                text = titleFor(audioDevice),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = if (selected) MaterialTheme.typography.titleMedium.copy(letterSpacing = bodyLargeStyle.letterSpacing) else bodyLargeStyle,
                color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            )
            val subtitle = subtitleFor(audioDevice)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
internal fun clickLabelFor(device: AudioDeviceUi) = titleFor(device = device)

@Composable
private fun titleFor(device: AudioDeviceUi): String =
    when (device) {
        is AudioDeviceUi.LoudSpeaker -> stringResource(R.string.kaleyra_call_action_audio_route_loudspeaker)
        is AudioDeviceUi.EarPiece -> stringResource(R.string.kaleyra_call_action_audio_route_earpiece)
        is AudioDeviceUi.WiredHeadset -> stringResource(R.string.kaleyra_call_action_audio_route_wired_headset)
        is AudioDeviceUi.Muted -> stringResource(R.string.kaleyra_call_action_audio_route_muted)
        is AudioDeviceUi.Bluetooth -> device.name ?: stringResource(R.string.kaleyra_call_action_audio_route_bluetooth)
    }

@Composable
private fun subtitleFor(device: AudioDeviceUi): String? =
    when (device) {
        is AudioDeviceUi.Bluetooth -> {
            val connectionState = device.connectionState ?: run {
                return null
            }
            val batteryLevel = device.batteryLevel

            val deviceState = when {
                connectionState == BluetoothDeviceState.Disconnected -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_disconnected)
                connectionState == BluetoothDeviceState.Failed -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_failed)
                connectionState == BluetoothDeviceState.Available -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_available)
                connectionState.isConnectedOrPlaying() -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
                connectionState == BluetoothDeviceState.Deactivating -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_deactivating)
                else -> ""
            }

            val battery = if (batteryLevel != null) stringResource(
                R.string.kaleyra_bluetooth_battery_info,
                stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_battery_level),
                batteryLevel
            ) else ""

            val connectingState = when {
                connectionState.isConnecting() && deviceState.isNotBlank() -> stringResource(
                    R.string.kaleyra_bluetooth_connecting_status_info,
                    stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
                )
                connectionState.isConnecting() -> stringResource(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
                else -> ""
            }

            stringResource(R.string.kaleyra_bluetooth_info, deviceState, battery, connectingState)
        }
        else -> null
    }

@Composable
fun painterFor(device: AudioDeviceUi): Painter = painterResource(
    id = when (device) {
        is AudioDeviceUi.LoudSpeaker -> R.drawable.ic_kaleyra_loud_speaker
        is AudioDeviceUi.EarPiece -> R.drawable.ic_kaleyra_earpiece
        is AudioDeviceUi.WiredHeadset -> R.drawable.ic_kaleyra_wired_headset
        is AudioDeviceUi.Muted -> R.drawable.ic_kaleyra_muted
        is AudioDeviceUi.Bluetooth -> R.drawable.ic_kaleyra_bluetooth_headset
    }
)

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputLoudSpeakerItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.LoudSpeaker)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputEarpieceItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.EarPiece)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputWiredHeadsetItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.WiredHeadset)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputMutedItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.Muted)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputBluetoothItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.Bluetooth(id = "", name = null, connectionState = BluetoothDeviceState.Activating, batteryLevel = 50))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputSelectedItemPreview() {
    AudioOutputItemPreview(AudioDeviceUi.LoudSpeaker, selected = true)
}

@Composable
private fun AudioOutputItemPreview(audioDevice: AudioDeviceUi, selected: Boolean = false) {
    KaleyraTheme {
        Surface {
            AudioOutputItem(
                audioDevice = audioDevice,
                selected = selected
            )
        }
    }
}


