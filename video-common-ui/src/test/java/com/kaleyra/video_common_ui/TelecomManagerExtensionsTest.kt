package com.kaleyra.video_common_ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.utils.logger.PHONE_CALL
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addCall
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addIncomingCall
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.getOrRegisterPhoneAccount
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.getPhoneAccountHandle
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.kaleyraAddress
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.placeOutgoingCall
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.getAppName
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasCallPhonePermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasManageOwnCallsPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasReadPhoneNumbersPermission
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class TelecomManagerExtensionsTest {

    private val logger = spyk(object : PriorityLogger() {
        override fun debug(tag: String, message: String) = Unit
        override fun error(tag: String, message: String) = Unit
        override fun info(tag: String, message: String) = Unit
        override fun verbose(tag: String, message: String) = Unit
        override fun warn(tag: String, message: String) = Unit
    }, recordPrivateCalls = true)

    private val loggerTag = "loggerTag"

    @Before
    fun setUp() {
        every { logger invokeNoArgs "getTag" } returns loggerTag
    }

    @Test
    fun testGetPhoneAccountHandle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(context, KaleyraCallConnectionService::class.java)
        val accountHandle = PhoneAccountHandle(componentName, context.applicationContext.packageName + ".connectionService")
        assertEquals(accountHandle, telecomManager.getPhoneAccountHandle(context))
    }

    @Test
    fun testGetOrRegisterPhoneAccount() {
        mockkObject(TelecomManagerExtensions)
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        val handle = PhoneAccountHandle(ComponentName(context, TelecomManagerExtensionsTest::class.java), "handleId")
        every { context.getAppName() } returns "appName"

        assertEquals(0, shadowManager.allPhoneAccounts.size)

        // test the account creation
        val account1 = telecomManager.getOrRegisterPhoneAccount(context, handle)
        assertNotEquals(0, shadowManager.allPhoneAccounts[0].capabilities and PhoneAccount.CAPABILITY_SELF_MANAGED)
        assertEquals(context.getAppName(), account1.label)

        // test the account retrieval
        val account2 = telecomManager.getOrRegisterPhoneAccount(context, handle)
        assertEquals(account1, account2)
        unmockkObject(TelecomManagerExtensions)
    }

    @Test
    fun testAddIncomingCall() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = spyk(context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager)
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns true
        every { telecomManager.isIncomingCallPermitted(any()) } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.addIncomingCall(context, uri)
        assertEquals(1, shadowManager.allIncomingCalls.size)
        assertEquals(accountHandle, shadowManager.allIncomingCalls[0].phoneAccount)
        assertEquals(uri, shadowManager.allIncomingCalls[0].extras.getParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, Uri::class.java))
        unmockkObject(ContextExtensions)
    }

    @Test
    fun isIncomingCallPermittedFalse_addIncomingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = spyk(context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager)
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns true
        every { telecomManager.isIncomingCallPermitted(any()) } returns false

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.addIncomingCall(context, Uri.parse("content://telecom"))
        assertEquals(0, shadowManager.allIncomingCalls.size)
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasManageOwnCallsPermissionFalse_addIncomingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = spyk(context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager)
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns false
        every { context.hasReadPhoneNumbersPermission() } returns true
        every { telecomManager.isIncomingCallPermitted(any()) } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.addIncomingCall(context, Uri.parse("content://telecom"))
        assertEquals(0, shadowManager.allIncomingCalls.size)
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasReadPhoneNumbersPermissionFalse_addIncomingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = spyk(context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager)
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns false
        every { telecomManager.isIncomingCallPermitted(any()) } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.addIncomingCall(context, Uri.parse("content://telecom"))
        assertEquals(0, shadowManager.allIncomingCalls.size)
        unmockkObject(ContextExtensions)
    }

    @Test
    fun testPlaceOutgoingCall() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasCallPhonePermission() } returns true
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri)
        assertEquals(1, shadowManager.allOutgoingCalls.size)
        assertEquals(accountHandle, shadowManager.allOutgoingCalls[0].phoneAccount)
        assertEquals(accountHandle, shadowManager.allOutgoingCalls[0].extras.getParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, PhoneAccountHandle::class.java))
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasCallPhonePermissionFalse_placeOutgoingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasCallPhonePermission() } returns false
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri, logger)
        assertEquals(0, shadowManager.allOutgoingCalls.size)
        verify { logger.error(PHONE_CALL, loggerTag, message = "Missing PHONE_CALL permission") }
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasManageOwnCallsPermissionFalse_placeOutgoingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasCallPhonePermission() } returns true
        every { context.hasManageOwnCallsPermission() } returns false
        every { context.hasReadPhoneNumbersPermission() } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri, logger)
        assertEquals(0, shadowManager.allOutgoingCalls.size)
        verify { logger.error(PHONE_CALL, loggerTag, message = "Missing MANAGE_OWN_CALLS permission") }
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasReadPhoneNumbersPermissionFalse_placeOutgoingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasCallPhonePermission() } returns true
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns false

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri, logger)
        assertEquals(0, shadowManager.allOutgoingCalls.size)
        verify { logger.error(PHONE_CALL, loggerTag, message = "Missing READ_PHONE_NUMBERS permission") }
        unmockkObject(ContextExtensions)
    }

    @Test
    fun callCreatorItsMe_addCall_outgoingCallIsPlaced() {
        mockkObject(ContextRetainer, KaleyraCallConnectionService, TelecomManagerExtensions) {
            val call = mockk<Call>(relaxed = true)
            val participants = mockk<CallParticipants>(relaxed = true)
            val me = mockk<CallParticipant.Me>(relaxed = true)
            val context = spyk(ApplicationProvider.getApplicationContext())
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val uri = kaleyraAddress
            every { ContextRetainer.context } returns context
            every { telecomManager.placeOutgoingCall(any(), any(), any()) } returns Unit
            every { call.participants } returns MutableStateFlow(participants)
            every { participants.me } returns me
            every { participants.creator() } returns me

            telecomManager.addCall(call, logger)
            verify(exactly = 1) { telecomManager.placeOutgoingCall(context, uri, logger) }
            assertEquals(logger, KaleyraCallConnectionService.logger)
        }
    }

    @Test
    fun callCreatorItsNotMe_addCall_incomingCallIsAdded() {
        mockkObject(ContextRetainer, KaleyraCallConnectionService, TelecomManagerExtensions) {
            val call = mockk<Call>(relaxed = true)
            val participants = mockk<CallParticipants>(relaxed = true)
            val me = mockk<CallParticipant.Me>(relaxed = true)
            val context = spyk(ApplicationProvider.getApplicationContext())
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val uri = kaleyraAddress
            every { ContextRetainer.context } returns context
            every { telecomManager.addIncomingCall(any(), any(), any()) } returns Unit
            every { call.participants } returns MutableStateFlow(participants)
            every { participants.me } returns me
            every { participants.creator() } returns mockk()

            telecomManager.addCall(call, logger)
            verify(exactly = 1) { telecomManager.addIncomingCall(context, uri, logger) }
            assertEquals(logger, KaleyraCallConnectionService.logger)
        }
    }

    @Test
    fun callCreatorItsNull_addCall_errorIsLogged() {
        mockkObject(ContextRetainer, KaleyraCallConnectionService, TelecomManagerExtensions) {
            val call = mockk<Call>(relaxed = true)
            val participants = mockk<CallParticipants>(relaxed = true)
            val me = mockk<CallParticipant.Me>(relaxed = true)
            val context = spyk(ApplicationProvider.getApplicationContext())
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            every { ContextRetainer.context } returns context
            every { call.participants } returns MutableStateFlow(participants)
            every { participants.me } returns me
            every { participants.creator() } returns null

            telecomManager.addCall(call, logger)
            verify { logger.error(PHONE_CALL, loggerTag, message = "No incoming or outgoing call found") }
        }
    }
}