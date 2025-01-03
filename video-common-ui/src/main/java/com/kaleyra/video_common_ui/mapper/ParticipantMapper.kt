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

package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

/**
 * Utility functions for the call participants
 */
object ParticipantMapper {

    /**
     * Utility function to retrieve my participant
     * @receiver Flow<Call> the call flow
     * @return Flow<CallParticipant.Me> flow emitting my participant whenever is available
     */
    fun Call.toMe(): Flow<CallParticipant.Me> =
        this.participants
            .mapNotNull { it.me }
            .distinctUntilChanged()

    /**
     * Utility function to retrieve the participants that are in-call
     * @receiver Flow<Call> the call flow
     * @return Flow<List<CallParticipant>> flow emitting all the participants that are in-call
     */
    fun Call.toInCallParticipants(): Flow<List<CallParticipant>> =
        this.participants
            .mapNotNull { participants -> participants.me?.let { Pair(it, participants.others) }}
            .flatMapLatest { (me, others) ->
                val inCallMap = mutableMapOf<String, CallParticipant>(me.userId to me)
                val notInCallMap = mutableMapOf<String, CallParticipant>()

                if (others.isEmpty()) flowOf<List<CallParticipant>>(listOf(me))
                else others
                    .map { participant ->
                        combine(participant.state, participant.streams) { state, streams ->
                            val isInCall = state == CallParticipant.State.InCall || streams.isNotEmpty()
                            Pair(participant, isInCall)
                        }
                    }
                    .merge()
                    .transform { (participant, isInCall) ->
                        if (isInCall) {
                            inCallMap[participant.userId] = participant
                            notInCallMap.remove(participant.userId)
                        } else {
                            notInCallMap[participant.userId] = participant
                            inCallMap.remove(participant.userId)
                        }
                        val values = (inCallMap.values + notInCallMap.values).toList()
                        if (values.size == others.size + 1) {
                            emit(inCallMap.values.toList())
                        }
                    }
            }
            .distinctUntilChanged()

    fun Call.areOtherParticipantsRinging(): Flow<Boolean> {
        return participants
            .map { it.others }
            .flatMapLatest { participants ->
                val map = mutableMapOf<String, CallParticipant.State>()
                if (participants.isEmpty()) flowOf( false)
                else participants
                    .map { participant ->
                        participant.state.map { participant.userId to it }
                    }
                    .merge()
                    .transform { (userId, state) ->
                        map[userId] = state
                        val values = map.values.toList()
                        if (values.size == participants.size) {
                            val isAnyRinging = values.any { it is CallParticipant.State.NotInCall.Ringing }
                            val isNoneInCall = values.none { it is CallParticipant.State.InCall }
                            emit(isAnyRinging && isNoneInCall)
                        }
                    }
            }
            .distinctUntilChanged()
    }
}