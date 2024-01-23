package com.kaleyra.video_common_ui.notification.call

import android.app.Notification
import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.ScreenShareOverlayProducer
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val incomingCallNotification = mockk<Notification>(relaxed = true)

    private val outgoingCallNotification = mockk<Notification>(relaxed = true)

    private val ongoingCallNotification = mockk<Notification>(relaxed = true)

    private val listener = spyk(object : CallNotificationProducer.Listener {
        override fun onNewNotification(call: Call, notification: Notification, id: Int) = Unit
        override fun onClearNotification(id: Int) = Unit
    })

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        mockkObject(ContextExtensions)
        mockkObject(NotificationManager)
        mockkObject(CallExtensions)
        mockkObject(ContextRetainer)
        mockkObject(DeviceUtils)
        mockkObject(AppLifecycle)
        with(NotificationManager) {
            every { buildIncomingCallNotification(any(), any(), any(), any(), any()) } returns incomingCallNotification
            every { buildOutgoingCallNotification(any(), any(), any(), any()) } returns outgoingCallNotification
            every { buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), any()) } returns ongoingCallNotification
            every { cancel(any()) } returns Unit
            every { notify(any(), any()) } returns Unit
        }
        every { ContextRetainer.context } returns contextMock
        every { DeviceUtils.isSmartGlass } returns false
        coEvery { ContactDetailsManager.refreshContactDetails(any(), any()) } returns Unit
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        with(CallExtensions) {
            every { isIncoming(any(), any()) } returns false
            every { isOutgoing(any(), any()) } returns false
            every { isOngoing(any(), any()) } returns false
        }
        with(callMock) {
            every { state } returns MutableStateFlow(Call.State.Connected)
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
            every { creator() } returns otherParticipantMock
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNotifyIncomingCall() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock)
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification("otherUsername", any(), this@CallNotificationProducerTest::class.java, any(), any()) }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingCallNotification) }
        verify(exactly = 1) { listener.onNewNotification(callMock, incomingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun groupCall_buildIncomingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), isGroupCall = true, any(), any(), any()) }
    }

    @Test
    fun oneToOneCall_buildIncomingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { participantsMock.others } returns listOf(otherParticipantMock)
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), isGroupCall = false, any(), any(), any()) }
    }

    @Test
    fun appInForeground_buildIncomingCallNotification_isHighPriorityIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = false, any()) }
    }

    @Test
    fun appInBackground_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = true, any()) }
    }

    @Test
    fun deviceIsSilent_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns true
        }
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = true, any()) }
    }

    @Test
    fun deviceIsNotSilent_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = false, any()) }
    }

    @Test
    fun deviceIsSmartglass_buildIncomingCallNotification_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun deviceIsSmartphone_buildIncomingCallNotification_callStyleEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun testNotifyOutgoingCall() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOutgoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock)
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification("otherUsername", any(), this@CallNotificationProducerTest::class.java, any()) }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, outgoingCallNotification) }
        verify(exactly = 1) { listener.onNewNotification(callMock, outgoingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun groupCall_buildOutgoingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOutgoing(any(), any()) } returns true
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), isGroupCall = true, any(), any()) }
    }

    @Test
    fun oneToOneCall_buildOutgoingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOutgoing(any(), any()) } returns true
        every { participantsMock.others } returns listOf(otherParticipantMock)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), isGroupCall = false, any(), any()) }
    }

    @Test
    fun deviceIsSmartglass_buildOutgoingCallNotification_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOutgoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun deviceIsSmartphone_buildOutgoingCallNotification_callStyleEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOutgoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns false
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun testNotifyOngoingCall() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock)
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification("otherUsername", any(), any(), any(), any(), any(), this@CallNotificationProducerTest::class.java, any()) }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, ongoingCallNotification) }
        verify(exactly = 1) { listener.onNewNotification(callMock, ongoingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun creatorIsNull_buildOngoingCallNotification_isLinkIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { participantsMock.creator() } returns null
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), isLink = true, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun creatorIsNotNull_buildOngoingCallNotification_isLinkIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { participantsMock.creator() } returns otherParticipantMock
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), isLink = false, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun groupCall_buildOngoingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), isGroupCall = true, any(), any(),  any(), any(), any()) }
    }

    @Test
    fun oneToOneCall_buildOngoingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { participantsMock.others } returns listOf(otherParticipantMock)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), isGroupCall = false, any(), any(),  any(), any(), any()) }
    }

    @Test
    fun callRecordingOnConnect_buildOngoingCallNotification_isCallRecordedTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), isCallRecorded = true, any(), any(), any(), any()) }
    }

    @Test
    fun callRecordingNotOnConnect_buildOngoingCallNotification_isCallRecordedFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { recordingMock.type } returns mockk(relaxed = true)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), isCallRecorded = false, any(), any(), any(), any()) }
    }

    @Test
    fun sharingScreenActive_buildOngoingCallNotification_isSharingScreenTrue() = runTest(UnconfinedTestDispatcher()) {
        val screenShareMock = mockk<Input.Video.Application>(relaxed = true)
        every { screenShareMock.state } returns MutableStateFlow(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenShareMock))
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), isSharingScreen = true, any(), any(), any()) }
    }

    @Test
    fun sharingScreenNotActive_buildOngoingCallNotification_isSharingScreenFalse() = runTest(UnconfinedTestDispatcher()) {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf())
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), isSharingScreen = false, any(), any(), any()) }
    }

    @Test
    fun callIsInConnectingState_buildOngoingCallNotification_isConnectingTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), isConnecting = true, any(), any()) }
    }

    @Test
    fun callIsNotInConnectingState_buildOngoingCallNotification_isConnectingFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { callMock.state } returns MutableStateFlow(mockk(relaxed = true))
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), isConnecting = false, any(), any()) }
    }

    @Test
    fun deviceIsSmartglass_buildOngoingCallNotification_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun deviceIsSmartphone_buildOngoingCallNotification_callStyleEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns false
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock)
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun testBind() = runTest(UnconfinedTestDispatcher()) {
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        callNotificationProducer.bind(callMock)
        TestCase.assertEquals(1, backgroundJob.children.count())
        assert(backgroundJob.children.all { it.isActive })
    }

    @Test
    fun testStop() = runTest(UnconfinedTestDispatcher()) {
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock)
        TestCase.assertEquals(1, backgroundJob.children.count())
        assert(backgroundJob.children.all { it.isActive })
        callNotificationProducer.stop()
        // check the two input jobs are cancelled
        assert(backgroundJob.children.all { it.isCancelled })
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { listener.onClearNotification(CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun testNotificationIsCancelledOnScopeCancel() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock)
        backgroundScope.cancel()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { listener.onClearNotification(CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

}