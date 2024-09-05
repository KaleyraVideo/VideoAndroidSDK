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
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.FlowUtils.flatMapLatestNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

/**
 * Utility functions for the call participants
 */
object ParticipantMapper {

    /**
     * Utility function to be used to receive a flag representing the current initialized state of the logged participants
     * @receiver Flow<Call> the call flow
     * @return Flow<Boolean> flow returning true when the logged user is initialized, false otherwise
     */
    fun Call.isMeParticipantInitialized(): Flow<Boolean> =
        this.participants.map { it.me != null }

    /**
     * Utility function to be used to receive a flag representing if the current call is a group call
     * @receiver Flow<Call> the call flow
     * @param companyId Flow<String> company identifier
     * @return Flow<Boolean> flow representing if if the current call is a group call, true if it is a group call, false otherwise
     */
    fun Call.isGroupCall(companyId: Flow<String>): Flow<Boolean> =
        combine(participants, companyId) { participants, companyId ->
            participants.others.filter { it.userId != companyId }.size > 1
        }.distinctUntilChanged()

    /**
     * Utility function to be used to receive the other participants display names as list
     * @receiver Flow<Call> the call flow
     * @return Flow<List<String>> flow representing the other participants display names as list
     */
    fun Call.toOtherDisplayNames(): Flow<List<String>> =
        this.participants
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, String>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.combinedDisplayName.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayName) ->
                        map[userId] = displayName ?: userId
                        val values = map.values.toList()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
            .distinctUntilChanged()

    /**
     * Utility function to be used to receive the other participants display images as list
     * @receiver Flow<Call> the call flow
     * @return Flow<List<Uri>> flow representing the other participants display images as list
     */
    fun Call.toOtherDisplayImages(): Flow<List<Uri>> =
        this.participants
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, Uri>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.combinedDisplayImage.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayImage) ->
                        map[userId] = displayImage ?: Uri.EMPTY
                        val values = map.values.toList()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
            .distinctUntilChanged()

    /**
     * Utility function to be used to retrieve current logged participant's state
     * @receiver Flow<Call> the call flow
     * @return Flow<CallParticipant.State> flow representing the current logged participant's state
     */
    fun Call.toMyParticipantState(): Flow<CallParticipant.State> =
        this.participants
            .flatMapLatestNotNull { it.me?.state }
            .distinctUntilChanged()
}
