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

import android.app.Notification
import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.isAnyScreenInputActive
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.CallExtensions.isIncoming
import com.kaleyra.video_common_ui.utils.CallExtensions.isOngoing
import com.kaleyra.video_common_ui.utils.CallExtensions.isOutgoing
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

internal class CallNotificationProducer(private val coroutineScope: CoroutineScope = MainScope()) {

    companion object {
        /**
         * The global call notification id
         */
        const val CALL_NOTIFICATION_ID = 22

        internal suspend fun buildOutgoingCallNotification(
            participants: CallParticipants,
            activityClazz: Class<*>,
            isCallServiceRunning: Boolean = true
        ): Notification {
            val isGroupCall = participants.others.count() > 1
            val calleeDescription = participants.others.map {
                it.combinedDisplayName.filterNotNull().firstOrNull() ?: " "
            }.joinToString()
            return NotificationManager.buildOutgoingCallNotification(
                calleeDescription,
                isGroupCall,
                activityClazz,
                isCallServiceRunning = isCallServiceRunning
            )
        }

        internal suspend fun buildIncomingCallNotification(
            participants: CallParticipants,
            activityClazz: Class<*>,
            isCallServiceRunning: Boolean = true
        ): Notification {
            val context = ContextRetainer.context
            val isGroupCall = participants.others.count() > 1
            val callerDescription = participants.creator()?.combinedDisplayName?.filterNotNull()?.firstOrNull() ?: " "
            return NotificationManager.buildIncomingCallNotification(
                callerDescription,
                isGroupCall,
                activityClazz,
                isHighPriority = !AppLifecycle.isInForeground.value || context.isSilent(),
                isCallServiceRunning = isCallServiceRunning
            )
        }
    }

    interface Listener {
        fun onNewNotification(call: Call, notification: Notification, id: Int)
        fun onClearNotification(id: Int)
    }

    var listener: Listener? = null

    private var job: Job? = null

    fun bind(call: CallUI) {
        stop()
        job = combine(
            call.state,
            call.participants,
            call.recording,
            call.isAnyScreenInputActive()
        ) { callState, participants, recording, isAnyScreenInputActive ->
            ContactDetailsManager.refreshContactDetails(*participants.list.map { it.userId }.toTypedArray())

            val notification = buildNotification(
                callState,
                participants,
                recording,
                call.activityClazz,
                isAnyScreenInputActive
            )

            if (notification != null) {
                showNotification(call, notification)
            }
            callState
        }
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { clearNotification() }
            .launchIn(coroutineScope)
    }

    fun stop() {
        job?.cancel()
    }

    private fun showNotification(call: Call, notification: Notification) {
        NotificationManager.notify(CALL_NOTIFICATION_ID, notification)
        listener?.onNewNotification(call, notification, CALL_NOTIFICATION_ID)
    }

    private fun clearNotification() {
        NotificationManager.cancel(CALL_NOTIFICATION_ID)
        listener?.onClearNotification(CALL_NOTIFICATION_ID)
    }

    private suspend fun buildNotification(
        callState: Call.State,
        participants: CallParticipants,
        recording: Call.Recording,
        activityClazz: Class<*>,
        isAnyScreenInputActive: Boolean
    ): Notification? {
        return when {
            isIncoming(callState, participants) -> buildIncomingCallNotification(
                participants,
                activityClazz
            )

            isOutgoing(callState, participants) -> buildOutgoingCallNotification(
                participants,
                activityClazz
            )

            isOngoing(callState, participants) -> buildOngoingCallNotification(
                participants,
                recording,
                callState,
                isAnyScreenInputActive,
                activityClazz
            )

            else -> null
        }
    }

    private suspend fun buildOngoingCallNotification(
        participants: CallParticipants,
        recording: Call.Recording,
        callState: Call.State,
        isAnyScreenInputActive: Boolean,
        activityClazz: Class<*>
    ): Notification {
        val isGroupCall = participants.others.count() > 1
        val calleeDescription = participants.others.map {
            it.combinedDisplayName.filterNotNull().firstOrNull() ?: " "
        }.joinToString()
        return NotificationManager.buildOngoingCallNotification(
            calleeDescription,
            isLink = participants.creator() == null,
            isGroupCall,
            isCallRecorded = recording.type == Call.Recording.Type.OnConnect,
            isSharingScreen = isAnyScreenInputActive,
            callState is Call.State.Connecting,
            activityClazz
        )
    }
}
