package com.kaleyra.video_common_ui

import android.content.Context
import android.telecom.TelecomManager
import android.widget.Toast
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

internal object ConferenceUIExtensions {

    fun ConferenceUI.configureCallServiceStart(activityClazz: Class<*>, logger: PriorityLogger?, coroutineScope: CoroutineScope) {
        call
            .onEach { call ->
                if (call.state.value is Call.State.Disconnected.Ended) return@onEach
                val context = ContextRetainer.context
                when {
                    !ConnectionServiceUtils.isConnectionServiceSupported || connectionServiceOption == ConnectionServiceOption.Disabled -> KaleyraCallService.start(logger)
                    context.hasConnectionServicePermissions() -> {
                        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                        telecomManager.addCall(call = call, logger)
                    }
                    else -> showProvisionalCallNotification(call, activityClazz, coroutineScope)
                }
            }
            .launchIn(coroutineScope)
    }

    private suspend fun showProvisionalCallNotification(call: Call, callActivityClazz: Class<*>, coroutineScope: CoroutineScope) {
        showCallNotification(call, callActivityClazz)
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
            .launchIn(coroutineScope)
    }

    private suspend fun showCallNotification(call: Call, callActivityClazz: Class<*>) {
        val participants = call.participants.value
        val state = call.state.value
        val notification = when {
            CallExtensions.isIncoming(state, participants) -> {
                CallNotificationProducer.buildIncomingCallNotification(participants, callActivityClazz, isCallServiceRunning = false)
            }
            CallExtensions.isOutgoing(state, participants) -> {
                CallNotificationProducer.buildOutgoingCallNotification(participants, callActivityClazz, isCallServiceRunning = false)
            }

            else -> return
        }

        NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, notification)
    }

    fun ConferenceUI.configureCannotJoinUrlToast(coroutineScope: CoroutineScope) {
        var currentCall: CallUI? = null
        call
            .onEach { call ->
                if (call.state.value is Call.State.Disconnected.Ended) return@onEach
                if (currentCall != null && currentCall?.state?.value !is Call.State.Disconnected.Ended && call.isLink) {
                    showCannotJoinUrlToast()
                    return@onEach
                }
                currentCall = call
            }
            .launchIn(coroutineScope)
    }

    private suspend fun showCannotJoinUrlToast() = withContext(Dispatchers.Main) {
        Toast.makeText(ContextRetainer.context, R.string.kaleyra_call_join_url_already_in_call_error, Toast.LENGTH_SHORT).show()
    }

    fun ConferenceUI.configureCallActivityShow(coroutineScope: CoroutineScope) {
        call
            .onEach { call ->
                if (call.state.value is Call.State.Disconnected.Ended || !call.shouldShowAsActivity()) return@onEach
                call.showOnAppResumed(coroutineScope)
            }
            .launchIn(coroutineScope)
    }
}