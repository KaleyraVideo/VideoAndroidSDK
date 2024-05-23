package com.kaleyra.video_common_ui

import android.app.Application
import android.content.Context
import android.telecom.TelecomManager
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.ScreenShareOverlayProducer
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableCallSounds
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile

@OptIn(ExperimentalCoroutinesApi::class)
internal object ConferenceUIExtensions {

    fun ConferenceUI.configureCallSounds(logger: PriorityLogger?, coroutineScope: CoroutineScope) {
        var soundScope: CoroutineScope? = null
        call
            .onEach { call ->
                soundScope?.cancel()
                soundScope = CoroutineScope(SupervisorJob(coroutineScope.coroutineContext[Job]))
                call.state
                    .takeWhile { it !is Call.State.Disconnected.Ended }
                    .onCompletion { soundScope?.cancel() }
                    .launchIn(coroutineScope)
                call.enableCallSounds(logger, soundScope!!)
            }
            .launchIn(coroutineScope)
    }

    fun ConferenceUI.configureScreenShareOverlayProducer(coroutineScope: CoroutineScope) {
        var screenShareOverlayProducer: ScreenShareOverlayProducer? = null

        call
            .onEach {
                screenShareOverlayProducer?.dispose()
                screenShareOverlayProducer = ScreenShareOverlayProducer(ContextRetainer.context as Application, coroutineScope)
                screenShareOverlayProducer!!.bind(it)
            }
            .onCompletion {
                screenShareOverlayProducer?.dispose()
                screenShareOverlayProducer = null
            }
            .launchIn(coroutineScope)

        call
            .flatMapLatest { it.state }
            .takeWhile { it != Call.State.Disconnected.Ended }
            .onCompletion {
                screenShareOverlayProducer?.dispose()
            }
            .launchIn(coroutineScope)
    }

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

    private fun showProvisionalCallNotification(call: Call, callActivityClazz: Class<*>, coroutineScope: CoroutineScope) {
        showCallNotification(call, callActivityClazz, coroutineScope)
        call.state
            .takeWhile { it !is Call.State.Disconnected.Ended }
            .onCompletion { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
            .launchIn(coroutineScope)
    }

    private fun showCallNotification(call: Call, callActivityClazz: Class<*>, coroutineScope: CoroutineScope) {
        combine(call.state, call.participants) { state, participants ->
            ContactDetailsManager.refreshContactDetails(*participants.list.map { it.userId }.toTypedArray())

            val notification = when {
                CallExtensions.isIncoming(state, participants) -> {
                    CallNotificationProducer.buildIncomingCallNotification(participants, callActivityClazz, isCallServiceRunning = false)
                }

                CallExtensions.isOutgoing(state, participants) -> {
                    CallNotificationProducer.buildOutgoingCallNotification(participants, callActivityClazz, isCallServiceRunning = false)
                }

                else -> return@combine
            }

            NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, notification)
        }
            .take(1)
            .launchIn(coroutineScope)
    }

    fun ConferenceUI.configureCallActivityShow(coroutineScope: CoroutineScope) {
        call
            .onEach { call ->
                if (call.state.value is Call.State.Disconnected.Ended) return@onEach
                when {
                    call.isLink -> call.showOnAppResumed(coroutineScope)
                    call.shouldShowAsActivity() -> call.show()
                }
            }
            .launchIn(coroutineScope)
    }

}