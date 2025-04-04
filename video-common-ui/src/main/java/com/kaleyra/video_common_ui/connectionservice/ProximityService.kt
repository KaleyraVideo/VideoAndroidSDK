package com.kaleyra.video_common_ui.connectionservice

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bandyer.android_audiosession.session.AudioCallSession
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.proximity.AudioCallSessionAudioProximityDelegate
import com.kaleyra.video_common_ui.proximity.CallProximityDelegate
import com.kaleyra.video_common_ui.proximity.ConnectionAudioProximityDelegate
import com.kaleyra.video_common_ui.proximity.ProximityCallActivity
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.texttospeech.AwaitingParticipantsTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallParticipantMutedTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallRecordingTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.TextToSpeechNotifier
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class ProximityService : LifecycleService(), ActivityLifecycleCallbacks {

    companion object {

        internal var connection: KaleyraCallConnection? = null

        fun start(connection: KaleyraCallConnection? = null) = with(ContextRetainer.context) {
            val intent = Intent(this, ProximityService::class.java)
            startService(intent)
            this@Companion.connection = connection
        }

        fun stop() = with(ContextRetainer.context) {
            connection = null
            stopService(Intent(this, ProximityService::class.java))
        }
    }

    var proximityCallActivity: ProximityCallActivity? = null
        private set

    private var proximityDelegate: CallProximityDelegate<LifecycleService>? = null

    private var recordingTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var awaitingParticipantsTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var mutedTextToSpeechNotifier: TextToSpeechNotifier? = null

    private var callActivityClazz: Class<*>? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (!isProximityCallActivity(activity)) return
        proximityCallActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (!isProximityCallActivity(activity)) return
        proximityCallActivity = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        MainScope().launch {
            val hasConfigured = requestConfiguration()
            if (!hasConfigured) {
                stopSelf()
                return@launch
            }
            KaleyraVideo.onCallReady(lifecycleScope) { call ->
                application.registerActivityLifecycleCallbacks(this@ProximityService)
                callActivityClazz = call.activityClazz
                combine(call.state, call.participants) { st, pa -> CallExtensions.isIncoming(st, pa) }
                    .onEach {
                        // if the call is incoming, don't immediately bind the proximity
                        if (it) return@onEach
                        setUpProximityDelegate(call)
                        setUpRecordingTextToSpeech(call, proximityDelegate!!)
                        setUpMutedTextToSpeech(call, proximityDelegate!!)
                        setUpAwaitingParticipantsTextToSpeech(call, proximityDelegate!!)
                    }
                    .takeWhile { it }
                    .launchIn(lifecycleScope)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterActivityLifecycleCallbacks(this)
        disposeAwaitingParticipantsTextToSpeech()
        disposeRecordingTextToSpeech()
        disposeMutedTextToSpeech()
        disposeProximityDelegate()
        proximityCallActivity = null
        callActivityClazz = null
    }

    private fun setUpProximityDelegate(call: CallUI) {
        proximityDelegate = CallProximityDelegate<LifecycleService>(
            lifecycleContext = this,
            call = call,
            disableProximity = { proximityCallActivity?.disableProximity ?: false },
            audioProximityDelegate = connection?.let { ConnectionAudioProximityDelegate(it) }
                ?: AudioCallSessionAudioProximityDelegate(AudioCallSession.getInstance())
        ).apply { bind() }
    }

    private fun disposeProximityDelegate() {
        proximityDelegate?.destroy()
        proximityDelegate = null
    }

    private fun setUpRecordingTextToSpeech(
        call: CallUI,
        proximityDelegate: CallProximityDelegate<LifecycleService>
    ) {
        recordingTextToSpeechNotifier = CallRecordingTextToSpeechNotifier(call, proximityDelegate.sensor!!).apply {
            start(lifecycleScope)
        }
    }

    private fun disposeRecordingTextToSpeech() {
        recordingTextToSpeechNotifier?.dispose()
        recordingTextToSpeechNotifier = null
    }

    private fun setUpMutedTextToSpeech(
        call: CallUI,
        proximityDelegate: CallProximityDelegate<LifecycleService>
    ) {
        mutedTextToSpeechNotifier = CallParticipantMutedTextToSpeechNotifier(call, proximityDelegate.sensor!!).apply {
            start(lifecycleScope)
        }
    }

    private fun disposeMutedTextToSpeech() {
        mutedTextToSpeechNotifier?.dispose()
        mutedTextToSpeechNotifier = null
    }

    private fun setUpAwaitingParticipantsTextToSpeech(
        call: CallUI,
        proximityDelegate: CallProximityDelegate<LifecycleService>
    ) {
        awaitingParticipantsTextToSpeechNotifier = AwaitingParticipantsTextToSpeechNotifier(call, proximityDelegate.sensor!!).apply {
            start(lifecycleScope)
        }
    }

    private fun disposeAwaitingParticipantsTextToSpeech() {
        awaitingParticipantsTextToSpeechNotifier?.dispose()
        awaitingParticipantsTextToSpeechNotifier = null
    }

    @OptIn(ExperimentalContracts::class)
    private fun isProximityCallActivity(activity: Activity): Boolean {
        contract {
            returns(true) implies (activity is ProximityCallActivity)
        }
        return activity::class.java == callActivityClazz && activity is ProximityCallActivity
    }

}