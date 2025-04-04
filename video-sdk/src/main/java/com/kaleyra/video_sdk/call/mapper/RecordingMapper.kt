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

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingTypeUi
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingUi
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object RecordingMapper {

    fun Call.toRecordingTypeUi(): Flow<RecordingTypeUi> =
        this.recording
            .map { it.type.mapToRecordingTypeUi() }
            .distinctUntilChanged()

    fun Call.toRecordingStateUi(): Flow<RecordingStateUi> =
        this.recording
            .flatMapLatest { it.state }
            .map { it.mapToRecordingStateUi() }
            .distinctUntilChanged()

    fun Call.toRecordingMessage(): Flow<RecordingMessage> =
        this.recording
            .flatMapLatest { it.state }
            .map { it.mapToRecordingMessage() }
            .distinctUntilChanged()

    fun Call.toRecordingUi(): Flow<RecordingUi> =
        combine(toRecordingTypeUi(), toRecordingStateUi()) { type, state ->
            RecordingUi(type, state)
        }.distinctUntilChanged()

    fun Call.Recording.Type.mapToRecordingTypeUi(): RecordingTypeUi =
        when (this) {
            Call.Recording.Type.OnConnect, Call.Recording.Type.Automatic -> RecordingTypeUi.Automatic
            Call.Recording.Type.OnDemand, Call.Recording.Type.Manual -> RecordingTypeUi.Manual
            else -> RecordingTypeUi.Never
        }

    fun Call.Recording.State.mapToRecordingStateUi(): RecordingStateUi =
        when(this) {
            is Call.Recording.State.Started -> RecordingStateUi.Started
            Call.Recording.State.Stopped -> RecordingStateUi.Stopped
            is Call.Recording.State.Stopped.Error -> RecordingStateUi.Error
        }

    private fun Call.Recording.State.mapToRecordingMessage(): RecordingMessage =
        when (this) {
            is Call.Recording.State.Started -> RecordingMessage.Started
            Call.Recording.State.Stopped -> RecordingMessage.Stopped
            is Call.Recording.State.Stopped.Error -> RecordingMessage.Failed
        }
}