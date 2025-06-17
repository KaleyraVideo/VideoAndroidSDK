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

@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toMe
import com.kaleyra.video_common_ui.utils.FlowUtils.flatMapLatestNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * Call Input flow utilities
 */
object InputMapper {

    /**
     * Utility function to detect whenever an input is active
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever a phone call input is active
     */
    inline fun <reified T : Input> Call.isInputActive(): Flow<Boolean> =
        this.inputs.availableInputs
            .map { inputs -> inputs.firstOrNull { it is T } }
            .flatMapLatest { it?.state ?: flowOf(null) }
            .map { it is Input.State.Active }
            .distinctUntilChanged()

    /**
     * Utility function to detect whenever the screen input is active
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever the screen input is active
     */
    fun Call.isDeviceScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Screen.My>()

    /**
     * Utility function to detect whenever the app screen input is active
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever the app screen input is active
     */
    fun Call.isAppScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Application>()

    /**
     * Utility function to detect whenever an input is active
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever a phone call input is active
     */
    fun Call.isAnyScreenInputActive(): Flow<Boolean> =
        combine(isDeviceScreenInputActive(), isAppScreenInputActive()) { isDeviceScreenInputActive, isAppScreenInputActive ->
            isDeviceScreenInputActive || isAppScreenInputActive
        }

    /**
     * Utility function to detect whenever an audio mute event has been sent
     * @receiver Flow<Call> the call flow
     * @return Flow<Input.Audio.Event.Request.Mute> flow emitting whenever an audio mute event has been sent
     */
    fun Call.toMuteEvents(): Flow<Input.Audio.Event.Request.Mute> =
        this.toCameraStreamAudio()
            .filterNotNull()
            .flatMapLatest { it.events }
            .filterIsInstance()

    /**
     * Utility function to detect whenever a stream with audio has been created
     * @receiver Flow<Call> the call flow
     * @return Flow<Input.Audio?> flow emitting whenever an audio is available on a publishing local stream
     */
    fun Call.toCameraStreamAudio(): Flow<Input.Audio?> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.firstOrNull { stream -> stream.id == CameraStreamConstants.CAMERA_STREAM_ID } }
            .flatMapLatest { it?.audio ?: flowOf(null) }

    /**
     * Utility function to detect whenever a screen sharing input is available
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever a screen sharing input is currently available
     */
    fun Call.hasScreenSharingInput(): Flow<Boolean> =
        this.inputs.availableInputs
            .map { inputs -> inputs.any { it is Input.Video.Screen.My } }
            .distinctUntilChanged()

    /**
     * Utility function to detect whenever an internal video input is available
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever an internal video input is currently available
     */
    fun Call.hasInternalCameraInput(): Flow<Boolean> =
        this.inputs.availableInputs
            .map { inputs -> inputs.any { it is Input.Video.Camera.Internal } }
            .distinctUntilChanged()

    /**
     * Utility function to detect whenever an audio input is available
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow emitting true whenever an audio input is currently available
     */
    fun Call.hasAudioInput(): Flow<Boolean> =
        this.inputs.availableInputs
            .map { inputs -> inputs.any { it is Input.Audio } }
            .distinctUntilChanged()

    fun Call.toAudioInput(): Flow<Input.Audio.My?> =
        this.inputs.availableInputs
            .map { it.filterIsInstance<Input.Audio.My>().firstOrNull() }

    fun Call.toCameraVideoInput(): Flow<Input.Video.My?> =
        this.inputs.availableInputs
            .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera } }
            .filterIsInstance<Input.Video.My?>()

    fun Call.toMyCameraStream(): Flow<Stream.Mutable> =
        this.participants
            .flatMapLatestNotNull { it.me?.streams }
            .mapNotNull { streams ->
                streams.firstOrNull { it.id == CameraStreamConstants.CAMERA_STREAM_ID } }

    fun Call.hasActiveVirtualBackground() =
        this.toMyCameraStream()
            .flatMapLatest { it.video }
            .flatMapLatest { it?.currentEffect ?: flowOf(null) }
            .map { it != null && it !is Effect.Video.None }
            .distinctUntilChanged()
}
