package com.kaleyra.video_common_ui.callservice

import android.app.Notification
import android.app.Service
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.CameraStreamManager
import com.kaleyra.video_common_ui.call.ParticipantManager
import com.kaleyra.video_common_ui.call.ScreenShareOverlayProducer
import com.kaleyra.video_common_ui.call.StreamsManager
import com.kaleyra.video_common_ui.connectionservice.ProximityService
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.DeviceUtils
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallForegroundServiceWorkerTest {

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ProximityService)
        mockkObject(DeviceUtils)
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoKt")
        mockkConstructor(CallNotificationProducer::class)
        mockkConstructor(FileShareNotificationProducer::class)
        mockkConstructor(ScreenShareOverlayProducer::class)
        mockkConstructor(CameraStreamManager::class)
        mockkConstructor(StreamsManager::class)
        mockkConstructor(ParticipantManager::class)
        every { KaleyraVideo.onCallReady(any(), captureLambda()) } answers {
            lambda<(CallUI) -> Unit>().invoke(callMock)
        }
        every { anyConstructed<CallNotificationProducer>().bind(callMock) } returns Unit
        every { anyConstructed<FileShareNotificationProducer>().bind(callMock) } returns Unit
        every { anyConstructed<ScreenShareOverlayProducer>().bind(callMock) } returns Unit
        every { anyConstructed<CameraStreamManager>().bind(callMock) } returns Unit
        every { anyConstructed<StreamsManager>().bind(callMock) } returns Unit
        every { anyConstructed<ParticipantManager>().bind(callMock) } returns Unit
        every { ProximityService.start() } returns Unit
        every { ProximityService.stop() } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBindOnSmartphone() = runTest {
        every { DeviceUtils.isSmartGlass } returns false
        val listener = object : CallNotificationProducer.Listener {
            override fun onNewNotification(call: Call, notification: Notification, id: Int) = Unit
            override fun onClearNotification(id: Int) = Unit
        }
        var call: CallUI? = null
        val callForegroundServiceWorker = CallForegroundServiceWorker(mockk(relaxed = true), this, listener)
        callForegroundServiceWorker.bind(mockk(relaxed = true)) {
            call = it
        }
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<CameraStreamManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<StreamsManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<ParticipantManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<FileShareNotificationProducer>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<ScreenShareOverlayProducer>().bind(callMock) }
        verify(exactly = 1) { ProximityService.start() }
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>() setProperty "listener" value listener }
        assertEquals(callMock, call)
    }

    @Test
    fun testBindOnSmartglasses() = runTest {
        every { DeviceUtils.isSmartGlass } returns true
        val listener = object : CallNotificationProducer.Listener {
            override fun onNewNotification(call: Call, notification: Notification, id: Int) = Unit
            override fun onClearNotification(id: Int) = Unit
        }
        var call: CallUI? = null
        val callForegroundServiceWorker = CallForegroundServiceWorker(mockk(relaxed = true), this, listener)
        callForegroundServiceWorker.bind(mockk(relaxed = true)) {
            call = it
        }
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<CameraStreamManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<StreamsManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<ParticipantManager>().bind(callMock) }
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>() setProperty "listener" value listener }
        verify(exactly = 0) { anyConstructed<FileShareNotificationProducer>().bind(callMock) }
        verify(exactly = 0) { anyConstructed<ScreenShareOverlayProducer>().bind(callMock) }
        verify(exactly = 0) { ProximityService.start() }
        assertEquals(callMock, call)
    }

    @Test
    fun testStopOnSmartphone() = runTest {
        every { DeviceUtils.isSmartGlass } returns false
        val callForegroundServiceWorker = CallForegroundServiceWorker(mockk(relaxed = true), this, mockk(relaxed = true))
        callForegroundServiceWorker.bind(mockk(relaxed = true))
        callForegroundServiceWorker.dispose()
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>().stop() }
        verify(exactly = 1) { anyConstructed<CameraStreamManager>().stop() }
        verify(exactly = 1) { anyConstructed<StreamsManager>().stop() }
        verify(exactly = 1) { anyConstructed<ParticipantManager>().stop() }
        verify(exactly = 1) { anyConstructed<FileShareNotificationProducer>().stop() }
        verify(exactly = 1) { anyConstructed<ScreenShareOverlayProducer>().stop() }
        verify(exactly = 1) { ProximityService.stop() }
    }

    @Test
    fun testStopOnSmartglasses() = runTest {
        every { DeviceUtils.isSmartGlass } returns true
        val callForegroundServiceWorker = CallForegroundServiceWorker(mockk(relaxed = true), this, mockk(relaxed = true))
        callForegroundServiceWorker.bind(mockk(relaxed = true))
        callForegroundServiceWorker.dispose()
        verify(exactly = 1) { callMock.end() }
        verify(exactly = 1) { anyConstructed<CallNotificationProducer>().stop() }
        verify(exactly = 1) { anyConstructed<CameraStreamManager>().stop() }
        verify(exactly = 1) { anyConstructed<StreamsManager>().stop() }
        verify(exactly = 1) { anyConstructed<ParticipantManager>().stop() }
        verify(exactly = 0) { anyConstructed<FileShareNotificationProducer>().stop() }
        verify(exactly = 0) { anyConstructed<ScreenShareOverlayProducer>().stop() }
        verify(exactly = 0) { ProximityService.stop() }
    }

    @Test
    fun serviceStoppedOnCallEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        val service = mockk<Service>(relaxed = true)
        val callForegroundServiceWorker = CallForegroundServiceWorker(mockk(relaxed = true), this, mockk(relaxed = true))
        callForegroundServiceWorker.bind(service)
        advanceUntilIdle()
        verify(exactly = 1) { service.stopSelf() }
    }
}