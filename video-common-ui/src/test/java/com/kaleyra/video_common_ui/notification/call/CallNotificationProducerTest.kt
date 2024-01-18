package com.kaleyra.video_common_ui.notification.call

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallNotificationProducerTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val contextMock = mockk<Context>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val otherParticipantMock = mockk<CallParticipant>(relaxed = true)

    private val meParticipantMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val recordingMock = mockk<Call.Recording>(relaxed = true)

    private val inputsMock = mockk<Inputs>(relaxed = true)

    private val screenShareMock = mockk<Input.Video.Screen.My>()

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        mockkObject(ContextExtensions)
        mockkObject(NotificationManager)
        mockkObject(ContextRetainer)
        mockkObject(DeviceUtils)
        mockkObject(AppLifecycle)
        with(NotificationManager) {
            every { buildIncomingCallNotification(any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
            every { buildOutgoingCallNotification(any(), any(), any(), any()) } returns mockk(relaxed = true)
            every { buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
            every { cancel(any()) } returns mockk(relaxed = true)
            every { notify(any(), any()) } returns mockk(relaxed = true)
        }
        every { ContextRetainer.context } returns contextMock
        every { DeviceUtils.isSmartGlass } returns false
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        coEvery { ContactDetailsManager.refreshContactDetails(any(), any()) } returns Unit
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        with(callMock) {
            every { activityClazz } returns this@CallNotificationProducerTest::class.java
            every { recording } returns MutableStateFlow(recordingMock)
            every { inputs } returns inputsMock
            every { participants } returns MutableStateFlow(participantsMock)
        }
        with(recordingMock) {
            every { type } returns Call.Recording.Type.OnConnect
            every { state } returns MutableStateFlow(Call.Recording.State.Started)
        }
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf())
        with(otherParticipantMock) {
            every { userId } returns "otherUserId"
            every { combinedDisplayName } returns MutableStateFlow("otherUsername")
        }
        with(meParticipantMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myUsername")
        }
        with(participantsMock) {
            every { list } returns listOf(otherParticipantMock, meParticipantMock)
            every { others } returns listOf(otherParticipantMock)
            every { me } returns meParticipantMock
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNotifyIncomingCall() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { participantsMock.creator() } returns otherParticipantMock
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), any()) }
        verify(exactly = 1) { NotificationManager.notify(any(), any()) }
    }

    @Test
    fun testNotifyOutgoingCall() = runTest {

    }

    @Test
    fun testNotifyOngoingCall() = runTest {

    }

//    @Test
//    fun testNotificationNotShownIfFileShareIsVisible() = runTest {
//        val fileShareNotificationProducer = FileShareNotificationProducer(this)
//        fileShareNotificationProducer.bind(call)
//        advanceUntilIdle()
//        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
//        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
//        coroutineContext.cancelChildren()
//    }
//
//    @Test
//    fun testUploadIsNotNotified() = runTest {
//        val fileShareNotificationProducer = FileShareNotificationProducer(this)
//        every { call.sharedFolder } returns mockk {
//            every { files } returns MutableStateFlow(setOf(uploadFile))
//        }
//        fileShareNotificationProducer.bind(call)
//        advanceUntilIdle()
//        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
//        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
//        coroutineContext.cancelChildren()
//    }
//
//    @Test
//    fun testNotificationIsCancelledOnScopeCancel() = runTest {
//        val fileShareNotificationProducer = FileShareNotificationProducer(this)
//        fileShareNotificationProducer.bind(call)
//        advanceUntilIdle()
//        coroutineContext.cancelChildren()
//        coroutineContext.job.children.first().join()
//        verify { NotificationManager.cancel("downloadId".hashCode()) }
//    }
//
//    @Test
//    fun testStop() = runTest {
//        val fileShareNotificationProducer = spyk(FileShareNotificationProducer(this))
//        fileShareNotificationProducer.bind(call)
//        fileShareNotificationProducer.stop()
//    }
}