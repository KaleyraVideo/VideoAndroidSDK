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

package com.kaleyra.video_sdk.call.voice_settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.voice_settings.model.ModalVoiceSettingsUi
import com.kaleyra.video_sdk.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.video_sdk.call.voice_settings.view.ModalVoiceSettingsContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun ModalVoiceSettingsComponent(
    modifier: Modifier = Modifier,
    viewModel: AudioOutputViewModel = viewModel(
        factory = AudioOutputViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onChangeAudioOutputRequested: () -> Unit,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onClick = remember {
        { target: ModalVoiceSettingsUi ->
            when (target) {
                ModalVoiceSettingsUi.CallSoundsSwitch ->
                    if (uiState.areCallSoundsEnabled) viewModel.disableCallSounds()
                    else viewModel.enableCallSounds()
                ModalVoiceSettingsUi.AudioDeviceSelection -> {
                    onChangeAudioOutputRequested()
                }
            }
        }
    }
    ModalVoiceSettingsComponent(
        uiState = uiState,
        onItemClick = onClick,
        onCloseClick = onDismiss,
        modifier = modifier
    )
}

@Composable
internal fun ModalVoiceSettingsComponent(
    uiState: AudioOutputUiState,
    onItemClick: (ModalVoiceSettingsUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        modifier = Modifier.padding(top = 8.dp).then(modifier),
        title = stringResource(id = R.string.kaleyra_strings_action_settings),
        onCloseClick = onCloseClick,
    ) {
        ModalVoiceSettingsContent(
            items = ImmutableList(listOf(ModalVoiceSettingsUi.CallSoundsSwitch, ModalVoiceSettingsUi.AudioDeviceSelection)),
            audioOutputUiState = uiState,
            onItemClick = onItemClick,
        )
    }
}

@MultiConfigPreview
@Composable
internal fun ScreenShareComponentPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareComponent(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
