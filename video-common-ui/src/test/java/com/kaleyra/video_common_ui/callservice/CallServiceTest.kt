package com.kaleyra.video_common_ui.callservice

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class CallServiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private var service: CallService? = null

    private var notificationBuilder: Notification.Builder? = null

    private var notificationManager = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Before
    fun setup() {
        service = Robolectric.setupService(CallService::class.java)
        notificationBuilder = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("Test")
            .setContentText("content text")
        mockkConstructor(CallForegroundServiceWorker::class)
        every { anyConstructed<CallForegroundServiceWorker>().bind(any(), any()) } returns Unit
        every { anyConstructed<CallForegroundServiceWorker>().dispose() } returns Unit
    }

    @Test
    fun testStartService() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns service!!.applicationContext
        CallService.start()
        val startedIntent: Intent = shadowOf(service!!).nextStartedService
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(CallService::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testStopService() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns service!!.applicationContext
        CallService.stop()
        val startedIntent: Intent = shadowOf(service!!).nextStoppedService
        val shadowIntent = shadowOf(startedIntent)
        assertEquals(CallService::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testOnStartCommand() {
        mockkObject(KaleyraVideo)
        val conferenceMock = mockk<ConferenceUI>(relaxed = true)
        val callMock = mockk<CallUI>(relaxed = true)
        every { KaleyraVideo.conference } returns conferenceMock
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        val startType = service!!.onStartCommand(null, 0, 0)
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().bind(service!!, callMock) }
        assertEquals(Service.START_STICKY, startType)
    }

    @Test
    fun testOnDestroy() {
        service!!.onDestroy()
        verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().dispose() }
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