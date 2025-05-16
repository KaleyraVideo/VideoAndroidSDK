package com.kaleyra.video_common_ui

import android.app.Application
import android.app.Notification
import android.content.Context
import android.telecom.TelecomManager
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_common_ui.ConferenceUIExtensions.bindCallButtons
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallActivityShow
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallServiceStart
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallSounds
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureScreenShareOverlayProducer
import com.kaleyra.video_common_ui.call.CallNotificationProducer
import com.kaleyra.video_common_ui.call.ScreenShareOverlayProducer
import com.kaleyra.video_common_ui.callservice.KaleyraCallService
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.showOnAppResumed
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.canUseFullScreenIntentCompat
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.shouldEnableCallSounds
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.enableCallSounds
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConferenceUIExtensionsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMockState: MutableStateFlow<Call.State> = MutableStateFlow(Call.State.Connected)

    private val callMock = mockk<CallUI>(relaxed = true) {
        every { state } returns callMockState
    }

    private val contextMock = mockk<Application>(relaxed = true)

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
            ContactDetailsManager,
            CollaborationAudioExtensions,
            DeviceUtils
        )
        every { ContextRetainer.context } returns contextMock
        every { KaleyraCallService.start(any()) } returns Unit
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.showOnAppResumed(any()) } returns Unit
        every { callMock.enableCallSounds(any(), any()) } returns Unit
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        every { conferenceMock.connectionServiceOption } returns ConnectionServiceOption.Enforced
        every { contextMock.getSystemService(Context.TELECOM_SERVICE) } returns telecomManagerMock
        every { telecomManagerMock.addCall(call = any(), any()) } returns Unit
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        coEvery { CallNotificationProducer.buildIncomingCallNotification(any(), any(), any(), any()) } returns incomingNotificationMock
        coEvery { CallNotificationProducer.buildOutgoingCallNotification(any(), any(), any(), any()) } returns outgoingNotificationMock
        every { NotificationManager.notify(any(), any()) } returns Unit
        every { NotificationManager.cancel(any()) } returns Unit
        coEvery { ContactDetailsManager.refreshContactDetails(*anyVararg()) } returns Unit
        every { contextMock.shouldEnableCallSounds() } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun shouldEnableCallSoundTrue_callSoundsAreEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.shouldEnableCallSounds() } returns true
        conferenceMock.configureCallSounds(logger, backgroundScope)
        verify(exactly = 1) { callMock.enableCallSounds(logger, any()) }
    }

    @Test
    fun shouldEnableCallSoundFalse_callSoundsAreNotEnabled() = runTest(UnconfinedTestDispatcher()) {
        every { contextMock.shouldEnableCallSounds() } returns false
        conferenceMock.configureCallSounds(logger, backgroundScope)
        verify(exactly = 0) { callMock.enableCallSounds(logger, any()) }
    }

    @Test
    fun callIsEnded_configureCallSounds_innerSoundScopeIsCancelled() = runTest(UnconfinedTestDispatcher()) {
        val callState = MutableStateFlow<Call.State>(Call.State.Connecting)
        every { callMock.state } returns callState
        conferenceMock.configureCallSounds(logger, backgroundScope)

        callState.value = Call.State.Disconnected.Ended

        verify(exactly = 1) { callMock.enableCallSounds(logger, any()) }
        assertEquals(1, backgroundScope.coroutineContext.job.children.count())
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

        coVerify(exactly = 0) { CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false, true) }
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
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false, true)
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
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, false, true)
        }
        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, outgoingNotificationMock) }
    }

    @Test
    fun incomingCallWithoutConnectionServicePermissions_configureCallServiceStart_incomingNotificationDisplayedWithCallStyle() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        every { contextMock.canUseFullScreenIntentCompat() } returns true
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        coVerify {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false, true)
        }
    }

    @Test
    fun outgoingCallWithoutConnectionServicePermissions_configureCallServiceStart_incomingNotificationDisplayedWithCallStyle() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns false
        every { contextMock.canUseFullScreenIntentCompat() } returns true
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns false
        every { CallExtensions.isOutgoing(any(), any()) } returns true

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        coVerify {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, false, true)
        }
    }

    @Test
    fun smartglassDevice_configureCallServiceStart_incomingNotificationDisplayedWithoutlStyle() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        every { contextMock.canUseFullScreenIntentCompat() } returns false
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        coVerify {
            CallNotificationProducer.buildIncomingCallNotification(participantsMock, clazz, false, false)
        }
    }

    @Test
    fun smartglassDevice_configureCallServiceStart_outgoingNotificationDisplayedWithoutlStyle() = runTest(UnconfinedTestDispatcher()) {
        every { DeviceUtils.isSmartGlass } returns true
        every { contextMock.canUseFullScreenIntentCompat() } returns false
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns false
        every { CallExtensions.isOutgoing(any(), any()) } returns true

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)

        coVerify {
            CallNotificationProducer.buildOutgoingCallNotification(participantsMock, clazz, false, false)
        }
    }

    @Test
    fun `provisional call notification when there is no connection service permissions is cancelled on call ended`() = runTest(UnconfinedTestDispatcher()) {
        val callStateFlow = MutableStateFlow(mockk<Call.State>(relaxed = true))
        every { callMock.state } returns callStateFlow
        every { callMock.participants } returns MutableStateFlow( mockk(relaxed = true))
        every { contextMock.hasConnectionServicePermissions() } returns false
        every { CallExtensions.isIncoming(any(), any()) } returns true
        every { CallExtensions.isOutgoing(any(), any()) } returns false

        conferenceMock.configureCallServiceStart(clazz, logger, backgroundScope)
        callStateFlow.value = Call.State.Disconnected.Ended

        verify(exactly = 1) { NotificationManager.notify(CallNotificationProducer.CALL_NOTIFICATION_ID, incomingNotificationMock) }
        verify(exactly = 1) { NotificationManager.cancel(CallNotificationProducer.CALL_NOTIFICATION_ID) }
    }

    @Test
    fun callStateDisconnectedEnded_configureCallActivityShow_showNotInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { callMock.isLink } returns false
        every { callMock.shouldShowAsActivity() } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 0) { callMock.show() }
    }

    @Test
    fun callStateDisconnectedEndedAndIsLink_configureCallActivityShow_showOnAppResumedNotInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        every { callMock.isLink } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 0) { callMock.showOnAppResumed(any()) }
    }

    @Test
    fun shouldShowAsActivityTrue_configureCallActivityShow_showInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.isLink } returns false
        every { callMock.shouldShowAsActivity() } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 1) { callMock.show() }
    }

    @Test
    fun shouldShowAsActivityFalse_configureCallActivityShow_showNotInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.isLink } returns false
        every { callMock.shouldShowAsActivity() } returns false
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 0) { callMock.show() }
    }

    @Test
    fun callIsLink_configureCallActivityShow_showOnAppResumedInvoked() = runTest(UnconfinedTestDispatcher()) {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.isLink } returns true
        conferenceMock.configureCallActivityShow(backgroundScope)
        verify(exactly = 1) { callMock.showOnAppResumed(backgroundScope) }
    }

    @Test
    fun callCreated_configureScreenShareOverlayProducer_screenShareOverlayProducerBound() = runTest {
        var hasBoundScreenShareOverlayProducer = false
        mockkConstructor(ScreenShareOverlayProducer::class)
        every { anyConstructed<ScreenShareOverlayProducer>().bind(callMock) } answers {
            hasBoundScreenShareOverlayProducer = true
            mockk()
        }

        conferenceMock.configureScreenShareOverlayProducer(backgroundScope)
        runCurrent()

        Assert.assertEquals(true, hasBoundScreenShareOverlayProducer)
    }

    @Test
    fun callEnded_configureScreenShareOverlayProducer_screenShareOverlayProducerDisposed() = runTest {
        var hasDisposedScreenShareOverlayProducer = false
        mockkConstructor(ScreenShareOverlayProducer::class)
        every { anyConstructed<ScreenShareOverlayProducer>().dispose() } answers {
            hasDisposedScreenShareOverlayProducer = true
            Unit
        }

        conferenceMock.configureScreenShareOverlayProducer(backgroundScope)
        callMockState.tryEmit(Call.State.Disconnected.Ended)
        runCurrent()

        Assert.assertEquals(true, hasDisposedScreenShareOverlayProducer)
    }

    @Test
    fun coroutineCanceled_configureScreenShareOverlayProducer_screenShareOverlayProducerDisposed() = runTest {
        var hasDisposedScreenShareOverlayProducer = false
        mockkConstructor(ScreenShareOverlayProducer::class)
        every { anyConstructed<ScreenShareOverlayProducer>().dispose() } answers {
            hasDisposedScreenShareOverlayProducer = true
            Unit
        }

        conferenceMock.configureScreenShareOverlayProducer(backgroundScope)
        runCurrent()
        backgroundScope.cancel()
        runCurrent()

        Assert.assertEquals(true, hasDisposedScreenShareOverlayProducer)
    }

    @Test
    fun whiteboardRequestReceived_buttonsProviderCalledWithWhiteboardButton() = runTest {
        val receivedCallButtons: MutableStateFlow<MutableSet<CallUI.Button>> = MutableStateFlow(mutableSetOf())
        val buttonsProvider: ((MutableSet<CallUI. Button>) -> Set<CallUI. Button>) = { callButtons ->
            receivedCallButtons.tryEmit(callButtons)
            callButtons
        }
        every { callMock.type } returns MutableStateFlow(Call.Type.audioVideo())
        every { callMock.actions } returns MutableStateFlow(setOf())
        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        every { callMock.buttonsProvider } returns buttonsProvider
        every { callMock.whiteboard } returns mockk {
            every { events } returns MutableStateFlow(Whiteboard.Event.Request.Show("displayName1"))
        }

        conferenceMock.bindCallButtons(backgroundScope)
        receivedCallButtons.first { it.contains(CallUI.Button.Whiteboard) }

        Assert.assertEquals(true, receivedCallButtons.value.contains(CallUI.Button.Whiteboard))
    }


    @Test
    fun sharedFileReceived_buttonsProviderCalledWithFileShareButton() = runTest {
        val receivedCallButtons: MutableStateFlow<MutableSet<CallUI.Button>> = MutableStateFlow(mutableSetOf())
        val buttonsProvider: ((MutableSet<CallUI. Button>) -> Set<CallUI. Button>) = { callButtons ->
            receivedCallButtons.tryEmit(callButtons)
            callButtons
        }
        every { callMock.type } returns MutableStateFlow(Call.Type.audioVideo())
        every { callMock.actions } returns MutableStateFlow(setOf())
        every { callMock.preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        every { callMock.participants } returns MutableStateFlow(mockk<CallParticipants> {
            every { me } returns mockk {
                every { userId } returns "me"
            }
        })
        every { callMock.buttonsProvider } returns buttonsProvider
        every { callMock.sharedFolder } returns mockk {
            every { signDocuments } returns MutableStateFlow(setOf())
            every { files } returns MutableStateFlow(setOf(mockk {
                every { id } returns "sharedFileId"
                every { name } returns "sharedFileName"
                every { size } returns 1024L
                every { creationTime } returns 1234L
                every { uri } returns mockk()
                every { state } returns MutableStateFlow(SharedFile.State.Available)
                every { sender } returns mockk { every { userId } returns "user1"}
            }))
        }

        conferenceMock.bindCallButtons(backgroundScope)
        receivedCallButtons.first { it.isNotEmpty() }

        Assert.assertEquals(true, receivedCallButtons.value.contains(CallUI.Button.FileShare))
    }
}
