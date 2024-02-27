package com.kaleyra.video_common_ui.callservice

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.BaseLogger
import com.kaleyra.video_utils.logging.PriorityLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class KaleyraCallServiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private var service: KaleyraCallService? = null

    private var notificationBuilder: Notification.Builder? = null

    private var notificationManager = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val callMock = mockk<CallUI>(relaxed = true)

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val logger = object : PriorityLogger(BaseLogger.VERBOSE) {
        override fun debug(tag: String, message: String) = Unit
        override fun error(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun verbose(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
    }

    @Before
    fun setup() {
        service = Robolectric.setupService(KaleyraCallService::class.java)
        KaleyraCallService.logger = logger
        notificationBuilder = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("Test")
            .setContentText("content text")
        mockkConstructor(CallForegroundServiceWorker::class)
        mockkObject(KaleyraVideo)
        mockkObject(CallExtensions)
        mockkObject(CollaborationAudioExtensions)
        mockkObject(ContextRetainer)
        every { anyConstructed<CallForegroundServiceWorker>().bind(any(), any()) } returns Unit
        every { anyConstructed<CallForegroundServiceWorker>().dispose() } returns Unit
        every { KaleyraVideo.conference } returns conferenceMock
        with(callMock) {
            every { shouldShowAsActivity() } returns false
            every { showOnAppResumed(any()) } returns Unit
            every { enableAudioRouting(logger = any(), any(), any()) } returns Unit
            every { disableAudioRouting() } returns Unit
        }
        with(conferenceMock) {
            every { call } returns MutableStateFlow(callMock)
        }
    }

    @After
    fun tearDown() {
        KaleyraCallService.logger = null
        unmockkAll()
    }

    @Test
    fun testStartService() {
        val newLogger = object : PriorityLogger(BaseLogger.VERBOSE) {
            override fun debug(tag: String, message: String) = Unit
            override fun error(tag: String, message: String) = Unit
            override fun info(tag: String, message: String) = Unit
            override fun verbose(tag: String, message: String) = Unit
            override fun warn(tag: String, message: String) = Unit
        }
        every { ContextRetainer.context } returns service!!.applicationContext
        KaleyraCallService.start(newLogger)
        val startedIntent: Intent = shadowOf(service!!).nextStartedService
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(KaleyraCallService::class.java, shadowIntent.intentClass)
        assertEquals(newLogger, KaleyraCallService.logger)
    }

    @Test
    fun testStopService() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns service!!.applicationContext
        KaleyraCallService.stop()
        val startedIntent: Intent = shadowOf(service!!).nextStoppedService
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(KaleyraCallService::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testOnStartCommand() {
        val conferenceMock = mockk<ConferenceUI>(relaxed = true)
        val callMock = mockk<CallUI>(relaxed = true)
        every { KaleyraVideo.conference } returns conferenceMock
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.enableAudioRouting(logger = any(), any(), any()) } returns Unit
        every { callMock.isLink } returns true
        val startType = service!!.onStartCommand(null, 0, 0)
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().bind(service!!, callMock) }
        verify(exactly = 1) {
            callMock.enableAudioRouting(logger, service!!.lifecycleScope, true)
        }
        assertEquals(Service.START_STICKY, startType)
    }

    @Test
    fun testOnDestroy() {
        service!!.onStartCommand(null, 0, 0)
        service!!.onDestroy()
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().dispose() }
        verify(exactly = 1) { callMock.disableAudioRouting() }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testOnNewNotification() {
        mockkObject(AppLifecycle)
        val notification = notificationBuilder!!.build()
        val callMock = mockk<Call>(relaxed = true)
        val isInForegroundFlow = MutableStateFlow(false)
        every { AppLifecycle.isInForeground } returns isInForegroundFlow
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(null, shadowOf(service).lastForegroundNotification)
        assertEquals(0, shadowOf(service).lastForegroundNotificationId)
        assertEquals(null, shadowOf(notificationManager).getNotification(10))
        assertEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)

        // when the value is set to true, the service is promoted to foreground
        isInForegroundFlow.value = true
        assertEquals(notification, shadowOf(service).lastForegroundNotification)
        assertEquals(10, shadowOf(service).lastForegroundNotificationId)
        assertEquals(notification, shadowOf(notificationManager).getNotification(10))
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
        unmockkObject(AppLifecycle)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testStartForegroundIsExecutedOnNotificationUpdate() {
        mockkObject(AppLifecycle)
        val notification = notificationBuilder!!.build()
        val callMock = mockk<Call>(relaxed = true)
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, shadowOf(service).lastForegroundNotification)
        assertEquals(10, shadowOf(service).lastForegroundNotificationId)

        val newNotification = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("new test")
            .setContentText("new content text")
            .build()
        service!!.onNewNotification(callMock, newNotification, 10)
        assertEquals(newNotification, shadowOf(service).lastForegroundNotification)
        assertEquals(10, shadowOf(service).lastForegroundNotificationId)
        unmockkObject(AppLifecycle)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun testOnNewNotificationWithForegroundServiceType() {
        mockkObject(AppLifecycle)
        val notification = notificationBuilder!!.build()
        val callMock = mockk<Call>(relaxed = true)
        val isInForegroundFlow = MutableStateFlow(false)
        every { AppLifecycle.isInForeground } returns isInForegroundFlow
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(null, shadowOf(service).lastForegroundNotification)
        assertEquals(0, shadowOf(service).lastForegroundNotificationId)
        assertEquals(null, shadowOf(notificationManager).getNotification(10))
        assertEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)

        // when the value is set to true, the service is promoted to foreground
        isInForegroundFlow.value = true
        assertEquals(notification, shadowOf(service).lastForegroundNotification)
        assertEquals(10, shadowOf(service).lastForegroundNotificationId)
        assertEquals(notification, shadowOf(notificationManager).getNotification(10))
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
        assertEquals(service!!.getForegroundServiceType(false), service!!.foregroundServiceType)
        unmockkObject(AppLifecycle)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M, Build.VERSION_CODES.N])
    fun testOnClearNotification() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onClearNotification(10)
        assertEquals(true, shadowOf(service).isForegroundStopped)
        assertEquals(true, shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnDestroyRemovesNotification() {
        val n: Notification = notificationBuilder!!.build()
        service!!.startForeground(10, n)
        service!!.onDestroy()
        assertEquals(null, shadowOf(notificationManager).getNotification(10))
    }
}