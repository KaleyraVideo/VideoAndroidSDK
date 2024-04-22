package com.kaleyra.video_common_ui

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.TestUtils.getPrivateField
import com.kaleyra.video_common_ui.connectionservice.ProximityService
import com.kaleyra.video_common_ui.proximity.CallProximityDelegate
import com.kaleyra.video_common_ui.proximity.ProximityCallActivity
import com.kaleyra.video_common_ui.texttospeech.AwaitingParticipantsTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallParticipantMutedTextToSpeechNotifier
import com.kaleyra.video_common_ui.texttospeech.CallRecordingTextToSpeechNotifier
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.coInvoke
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProximityServiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private var service: ProximityService? = null

    private val callMock = mockk<CallUI>(relaxed = true)

    private val sensorMock = mockk<ProximitySensor>(relaxed = true)

    @Before
    fun setup() {
        service = Robolectric.setupService(ProximityService::class.java)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        mockkConstructor(CallProximityDelegate::class)
        mockkConstructor(CallRecordingTextToSpeechNotifier::class)
        mockkConstructor(AwaitingParticipantsTextToSpeechNotifier::class)
        mockkConstructor(CallParticipantMutedTextToSpeechNotifier::class)
        mockkObject(CallExtensions)
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoKt")
        every { KaleyraVideo.onCallReady(any(), captureCoroutine()) } answers {
            coroutine<suspend (CallUI) -> Unit>().coInvoke(callMock)
        }
        every { anyConstructed<CallProximityDelegate<LifecycleService>>().bind() } returns Unit
        every { anyConstructed<CallProximityDelegate<LifecycleService>>().destroy() } returns Unit
        every { anyConstructed<CallProximityDelegate<LifecycleService>>().sensor } returns sensorMock
        every { anyConstructed<CallRecordingTextToSpeechNotifier>().start(any()) } returns Unit
        every { anyConstructed<CallRecordingTextToSpeechNotifier>().dispose() } returns Unit
        every { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().start(any()) } returns Unit
        every { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().dispose() } returns Unit
        every { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().start(any()) } returns Unit
        every { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().dispose() } returns Unit
        every { CallExtensions.isIncoming(any(), any()) } returns false
        with(callMock) {
            every { state } returns MutableStateFlow(Call.State.Disconnected)
            every { participants } returns MutableStateFlow(mockk(relaxed = true))
        }
    }

    @Test
    fun testStartService() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns service!!.applicationContext
        ProximityService.start()
        val startedIntent: Intent = Shadows.shadowOf(service!!).nextStartedService
        val shadowIntent = Shadows.shadowOf(startedIntent)
        assertEquals(ProximityService::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testStopService() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns service!!.applicationContext
        ProximityService.stop()
        val startedIntent: Intent = Shadows.shadowOf(service!!).nextStoppedService
        val shadowIntent = Shadows.shadowOf(startedIntent)
        assertEquals(ProximityService::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testOnStartCommandWhenCallIsIncomingState() {
        val callState = MutableStateFlow<Call.State>(Call.State.Disconnected)
        every { callMock.state } returns callState
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val startType = service!!.onStartCommand(null, 0, 0)
        val lifecycleCallbacks = getRegisteredActivityLifecycleCallbacks()
        assertEquals(Service.START_STICKY, startType)
        assertEquals(service!!, lifecycleCallbacks.first())
        verify(exactly = 0) { anyConstructed<CallProximityDelegate<LifecycleService>>().bind() }
        verify(exactly = 0) { anyConstructed<CallRecordingTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 0) { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 0) { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        assert(service!!.lifecycleScope.coroutineContext.job.children.any { it.isActive })

        // when the call is no more in the incoming state, bind the proximity
        every { CallExtensions.isIncoming(any(), any()) } returns false
        callState.value = Call.State.Connected
        verify(exactly = 1) { anyConstructed<CallProximityDelegate<LifecycleService>>().bind() }
        verify(exactly = 1) { anyConstructed<CallRecordingTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 1) { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 1) { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        assert(service!!.lifecycleScope.coroutineContext.job.children.all { it.isCompleted })
    }

    @Test
    fun testOnStartCommand() {
        every { CallExtensions.isIncoming(any(), any()) } returns false
        val startType = service!!.onStartCommand(null, 0, 0)
        val lifecycleCallbacks = getRegisteredActivityLifecycleCallbacks()
        assertEquals(Service.START_STICKY, startType)
        assertEquals(service!!, lifecycleCallbacks.first())
        verify(exactly = 1) { anyConstructed<CallProximityDelegate<LifecycleService>>().bind() }
        verify(exactly = 1) { anyConstructed<CallRecordingTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 1) { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        verify(exactly = 1) { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().start(service!!.lifecycleScope) }
        assert(service!!.lifecycleScope.coroutineContext.job.children.all { it.isCompleted })
    }

    @Test
    fun testOnDestroy() {
        service!!.onStartCommand(null, 0, 0)
        service!!.onDestroy()
        val lifecycleCallbacks = getRegisteredActivityLifecycleCallbacks()
        assertEquals(null, lifecycleCallbacks.firstOrNull())
        verify(exactly = 1) { anyConstructed<CallProximityDelegate<LifecycleService>>().destroy() }
        verify(exactly = 1) { anyConstructed<CallRecordingTextToSpeechNotifier>().dispose() }
        verify(exactly = 1) { anyConstructed<AwaitingParticipantsTextToSpeechNotifier>().dispose() }
        verify(exactly = 1) { anyConstructed<CallParticipantMutedTextToSpeechNotifier>().dispose() }
    }

    @Test
    fun testOnActivityCreated() {
        val callActivity = object : Activity(), ProximityCallActivity {
            override val disableProximity: Boolean = false
        }
        every { callMock.activityClazz } returns callActivity::class.java
        service!!.onStartCommand(null, 0, 0)
        service!!.onActivityCreated(callActivity, null)
        assertEquals(callActivity, service!!.proximityCallActivity)
    }

    @Test
    fun testOnActivityDestroyed() {
        val callActivity = object : Activity(), ProximityCallActivity {
            override val disableProximity: Boolean = false
        }
        every { callMock.activityClazz } returns callActivity::class.java
        service!!.onStartCommand(null, 0, 0)
        service!!.onActivityCreated(callActivity, null)
        service!!.onActivityDestroyed(callActivity)
        assertEquals(null, service!!.proximityCallActivity)
    }

    private fun getRegisteredActivityLifecycleCallbacks(): ArrayList<Application.ActivityLifecycleCallbacks> {
        return ApplicationProvider
            .getApplicationContext<Application>()
            .getPrivateField<Application, ArrayList<Application.ActivityLifecycleCallbacks>>("mActivityLifecycleCallbacks")
    }
}