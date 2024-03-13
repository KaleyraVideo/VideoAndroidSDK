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
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.mapper.VideoMapper.mapParticipantsToVideos
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * StreamsManager
 */
internal class StreamsManager(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private val jobs = mutableListOf<Job>()

    fun bind(call: Call) {
        stop()
        handleStreamsOpening(call)
        handleStreamsVideoView(call)
    }

    fun stop() {
        jobs.forEach { it.cancel() }
    }

    /**
     * Open Participant streams
     * @param call Call the call object
     */
    fun handleStreamsOpening(call: Call) {
        jobs += call.participants
            .map { it.list }
            .flatMapLatest { participantsList ->
                participantsList.map { it.streams }.merge()
            }
            .onEach { it.forEach { stream -> stream.open() } }
            .launchIn(coroutineScope)
    }

    /**
     * Sets the streams video view
     *
     * @param call Call The call on which set the video view
     */
    fun handleStreamsVideoView(call: Call) {
        val context = ContextRetainer.context
        jobs += call.participants
            .map { it.list }
            .mapParticipantsToVideos()
            .transform { videos -> videos.forEach { emit(it) } }
            .filterIsInstance<Input.Video>()
            .onEach { video ->
                withContext(Dispatchers.Main) {
                    if (video.view.value != null) return@withContext
                    video.view.value = VideoStreamView(context.applicationContext)
                }
            }
            .launchIn(coroutineScope)
    }

}