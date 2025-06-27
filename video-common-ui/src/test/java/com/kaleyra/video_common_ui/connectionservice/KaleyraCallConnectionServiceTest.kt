package com.kaleyra.video_common_ui.connectionservice

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.net.Uri
import android.os.Build
import android.telecom.Connection
import androidx.test.core.app.ApplicationProvider
import com.bandyer.android_audiosession.sounds.CallSound
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.TestUtils.setPrivateField
import com.kaleyra.video_common_ui.callservice.CallForegroundServiceWorker
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.shouldShowAsActivity
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.showOnAppResumed
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
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class KaleyraCallConnectionServiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private var service: KaleyraCallConnectionService? = null

    private var notificationBuilder: Notification.Builder? = null

    private var notificationManager = ApplicationProvider.getApplicationContext<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val otherParticipantMock = mockk<CallParticipant>(relaxed = true)

    private val connectionMock = mockk<KaleyraCallConnection>(relaxed = true)

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val coroutineScope = MainScope()

    private val logger = object : PriorityLogger(BaseLogger.VERBOSE) {
        override fun debug(tag: String, message: String) = Unit
        override fun error(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun verbose(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
    }

    @Before
    fun setup() {
        service = spyk(Robolectric.setupService(KaleyraCallConnectionService::class.java))
        service!!.setPrivateField("coroutineScope", coroutineScope)
        KaleyraCallConnectionService.logger = logger
        notificationBuilder = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("Test")
            .setContentText("content text")
        mockkConstructor(CallForegroundServiceWorker::class)
        mockkObject(ContactsController)
        mockkObject(CallExtensions)
        mockkObject(ContactDetailsManager)
        mockkObject(KaleyraCallConnection)
        mockkObject(KaleyraVideo)
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk(relaxed = true)
        every { anyConstructed<CallForegroundServiceWorker>().bind(any(), any()) } returns Unit
        every { anyConstructed<CallForegroundServiceWorker>().bind(any(), any(), any()) } returns Unit
        every { anyConstructed<CallForegroundServiceWorker>().dispose() } returns Unit
        every { ContactsController.createOrUpdateConnectionServiceContact(any(), any(), any()) } returns Unit
        every { ContactsController.deleteConnectionServiceContact(any(), any()) } returns Unit
        with(callMock) {
            every { shouldShowAsActivity() } returns false
            every { showOnAppResumed(any()) } returns Unit
            every { participants } returns MutableStateFlow(participantsMock)
        }
        every { KaleyraCallConnection.create(any(), any(), any()) } returns connectionMock
        every { KaleyraVideo.conference } returns conferenceMock
        with(conferenceMock) {
            every { call } returns MutableStateFlow(callMock)
        }
    }

    @After
    fun tearDown() {
        KaleyraCallConnectionService.logger = null
        service!!.onDestroy()
        unmockkAll()
    }

    @Test
    fun testOnStartCommand() {
        val startType = service!!.onStartCommand(null, 0, 0)
        assertEquals(Service.START_STICKY, startType)
    }

    @Test
    fun testOnDestroy() {
        mockkObject(CollaborationAudioExtensions) {
            val uri = Uri.parse("")
            val application = mockk<Application>(relaxed = true)
            every { connectionMock.address } returns uri
            every { service!!.application } returns application
            val conn = service!!.onCreateOutgoingConnection(mockk(), mockk())
            service!!.onDestroy()
            verify(exactly = 1) { anyConstructed<CallForegroundServiceWorker>().dispose() }
            verify(exactly = 1) { ContactsController.deleteConnectionServiceContact(service!!, uri) }
            verify(exactly = 1) { callMock.disableAudioRouting(any()) }
            verify(exactly = 1) { application.unregisterActivityLifecycleCallbacks(conn as ActivityLifecycleCallbacks) }
        }
    }

    @Test
    fun testOnSilence() {
        mockkObject(CallSound)
        service!!.onSilence()
        verify(exactly = 1) { CallSound.stop(any(), true) }
        unmockkObject(CallSound)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testOnNewNotification() {
        val notification = notificationBuilder!!.build()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onCreateOutgoingConnection(mockk(), mockk())
        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
        assertEquals(
            notification,
            Shadows.shadowOf(notificationManager).getNotification(10)
        )
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun testOnNewNotificationWithForegroundServiceType() {
        val notification = notificationBuilder!!.build()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onCreateOutgoingConnection(mockk(), mockk())
        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
        assertEquals(
            notification,
            Shadows.shadowOf(notificationManager).getNotification(10)
        )
        assertNotEquals(0, notification.flags and Notification.FLAG_FOREGROUND_SERVICE)
        assertEquals(
            service!!.getForegroundServiceType(false, false, false),
            service!!.foregroundServiceType
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testStartForegroundIsExecutedOnNotificationUpdate() {
        val notification = notificationBuilder!!.build()
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())

        service!!.onCreateOutgoingConnection(mockk(), mockk())
        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)

        val newNotification = Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("new test")
            .setContentText("new content text")
            .build()
        service!!.onNewNotification(callMock, newNotification, 10)
        assertEquals(newNotification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun testInputRemovedForegroundTypenUpdated() = runTest {
        val notification = notificationBuilder!!.build()
        val inputsFlow = MutableStateFlow(setOf(mockk<Input.Audio>(), mockk<Input.Video.Camera.Internal>(), mockk<Input.Video.Screen.My>()))
        every { callMock.inputs.availableInputs } returns inputsFlow

        service!!.onCreateOutgoingConnection(mockk(), mockk())
        service!!.onNewNotification(callMock, notification, 10)
        assertEquals(notification, Shadows.shadowOf(service).lastForegroundNotification)
        assertEquals(10, Shadows.shadowOf(service).lastForegroundNotificationId)
        assertEquals(service!!.getForegroundServiceType(true, true, true), service!!.foregroundServiceType)

        inputsFlow.emit(setOf())
        runCurrent()

        assertEquals(service!!.getForegroundServiceType(false, false, false), service!!.foregroundServiceType)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun testConnectionServiceAlwaysDisabledOnSmartGlass() {
        val conferenceUI = ConferenceUI(mockk(relaxed = true), Class.forName("androidx.appcompat.app.AppCompatActivity"), isSmartGlass = true)
        Assert.assertEquals(ConnectionServiceOption.Disabled, conferenceUI.connectionServiceOption)
        conferenceUI.connectionServiceOption = ConnectionServiceOption.Enforced
        Assert.assertEquals(ConnectionServiceOption.Disabled, conferenceUI.connectionServiceOption)
    }

    @Test
    fun testOnClearNotification() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onClearNotification(10)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnDestroyRemovesNotification() {
        val n: Notification = notificationBuilder!!.build()
        service!!.startForeground(10, n)
        service!!.onDestroy()
        assertEquals(null, Shadows.shadowOf(notificationManager).getNotification(10))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun testOnConnectionServiceFocusLost() {
        service!!.onConnectionServiceFocusLost()
        verify(exactly = 1) { service!!.connectionServiceFocusReleased() }
    }

    @Test
    fun testOnCreateIncomingConnectionFailed() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onCreateIncomingConnectionFailed(null, null)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnCreateOutgoingConnectionFailed() {
        service!!.startForeground(10, notificationBuilder!!.build())
        service!!.onCreateOutgoingConnectionFailed(null, null)
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).notificationShouldRemoved)
    }

    @Test
    fun testOnCreateOutgoingConnection() {
        mockkObject(CollaborationAudioExtensions) {
            val application = mockk<Application>(relaxed = true)
            every { service!!.application } returns application
            val createdConnection = service!!.onCreateOutgoingConnection(mockk(), mockk()) as KaleyraCallConnection
            assertEquals(connectionMock, createdConnection)
            verify { connectionMock.setDialing() }
            verify { connectionMock.addListener(service!!) }
            verify(exactly = 1) {
                anyConstructed<CallForegroundServiceWorker>().bind(
                    service!!,
                    callMock,
                    connectionMock
                )
            }
            verify(exactly = 1) {
                callMock.enableAudioRouting(
                    createdConnection,
                    createdConnection.currentAudioDevice,
                    createdConnection.availableAudioDevices,
                    logger,
                    coroutineScope
                )
            }
            verify(exactly = 1) { application.registerActivityLifecycleCallbacks(createdConnection as ActivityLifecycleCallbacks) }
        }
    }

    @Test
    fun testOnCreateIncomingConnection() {
        mockkObject(CollaborationAudioExtensions) {
            val application = mockk<Application>(relaxed = true)
            every { service!!.application } returns application
            val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk()) as KaleyraCallConnection
            assertEquals(connectionMock, createdConnection)
            verify { connectionMock.setRinging() }
            verify { connectionMock.addListener(service!!) }
            verify(exactly = 1) {
                anyConstructed<CallForegroundServiceWorker>().bind(
                    service!!,
                    callMock,
                    connectionMock
                )
            }
            verify(exactly = 1) {
                callMock.enableAudioRouting(
                    createdConnection,
                    createdConnection.currentAudioDevice,
                    createdConnection.availableAudioDevices,
                    logger,
                    coroutineScope
                )
            }
            verify(exactly = 1) { application.registerActivityLifecycleCallbacks(createdConnection as ActivityLifecycleCallbacks) }
        }
    }

    @Test
    fun testIncomingConnectionAnswerAfterInitialization() = runTest {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        val result = KaleyraCallConnectionService.answer()
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onAnswer() }
    }

    @Test
    fun testIncomingConnectionAnswerBeforeInitialization() = runTest(UnconfinedTestDispatcher()) {
        var result = false
        backgroundScope.launch { result = KaleyraCallConnectionService.answer() }
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onAnswer() }
    }

    @Test
    fun testIncomingConnectionAnswerTimeout() = runTest {
        val result = KaleyraCallConnectionService.answer()
        assertEquals(false, result)
    }

    @Test
    fun testIncomingConnectionRejectAfterInitialization() = runTest {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        val result = KaleyraCallConnectionService.reject()
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onReject() }
    }

    @Test
    fun testIncomingConnectionRejectBeforeInitialization() = runTest(UnconfinedTestDispatcher()) {
        var result = false
        backgroundScope.launch { result = KaleyraCallConnectionService.reject() }
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onReject() }
    }

    @Test
    fun testIncomingConnectionEndAfterInitialization() = runTest {
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        val result = KaleyraCallConnectionService.hangUp()
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun testIncomingConnectionRejectTimeout() = runTest {
        val result = KaleyraCallConnectionService.reject()
        assertEquals(false, result)
    }

    @Test
    fun testIncomingConnectionEndBeforeInitialization() = runTest(UnconfinedTestDispatcher()) {
        var result = false
        backgroundScope.launch { result = KaleyraCallConnectionService.hangUp() }
        val createdConnection = service!!.onCreateIncomingConnection(mockk(), mockk())
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun testOutgoingConnectionEndAfterInitialization() = runTest {
        val createdConnection = service!!.onCreateOutgoingConnection(mockk(), mockk())
        val result = KaleyraCallConnectionService.hangUp()
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun testOutgoingConnectionEndBeforeInitialization() = runTest(UnconfinedTestDispatcher()) {
        var result = false
        backgroundScope.launch { result = KaleyraCallConnectionService.hangUp() }
        val createdConnection = service!!.onCreateOutgoingConnection(mockk(), mockk())
        assertEquals(true, result)
        verify(exactly = 1) { createdConnection.onDisconnect() }
    }

    @Test
    fun testOutgoingConnectionEndTimeout() = runTest {
        val result = KaleyraCallConnectionService.hangUp()
        assertEquals(false, result)
    }

    @Test
    fun connectionStateDisconnected_onConnectionStateChange_serviceIsStopped() {
        val connection = mockk<KaleyraCallConnection>(relaxed = true)
        every { connection.state } returns Connection.STATE_DISCONNECTED
        service!!.onConnectionStateChange(connection)
        verify { connection.removeListener(service!!) }
        assertEquals(true, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(true, Shadows.shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun genericConnectionState_onConnectionStateChange_serviceIsNotStopped() {
        val connection = mockk<KaleyraCallConnection>(relaxed = true)
        every { connection.state } returns mockk(relaxed = true)
        service!!.onConnectionStateChange(connection)
        verify(exactly = 0) { connection.removeListener(service!!) }
        assertEquals(false, Shadows.shadowOf(service).isForegroundStopped)
        assertEquals(false, Shadows.shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun testOnShowIncomingCallUi() {
        mockkObject(CollaborationAudioExtensions) {
            val connection = mockk<KaleyraCallConnection>(relaxed = true)
            service!!.onShowIncomingCallUi(connection)
            verify(exactly = 1) { ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, any()) }
        }
    }

    @Test
    fun groupCall_onShowIncomingCallUi_contactSetAsIncomingGroupCallText() {
        val connection = mockk<KaleyraCallConnection>(relaxed = true)
        val otherParticipantMock2 = mockk<CallParticipant>(relaxed = true)
        every { participantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        service!!.onShowIncomingCallUi(connection)
        val text = service!!.resources.getString(R.string.kaleyra_notification_incoming_group_call)
        verify(exactly = 1) {
            ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, text)
        }
    }

    @Test
    fun oneToOne_onShowIncomingCallUi_contactSetAsOtherUsername() {
        val connection = mockk<KaleyraCallConnection>(relaxed = true)
        every { participantsMock.others } returns listOf(otherParticipantMock)
        every { otherParticipantMock.combinedDisplayName } returns MutableStateFlow("otherDisplayName")
        service!!.onShowIncomingCallUi(connection)
        verify(exactly = 1) {
            ContactsController.createOrUpdateConnectionServiceContact(service!!, connection.address, "otherDisplayName")
        }
    }

}