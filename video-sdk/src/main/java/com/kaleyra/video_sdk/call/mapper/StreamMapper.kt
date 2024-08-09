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

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toMe
import com.kaleyra.video_sdk.call.mapper.AudioMapper.mapToAudioUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.mapToVideoUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

internal object StreamMapper {
    fun Call.toStreamsUi(): Flow<List<StreamUi>> =
        this.participants.flatMapLatest { participants ->
            val map = mutableMapOf<String, List<StreamUi>>()
            val participantsList = participants.list

            if (participantsList.isEmpty()) flowOf(listOf())
            else participantsList
                .map { participant ->
                    // TODO add the call participant state check?
//                combine(participant.streams, participant.state) { streams, state ->
//                    if (state == CallParticipant.State.InCall) streams
//                    else listOf()
//                }
                    participant.streams
                        .mapToStreamsUi(
                            isLocalParticipant = participant.userId == participants.me?.userId,
                            displayName = participant.combinedDisplayName,
                            displayImage = participant.combinedDisplayImage
                        )
                        .map {
                            Pair(participant.userId, it)
                        }
                }
                .merge()
                .transform { (userId, streams) ->
                    map[userId] = streams
                    val values = map.values.toList()
                    if (values.size == participants.list.size) {
                        emit(values.flatten())
                    }
                }
        }.distinctUntilChanged()

    fun Call.toMyStreamsUi(): Flow<List<StreamUi>> =
        this.participants
            .mapNotNull { it.me }
            .flatMapLatest { me ->
                me.streams.mapToStreamsUi(isLocalParticipant = true, me.combinedDisplayName, me.combinedDisplayImage)
            }
            .distinctUntilChanged()

    fun Flow<List<Stream>>.mapToStreamsUi(
        isLocalParticipant: Boolean,
        displayName: Flow<String?>,
        displayImage: Flow<Uri?>
    ): Flow<List<StreamUi>> =
         this.flatMapLatest { streams ->
            val map = mutableMapOf<String, StreamUi>()

            if (streams.isEmpty()) flowOf(listOf())
            else streams
                .map { stream ->
                    val id = stream.id
                    val video = stream.video.mapToVideoUi()
                    val audio = stream.audio.mapToAudioUi()

                    combine(
                        video,
                        audio,
                        displayName,
                        displayImage
                    ) { video, audio, name, image ->
                        StreamUi(
                            id = id,
                            video = video,
                            audio = audio,
                            username = name ?: "",
                            avatar = image?.let { ImmutableUri(it) },
                            isMine = isLocalParticipant
                        )
                    }
                }
                .merge()
                .transform { stream ->
                    map[stream.id] = stream
                    val values = map.values.toList()
                    if (values.size == streams.size) {
                        emit(values)
                    }
                }
        }.distinctUntilChanged()

    fun Call.doIHaveStreams(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

    fun Flow<List<StreamUi>>.hasAtLeastAVideoEnabled(): Flow<Boolean> =
        this.map { streams -> streams.map { it.video } }
            .map { video -> video.any { it?.isEnabled == true } }
            .distinctUntilChanged()

}