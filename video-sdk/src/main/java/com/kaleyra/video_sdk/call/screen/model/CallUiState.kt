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

package com.kaleyra.video_sdk.call.screen.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.streamnew.model.StreamUi
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.call.recording.model.RecordingUi
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Call Ui State representing the Ui state used in the Call Ui
 * @property callState CallStateUi call state
 * @property thumbnailStreams ImmutableList<StreamUi> thumbnail streams list
 * @property featuredStreams ImmutableList<StreamUi> streams list
 * @property fullscreenStream StreamUi? optional current fullscreen stream
 * @property watermarkInfo WatermarkInfo? optional watermark info
 * @property recording RecordingUi? optional recording
 * @property isAudioOnly Boolean flag indicating if the call is audio only, true if audio only, false otherwise
 * @property isGroupCall Boolean flag indicating if the call is a group call, true if a group call, false otherwise
 * @property amIWaitingOthers Boolean flag indicating if the logged participant is awaiting other participants
 * @property amILeftAlone Boolean flag indicating if the logged participant is currently the only participant in call, true if alone, false otherwise
 * @property showFeedback Boolean flag indicating if the call ui should present the call feedback on call ended, true to show it, false otherwise
 * @property shouldAutoHideSheet Boolean flag indicating if the call should auto hide the call controls, true to auto hide the call controls, false otherwise
 * @constructor
 */
@Immutable
data class CallUiState(
    val callState: CallStateUi = CallStateUi.Disconnected,
    val thumbnailStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val featuredStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val fullscreenStream: StreamUi? = null,
    val watermarkInfo: WatermarkInfo? = null,
    val recording: RecordingUi? = null,
    val isAudioOnly: Boolean = false,
    val isGroupCall: Boolean = false,
    val areCallActionsReady: Boolean = false,
    val amIWaitingOthers: Boolean = false,
    val amILeftAlone: Boolean = false,
    val showFeedback: Boolean = false,
    val shouldAutoHideSheet: Boolean = false
) : UiState
