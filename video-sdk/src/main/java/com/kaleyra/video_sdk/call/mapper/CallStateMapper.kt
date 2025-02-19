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
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.areOtherParticipantsRinging
import com.kaleyra.video_common_ui.mapper.StreamMapper.doAnyOfMyStreamsIsLive
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

internal object CallStateMapper {

    fun Call.isConnected(): Flow<Boolean> =
        this.state
            .map { it is Call.State.Connected }
            .distinctUntilChanged()

    fun CallUI.toCallStateUi(): Flow<CallStateUi> {
        return combine(
            state,
            participants,
            doAnyOfMyStreamsIsLive(),
            areOtherParticipantsRinging()
        ) { state, participants, doAnyOfMyStreamsIsLive, areOtherParticipantsRinging ->
            when {
                state !is Call.State.Disconnected.Ended
                    && KaleyraVideo.connectedUser.value == null
                    || (state is Call.State.Connected && !doAnyOfMyStreamsIsLive) -> CallStateUi.Connecting
                isRingingLocally(state, participants, isLink) -> CallStateUi.Ringing
                isRingingRemotely(state, areOtherParticipantsRinging) -> CallStateUi.RingingRemotely
                isDialing(state, participants) -> CallStateUi.Dialing
                state is Call.State.Connected -> CallStateUi.Connected
                state is Call.State.Reconnecting -> CallStateUi.Reconnecting
                state is Call.State.Disconnecting -> CallStateUi.Disconnecting
                state is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
                state is Call.State.Disconnected.Ended.Declined -> CallStateUi.Disconnected.Ended.Declined
                state is Call.State.Disconnected.Ended.LineBusy -> CallStateUi.Disconnected.Ended.LineBusy
                state is Call.State.Disconnected.Ended.CurrentUserInAnotherCall -> CallStateUi.Disconnected.Ended.CurrentUserInAnotherCall
                state is Call.State.Disconnected.Ended.Timeout -> CallStateUi.Disconnected.Ended.Timeout
                state is Call.State.Disconnected.Ended.Error.Server -> CallStateUi.Disconnected.Ended.Error.Server
                state is Call.State.Disconnected.Ended.Error.Unknown -> CallStateUi.Disconnected.Ended.Error.Unknown
                state is Call.State.Disconnected.Ended.HungUp -> CallStateUi.Disconnected.Ended.HungUp
                state is Call.State.Disconnected.Ended.Kicked -> {
                    val admin = participants.others.firstOrNull { it.userId == state.userId }
                    val adminName = admin?.combinedDisplayName?.firstOrNull() ?: admin?.userId ?: ""
                    CallStateUi.Disconnected.Ended.Kicked(adminName)
                }

                state == Call.State.Disconnected.Ended.Error -> CallStateUi.Disconnected.Ended.Error
                state == Call.State.Disconnected.Ended -> CallStateUi.Disconnected.Ended
                else -> CallStateUi.Disconnected
            }
        }.distinctUntilChanged()
    }

    private fun isDialing(state: Call.State, participants: CallParticipants): Boolean =
        (participants.creator() == null || participants.me == participants.creator()) && state is Call.State.Connecting

    private fun isRingingLocally(state: Call.State, participants: CallParticipants, isLink: Boolean) =
        !isLink && participants.creator().let { it != null && it != participants.me } && (state == Call.State.Disconnected || state is Call.State.Connecting)

    private fun isRingingRemotely(state: Call.State, areOtherParticipantsRinging: Boolean) =
        (state == Call.State.Disconnected || state is Call.State.Connecting) && areOtherParticipantsRinging
}
