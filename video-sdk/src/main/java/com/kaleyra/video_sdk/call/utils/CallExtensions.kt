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

package com.kaleyra.video_sdk.call.utils

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamPublisher

/**
 * Call Extensions
 */
internal object CallExtensions {

    /**
     * Utility function to retrieve the camera stream added to a call if any
     * @receiver Call the call in which to search for the camera stream
     * @return Stream.Mutable? the camera stream if found in the call
     */
    fun Call.toMyCameraStream(): Stream.Mutable? {
        val me = participants.value.me ?: return null
        val streams = me.streams.value
        return streams.firstOrNull { it.id == CameraStreamPublisher.CAMERA_STREAM_ID }
    }
}
