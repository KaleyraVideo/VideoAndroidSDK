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

package com.kaleyra.video_sdk.call.audiooutput.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Audio Device State
 * @property lastUsedDeviceId String? last valid audio device used
 * @property audioDeviceList ImmutableList<AudioDeviceUi> available audio devices list
 * @property playingDeviceId String? current playing device identifier
 * @property areCallSoundsEnabled Boolean flag indicating if the call sounds are enabled or not
 * @constructor
 */
@Immutable
internal data class AudioOutputUiState(
    val lastUsedDeviceId: String? = null,
    val audioDeviceList: ImmutableList<AudioDeviceUi> = ImmutableList(emptyList()),
    val playingDeviceId: String? = null,
    val areCallSoundsEnabled: Boolean = true
) : UiState