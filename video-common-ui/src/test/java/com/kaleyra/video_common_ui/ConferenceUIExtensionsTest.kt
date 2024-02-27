package com.kaleyra.video_common_ui

import android.app.Notification
import android.content.Context
import android.telecom.TelecomManager
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallActivityShow
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallServiceStart
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_common_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConferenceUIExtensionsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    private val telecomManagerMock = mockk<TelecomManager>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val incomingNotificationMock = mockk<Notification>(relaxed = true)

    private val outgoingNotificationMock = mockk<Notification>(relaxed = true)

    private val clazz = this::class.java

    private
    val logger = object : PriorityLogger() {
        override fun debug(tag: String, message: String) = Unit
        override fun error(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun verbose(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
    }

    @Before
    fun setUp() {
        mockkObject(
            CallExtensions,
            ContextRetainer,
            ConnectionServiceUtils,
            ContextExtensions,
            CallNotificationProducer,
            NotificationManager,
            KaleyraCallService,
            TelecomManagerExtensions,
            ContactDetailsManager
        )
        every { ContextRetainer.context } returns contextMock
        every { KaleyraCallService.start(any()) } returns Unit
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.showOnAppResumed(any()) } returns Unit
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enabled
        every { contextMock.getSystemService(Context.TELECOM_SERVICE) } returns telecomManagerMock
        every { telecomManagerMock.addCall(call = any(), any()) } returns Unit
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { callMock.state } returns MutableStateFlow(mockk(relaxed = true))
        coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any()) } returns incomingNotificationMock
        coEvery { CallNotificationProducer.buildOutgoingCallNotification(any(), any(), any()) } returns outgoingNotificationMock
        every { NotificationManager.notify(any(), any()) } returns Unit
        every { NotificationManager.cancel(any()) } returns Unit
        coEvery { ContactDetailsManager.refreshContactDetails(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun callIsEnded_configureCallServiceStart_connectionServiceIsNotStarted() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { contextMock.hasConnectionServicePermissions() } returns true

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        verify(exactly = 0) { telecomManagerMock.addCall(call = callMock, logger) }
    }

    @Test
    fun callIsEnded_configureCallServiceStart_callServiceIsNotStarted() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        verify(exactly = 0) { KaleyraCallService.start(any()) }
    }

    @Test
    fun callIsEnded_configureCallServiceStart_notificationIsNotDisplayed() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        coVerify(exactly = 0) { CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false) }
        verify(exactly = 0) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingNotificationMock) }
    }

    @Test
    fun isConnectionServiceSupportedFalse_configureCallServiceStart_callServiceStarted() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        verify(exactly = 1) { KaleyraCallService.start(any()) }
    }

    @Test
    fun connectionServiceOptionDisabled_configureCallServiceStart_callServiceStarted() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Disabled

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        verify(exactly = 1) { KaleyraCallService.start(any()) }
    }

    @Test
    fun hasConnectionServicePermissionsTrue_configureCallServiceStart_connectionServiceStarted() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.hasConnectionServicePermissions() } returns true

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        verify(exactly = 1) { telecomManagerMock.addCall(call = callMock, logger) }
    }

    @Test
    fun incomingCallWithoutConnectionServicePermissions_configureCallServiceStart_incomingNotificationDisplayed() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        val userIds = participantsMock.list.map { it.userId }.toTypedArray()
        coVerifyOrder {
            ContactDetailsManager.refreshContactDetails(*userIds)
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false)
        }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingNotificationMock) }
    }

    @Test
    fun outgoingCallWithoutConnectionServicePermissions_configureCallServiceStart_outgoingNotificationDisplayed() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns false
        every { CallExtensions.isOutgoing(any(), any()) } returns true

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        val userIds = participantsMock.list.map { it.userId }.toTypedArray()
        coVerifyOrder {
            ContactDetailsManager.refreshContactDetails(*userIds)
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, false)
        }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, outgoingNotificationMock) }
    }

    @Test
    fun `provisional call notification when there is no connection service permissions is cancelled on call ended`() = runTest(UnconfinedTestDispatcher()) {
        val callStateFlow = MutableStateFlow(mockk<Call.State>(relaxed = true))
        every { callMock.state } returns callStateFlow
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        callStateFlow.value = Call.State.Disconnected.Ended

        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingNotificationMock) }
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun callStateDisconnectedEnded_configureCallActivityShow_showOnAppResumedNotInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { callMock.shouldShowAsActivity() } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 0) { callMock.showOnAppResumed(backgroundScope) }
    }

    @Test
    fun shouldShowAsActivityTrue_configureCallActivityShow_showOnAppResumedInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.shouldShowAsActivity() } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 1) { callMock.showOnAppResumed(backgroundScope) }
    }

    @Test
    fun shouldShowAsActivityFalse_configureCallActivityShow_showOnAppResumedNotInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.shouldShowAsActivity() } returns false
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 0) { callMock.showOnAppResumed(backgroundScope) }
    }

}