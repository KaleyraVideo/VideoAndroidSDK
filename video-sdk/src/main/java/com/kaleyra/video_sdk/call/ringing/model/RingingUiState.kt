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

package com.kaleyra.video_sdk.call.ringing.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.precall.model.PreCallUiState
import com.kaleyra.video_sdk.call.recording.model.RecordingTypeUi
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Ringing Ui State
 * @property amIWaitingOthers Boolean flag indicating if the logged in-call participant is waiting for other participants to join the call
 * @property recording RecordingTypeUi? optional recording type
 * @property video VideoUi? optional video component
 * @property avatar ImmutableUri? optional avatar uri
 * @property participants ImmutableList<String> participants identifiers
 * @property watermarkInfo WatermarkInfo? optional watermark info
 * @property isLink Boolean flag indicating if the call is a join url call, true if a join url call, false otherwise
 * @property isConnecting Boolean flag indicating if the call is connecting, true if connecting, false otherwise
 * @constructor
 */
@Immutable
data class RingingUiState(
    val amIWaitingOthers: Boolean = false,
    val recording: RecordingTypeUi? = null,
    override val video: VideoUi? = null,
    override val avatar: ImmutableUri? = null,
    override val participants: ImmutableList<String> = ImmutableList(listOf()),
    override val watermarkInfo: WatermarkInfo? = null,
    override val isLink: Boolean = false,
    override val isConnecting: Boolean = false
): PreCallUiState<RingingUiState> {

    override fun clone(
        video: VideoUi?,
        avatar: ImmutableUri?,
        participants: ImmutableList<String>,
        watermarkInfo: WatermarkInfo?,
        isLink: Boolean,
        isConnecting: Boolean
    ): RingingUiState {
        return copy(
            video = video,
            avatar = avatar,
            participants = participants,
            watermarkInfo = watermarkInfo,
            isLink = isLink,
            isConnecting = isConnecting
        )
    }
}
