package com.kaleyra.video_common_ui

import android.content.Context
import android.content.Intent
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver.Companion.ACTION_ANSWER
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver.Companion.ACTION_DECLINE
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver.Companion.ACTION_HANGUP
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver.Companion.ACTION_STOP_SCREEN_SHARE
import com.kaleyra.video_common_ui.notification.CallNotificationExtra
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallNotificationActionReceiverTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val receiver = spyk(CallNotificationActionReceiver(mainDispatcherRule.testDispatcher))

    private val contextMock = mockk<Context>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(
            KaleyraVideo,
            NotificationManager,
            ContextRetainer,
            ConnectionServiceUtils,
            ContextExtensions,
            KaleyraCallConnectionService
        )
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoKt")
        every { NotificationManager.cancel(any()) } returns Unit
        every { contextMock.goToLaunchingActivity() } returns Unit
        every { ContextRetainer.context } returns contextMock
        every { KaleyraVideo.onCallReady(any(), captureCoroutine()) } answers {
            coroutine<suspend (CallUI) -> Unit>().coInvoke(callMock)
        }
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns false
        coEvery { KaleyraCallConnectionService.answer() } returns Unit
        coEvery { KaleyraCallConnectionService.reject() } returns Unit
        coEvery { KaleyraCallConnectionService.hangUp() } returns Unit
        coEvery { receiver.requestConfigure() } returns true
        every { receiver.goAsync() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testRequestConfigurationFails() = runTest {
        coEvery { receiver.requestConfigure() } returns false

        receiver.onReceive(contextMock, Intent())

        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { contextMock.goToLaunchingActivity() }
    }

    @Test
    fun testActionHangUp() = runTest {
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_HANGUP)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testActionHangUpWithConnectionService() = runTest {
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns true
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_HANGUP)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        coVerify(exactly = 1) { KaleyraCallConnectionService.hangUp() }
    }

    @Test
    fun testActionDecline() = runTest {
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_DECLINE)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testActionDeclineWithConnectionService() = runTest {
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns true
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_DECLINE)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        coVerify(exactly = 1) { KaleyraCallConnectionService.reject() }
    }

    @Test
    fun testActionAnswer() = runTest {
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_ANSWER)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { callMock.connect() }
    }

    @Test
    fun testActionAnswerWithConnectionService() = runTest {
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns true
        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_ANSWER)
        }

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        coVerify(exactly = 1) { KaleyraCallConnectionService.answer() }
    }

    @Test
    fun testStopApplicationScreenShare() {
        testScreenShare(mockk<Input.Video.Application>(relaxed = true))
    }

    @Test
    fun testStopDeviceScreenShare() {
        testScreenShare(mockk<Input.Video.Screen.My>(relaxed = true))
    }

    private fun testScreenShare(screenShareVideo: Input.Video.My) = runTest {
        val callParticipantsMock = mockk<CallParticipants>(relaxed = true)
        val inputsMock = mockk<Inputs>(relaxed = true)
        val meMock = mockk<CallParticipant.Me>(relaxed = true)
        val myStreamMock = mockk<Stream.Mutable>(relaxed = true)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callMock.inputs } returns inputsMock
        every { callParticipantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { myStreamMock.video } returns MutableStateFlow(screenShareVideo)
        every { screenShareVideo.enabled } returns MutableStateFlow(true)
        every { screenShareVideo.tryDisable() } returns true

        val intent = Intent().apply {
            putExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA, ACTION_STOP_SCREEN_SHARE)
        }
        val availableInputs = setOf(screenShareVideo, mockk<Input.Video.Camera>())
        every { inputsMock.availableInputs } returns MutableStateFlow(availableInputs)

        receiver.onReceive(contextMock, intent)

        advanceUntilIdle()
        verify(exactly = 1) { screenShareVideo.tryDisable() }
        verify(exactly = 1) { meMock.removeStream(myStreamMock) }
    }

}