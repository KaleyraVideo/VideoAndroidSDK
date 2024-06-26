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

package com.kaleyra.video_sdk.call.audiooutput

import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.model.mockAudioDevices
import com.kaleyra.video_sdk.call.audiooutput.view.AudioOutputContent
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.video_sdk.call.utils.BluetoothConnectPermission
import com.kaleyra.video_sdk.call.utils.BluetoothScanPermission
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun AudioOutputComponent(
    viewModel: AudioOutputViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AudioOutputViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onDeviceConnected: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    val isConnectionServiceEnabled = remember(viewModel) { viewModel.isConnectionServiceEnabled }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shouldAskBluetoothPermissions by remember(uiState) {
        derivedStateOf {
            uiState.audioDeviceList.value.any { it is AudioDeviceUi.Bluetooth }
        }
    }
    val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(permissions = listOf(BluetoothScanPermission, BluetoothConnectPermission))
    } else null

    if (shouldAskBluetoothPermissions && isConnectionServiceEnabled && !isTesting) {
        LaunchedEffect(Unit) {
            permissionsState?.launchMultiplePermissionRequest()
        }
    }

    AudioOutputComponent(
        uiState = uiState,
        onItemClick = remember(viewModel, onDeviceConnected) {
            {
                if (it is AudioDeviceUi.Bluetooth && !isConnectionServiceEnabled && !isTesting) {
                    permissionsState?.launchMultiplePermissionRequest()
                }
                viewModel.setDevice(it)
                onDeviceConnected()
            }
        },
        onCloseClick = onCloseClick,
        modifier = modifier
    )
}

@Composable
internal fun AudioOutputComponent(
    uiState: AudioOutputUiState,
    onItemClick: (AudioDeviceUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_audio_route_title),
        onCloseClick = onCloseClick,
        modifier = modifier
    ) {
        AudioOutputContent(
            items = uiState.audioDeviceList,
            playingDeviceId = uiState.playingDeviceId,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun AudioOutputComponentPreview() {
    KaleyraM3Theme {
        Surface {
            AudioOutputComponent(
                uiState = AudioOutputUiState(
                    audioDeviceList = mockAudioDevices,
                    playingDeviceId = mockAudioDevices.value[0].id
                ),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}