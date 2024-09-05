package com.kaleyra.video_common_ui.notification.payloadworker

import com.kaleyra.video.State
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushNotificationPayloadWorkerTest {

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk(relaxed = true)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testPushPayloadWorkerCreated_kaleyraPushReceived_notificationManaged() = runTest {
        mockkStatic(::requestConfiguration)
        mockkStatic(::requestConnect)
        every { requestConfiguration() } returns true
        coEvery { requestConnect(any()) } returns true
        val pushPayloadWorker = PushNotificationPayloadWorker(mockk(relaxed = true), mockk(relaxed = true))

        pushPayloadWorker.doWork()
        advanceUntilIdle()

        verify(exactly = 1) { requestConfiguration() }
        coVerify(exactly = 1) { requestConnect() }
    }
}