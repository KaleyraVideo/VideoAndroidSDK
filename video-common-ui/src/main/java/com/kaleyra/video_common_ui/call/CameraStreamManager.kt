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

package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.mapper.InputMapper.toAudioInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraVideoInput
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_common_ui.utils.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

object CameraStreamConstants {
    const val CAMERA_STREAM_ID = "camera"
}

internal class CameraStreamManager(
    private val coroutineScope: CoroutineScope = CoroutineScope(
        Dispatchers.IO
    )
) {

    private val jobs = mutableListOf<Job>()

    fun bind(call: Call) {
        stop()
        addCameraStream(call)
        handleCameraStreamAudio(call)
        handleCameraStreamVideo(call)
    }

    fun stop() {
        jobs.forEach { it.cancel() }
    }

    /**
     * Add a stream of camera type to the call
     *
     * @param call The call in which to add the camera stream
     */
    fun addCameraStream(call: Call) {
        jobs += coroutineScope.launch {
            val me = call.participants.mapNotNull { it.me }.first()
            if (me.streams.value.firstOrNull { it.id == CAMERA_STREAM_ID } != null) return@launch
            me.addStream(CAMERA_STREAM_ID)
        }
    }

    fun handleCameraStreamAudio(call: Call) {
        jobs += combine(call.toAudioInput(), call.toMyCameraStream()) { audio, cameraStream ->
            cameraStream.audio.value = audio
        }.launchIn(coroutineScope)
    }

    fun handleCameraStreamVideo(call: Call) {
        jobs += combine(
            call.toCameraVideoInput(),
            call.preferredType,
            call.toMyCameraStream()
        ) { video, preferredType, cameraStream ->
            val hasVideo = preferredType.hasVideo()
            if (!hasVideo) return@combine

            val quality = if (DeviceUtils.isSmartGlass) {
                Input.Video.Quality.Definition.HD
            } else Input.Video.Quality.Definition.SD
            video.setQuality(quality)
            if (video is Input.Video.Camera.Usb) video.awaitPermission()
            cameraStream.video.value = video
        }.launchIn(coroutineScope)
    }

    private suspend fun Input.Video.Camera.Usb.awaitPermission() {
        if (state.value is Input.State.Closed.AwaitingPermission) {
            state.firstOrNull { it !is Input.State.Closed.AwaitingPermission }
        }
    }

}
