package com.kaleyra.video_common_ui.notification.call

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.Looper
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
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.canUseFullScreenIntentCompat
import com.kaleyra.video_common_ui.utils.mockSdkInt
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallNotificationProducerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

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

    private val clazz = this::class.java

    @Before
    fun setUp() {
        mockkConstructor(Handler::class)
        mockkStatic(Looper::getMainLooper)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        mockkObject(ContactDetailsManager)
        mockkObject(ContextExtensions)
        mockkObject(CallExtensions)
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns contextMock
        val notificationManagerMock = mockk<android.app.NotificationManager>(relaxed = true)
        every { contextMock.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManagerMock
        mockkObject(DeviceUtils)
        mockkObject(AppLifecycle)
        mockkObject(NotificationManager)
        with(NotificationManager) {
            every { buildIncomingCallNotification(any(), any(), any(), any(), any(), any()) } returns incomingCallNotification
            every { buildOutgoingCallNotification(any(), any(), any(), any(), any()) } returns outgoingCallNotification
            every { buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), any()) } returns ongoingCallNotification
            every { cancel(any()) } returns Unit
            every { notify(any(), any()) } returns Unit
        }
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
        mockkObject(CallNotificationProducer) {
            every { CallExtensions.isIncoming(any(), any()) } returns true
            every { CallExtensions.isOutgoing(any(), any()) } returns false
            every { CallExtensions.isOngoing(any(), any()) } returns false
            every { contextMock.canUseFullScreenIntentCompat() } returns true
            coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any(), any()) } returns incomingCallNotification
            val callNotificationProducer = CallNotificationProducer(backgroundScope)
            callNotificationProducer.listener = listener
            callNotificationProducer.bind(callMock, mockk(relaxed = true))
            val participants = callMock.participants.value
            val activityClazz = callMock.activityClazz
            coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
            coVerify(exactly = 1) { CallNotificationProducer.buildIncomingCallNotification(participants, activityClazz, true, true) }
            verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingCallNotification) }
            verify(exactly = 1) { listener.onNewNotification(callMock, incomingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
        }
    }

    @Test
    fun groupCall_buildIncomingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = false)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), isGroupCall = true, any(), any(), any(), any()) }
    }

    @Test
    fun oneToOneCall_buildIncomingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { participantsMock.others } returns listOf(otherParticipantMock)
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = false)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), isGroupCall = false, any(), any(), any(), any()) }
    }

    @Test
    fun appInForeground_buildIncomingCallNotification_isHighPriorityIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = false)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = false, any(), any()) }
    }

    @Test
    fun appInBackground_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = true, any(), enableCallStyle = true) }
    }

    @Test
    fun deviceIsSilent_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns true
        }
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = true, any(), enableCallStyle = true) }
    }

    @Test
    fun deviceIsNotSilent_buildIncomingCallNotification_isHighPriorityIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        with(ContextExtensions) {
            every { contextMock.isSilent() } returns false
        }
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), isHighPriority = false, any(), enableCallStyle = true) }
    }

    @Test
    fun incomingCall_noFullScreenPermission_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        every { contextMock.canUseFullScreenIntentCompat() } returns false
        mockkObject(CallNotificationProducer) {
            every { CallExtensions.isIncoming(any(), any()) } returns true
            coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any(), any()) } returns incomingCallNotification
            val callNotificationProducer = CallNotificationProducer(backgroundScope)
            callNotificationProducer.listener = listener
            callNotificationProducer.bind(callMock, mockk())
            val participants = callMock.participants.value
            val activityClazz = callMock.activityClazz
            coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
            coVerify(exactly = 1) { CallNotificationProducer.buildIncomingCallNotification(participants, activityClazz, true, false) }
            verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingCallNotification) }
            verify(exactly = 1) { listener.onNewNotification(callMock, incomingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
        }
    }

    @Test
    fun deviceIsPhoneApi34_callServiceInBackground_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        mockSdkInt(34)
        every { contextMock.canUseFullScreenIntentCompat() } returns false
        val callService = mockk<Service>(relaxed = true) {
            every { foregroundServiceType } returns 0
        }
        mockkObject(CallNotificationProducer) {
            every { CallExtensions.isIncoming(any(), any()) } returns true
            coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any(), any()) } returns incomingCallNotification
            val callNotificationProducer = CallNotificationProducer(backgroundScope)
            callNotificationProducer.listener = listener
            callNotificationProducer.bind(callMock, callService)
            val participants = callMock.participants.value
            val activityClazz = callMock.activityClazz
            coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
            coVerify(exactly = 1) { CallNotificationProducer.buildIncomingCallNotification(participants, activityClazz, true, false) }
            verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingCallNotification) }
            verify(exactly = 1) { listener.onNewNotification(callMock, incomingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
        }
    }

    @Test
    fun deviceIsPhoneApi34_callServiceInForeground_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        mockSdkInt(34)
        every { contextMock.canUseFullScreenIntentCompat() } returns false
        val callService = mockk<Service>(relaxed = true) {
            every { foregroundServiceType } returns ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
        }
        mockkObject(CallNotificationProducer) {
            every { CallExtensions.isIncoming(any(), any()) } returns true
            coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any(), any()) } returns incomingCallNotification
            val callNotificationProducer = CallNotificationProducer(backgroundScope)
            callNotificationProducer.listener = listener
            callNotificationProducer.bind(callMock, callService)
            val participants = callMock.participants.value
            val activityClazz = callMock.activityClazz
            coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
            coVerify(exactly = 1) { CallNotificationProducer.buildIncomingCallNotification(participants, activityClazz, true, false) }
            verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingCallNotification) }
            verify(exactly = 1) { listener.onNewNotification(callMock, incomingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
        }
    }

    @Test
    fun deviceIsSmartglass_buildIncomingCallNotification_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = false)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun deviceIsSmartphone_buildIncomingCallNotification_callStyleEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, enableCallStyle = true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), isCallServiceRunning = true, enableCallStyle = true) }
    }

    @Test
    fun isCallServiceRunningTrue_buildIncomingCallNotification_isCallServiceRunningTrue() = runTest(UnconfinedTestDispatcher()) {
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, true, true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun isCallServiceRunningFalse_buildIncomingCallNotification_isCallServiceRunningFalse() = runTest(UnconfinedTestDispatcher()) {
        backgroundScope.launch {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false, true)
        }
        verify(exactly = 1) { NotificationManager.buildIncomingCallNotification(any(), any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun testNotifyOutgoingCall() = runTest(UnconfinedTestDispatcher()) {
        mockkObject(CallNotificationProducer) {
            coEvery { CallNotificationProducer.buildOutgoingCallNotification(any(), any(), any(), any()) } returns outgoingCallNotification
            every { CallExtensions.isOutgoing(any(), any()) } returns true
            val callNotificationProducer = CallNotificationProducer(backgroundScope)
            callNotificationProducer.listener = listener
            callNotificationProducer.bind(callMock, mockk())
            val participants = callMock.participants.value
            val activityClazz = callMock.activityClazz
            coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId", "myUserId").toTypedArray()) }
            coVerify(exactly = 1) { CallNotificationProducer.buildOutgoingCallNotification(participants, activityClazz, true, true) }
            verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, outgoingCallNotification) }
            verify(exactly = 1) { listener.onNewNotification(callMock, outgoingCallNotification, CallNotificationProducer.CALL_NOTIFICATION_ID) }
        }
    }

    @Test
    fun testBuildOutgoingCallNotification() = runTest(UnconfinedTestDispatcher()) {
        every { participantsMock.others } returns listOf(otherParticipantMock)
        backgroundScope.launch {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, true, true)
        }
        verify(exactly = 1) {
            NotificationManager.buildOutgoingCallNotification("otherUsername", any(), clazz, any(), any())
        }
    }

    @Test
    fun groupCall_buildOutgoingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        val calleeDescription = participantsMock.others.map { it.combinedDisplayName.first() }.joinToString()
        backgroundScope.launch {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, true, true)
        }
        verify(exactly = 1) {
            NotificationManager.buildOutgoingCallNotification(calleeDescription, isGroupCall = true, any(), any(), any())
        }
    }

    @Test
    fun oneToOneCall_buildOutgoingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { participantsMock.others } returns listOf(otherParticipantMock)
        backgroundScope.launch {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, true, true)
        }
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), isGroupCall = false, any(), any(), any()) }
    }

    @Test
    fun isCallServiceRunningTrue_buildOutgoingCallNotification_isCallServiceRunningTrue() = runTest(UnconfinedTestDispatcher()) {
        backgroundScope.launch {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, true, true)
        }
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun isCallServiceRunningFalse_buildOutgoingCallNotification_isCallServiceRunningFalse() = runTest(UnconfinedTestDispatcher()) {
        backgroundScope.launch {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, false, false)
        }
        verify(exactly = 1) { NotificationManager.buildOutgoingCallNotification(any(), any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun testNotifyOngoingCall() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock, mockk())
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
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), isLink = true, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun creatorIsNotNull_buildOngoingCallNotification_isLinkIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { participantsMock.creator() } returns otherParticipantMock
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), isLink = false, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun groupCall_buildOngoingCallNotification_isGroupCallIsTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { otherParticipantMock2.combinedDisplayName } returns MutableStateFlow("otherParticipant2")
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), isGroupCall = true, any(), any(),  any(), any(), any()) }
    }

    @Test
    fun oneToOneCall_buildOngoingCallNotification_isGroupCallIsFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isIncoming(any(), any()) } returns false
        every { CallExtensions.isOutgoing(any(), any()) } returns false
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { contextMock.canUseFullScreenIntentCompat() } returns true
        every { participantsMock.others } returns listOf(otherParticipantMock)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk(relaxed = true))
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), isGroupCall = false, any(), any(),  any(), any(), any()) }
    }

    @Test
    fun callRecordingOnConnect_buildOngoingCallNotification_isCallRecordedTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), isCallRecorded = true, any(), any(), any(), any()) }
    }

    @Test
    fun callRecordingNotOnConnect_buildOngoingCallNotification_isCallRecordedFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { recordingMock.type } returns mockk(relaxed = true)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), isCallRecorded = false, any(), any(), any(), any()) }
    }

    @Test
    fun sharingScreenActive_buildOngoingCallNotification_isSharingScreenTrue() = runTest(UnconfinedTestDispatcher()) {
        val screenShareMock = mockk<Input.Video.Application>(relaxed = true)
        every { screenShareMock.state } returns MutableStateFlow(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenShareMock))
        every { CallExtensions.isOngoing(any(), any()) } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), isSharingScreen = true, any(), any(), any()) }
    }

    @Test
    fun sharingScreenNotActive_buildOngoingCallNotification_isSharingScreenFalse() = runTest(UnconfinedTestDispatcher()) {
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf())
        every { CallExtensions.isIncoming(any(), any()) } returns false
        every { CallExtensions.isOutgoing(any(), any()) } returns false
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { contextMock.canUseFullScreenIntentCompat() } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk(relaxed = true))
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), isSharingScreen = false, any(), any(), any()) }
    }

    @Test
    fun callIsInConnectingState_buildOngoingCallNotification_isConnectingTrue() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), isConnecting = true, any(), any()) }
    }

    @Test
    fun callIsNotInConnectingState_buildOngoingCallNotification_isConnectingFalse() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { callMock.state } returns MutableStateFlow(mockk(relaxed = true))
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), isConnecting = false, any(), any()) }
    }

    @Test
    fun deviceIsSmartglass_buildOngoingCallNotification_callStyleDisabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns true
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), enableCallStyle = false) }
    }

    @Test
    fun deviceIsSmartphone_buildOngoingCallNotification_callStyleEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { CallExtensions.isOngoing(any(), any()) } returns true
        every { DeviceUtils.isSmartGlass } returns false
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        callNotificationProducer.bind(callMock, mockk())
        verify(exactly = 1) { NotificationManager.buildOngoingCallNotification(any(), any(), any(), any(), any(), any(), any(), enableCallStyle = true) }
    }

    @Test
    fun testBind() = runTest(UnconfinedTestDispatcher()) {
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        callNotificationProducer.bind(callMock, mockk())
        TestCase.assertEquals(1, backgroundJob.children.count())
        assert(backgroundJob.children.all { it.isActive })
    }

    @Test
    fun testStop() = runTest(UnconfinedTestDispatcher()) {
        val callNotificationProducer = CallNotificationProducer(backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        callNotificationProducer.listener = listener
        callNotificationProducer.bind(callMock, mockk())
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
        callNotificationProducer.bind(callMock, mockk())
        backgroundScope.cancel()
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
        verify(exactly = 1) { listener.onClearNotification(CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

}