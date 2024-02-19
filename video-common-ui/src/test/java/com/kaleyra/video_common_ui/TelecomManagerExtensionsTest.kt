package com.kaleyra.video_common_ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnectionService
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.addIncomingCall
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.getOrRegisterPhoneAccount
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.getPhoneAccountHandle
import com.kaleyra.video_common_ui.connectionservice.TelecomManagerExtensions.placeOutgoingCall
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.getAppName
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasManageOwnCallsPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasReadPhoneNumbersPermission
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class TelecomManagerExtensionsTest {

    @Test
    fun testGetPhoneAccountHandle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val telecomManager = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(context, KaleyraCallConnectionService::class.java)
        val accountHandle = PhoneAccountHandle(componentName, context.applicationContext.packageName + ".connectionService")
        assertEquals(accountHandle, telecomManager.getPhoneAccountHandle(context))
    }

    @Test
    fun testGetOrRegisterPhoneAccount() {
        mockkObject(TelecomManagerExtensions)
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
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
        val telecomManager = spyk(context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager)
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
        val telecomManager = spyk(context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager)
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
        val telecomManager = spyk(context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager)
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
        val telecomManager = spyk(context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager)
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
        val telecomManager = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
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
    fun hasManageOwnCallsPermissionFalse_placeOutgoingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns false
        every { context.hasReadPhoneNumbersPermission() } returns true

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri)
        assertEquals(0, shadowManager.allOutgoingCalls.size)
        unmockkObject(ContextExtensions)
    }

    @Test
    fun hasReadPhoneNumbersPermissionFalse_placeOutgoingCall_fails() {
        val context = spyk(ApplicationProvider.getApplicationContext())
        val telecomManager = context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val shadowManager = shadowOf(telecomManager)
        mockkObject(ContextExtensions)
        every { context.hasManageOwnCallsPermission() } returns true
        every { context.hasReadPhoneNumbersPermission() } returns false

        val accountHandle = telecomManager.getPhoneAccountHandle(context)
        val uri = Uri.parse("content://telecom")
        telecomManager.getOrRegisterPhoneAccount(context, accountHandle)
        telecomManager.placeOutgoingCall(context, uri)
        assertEquals(0, shadowManager.allOutgoingCalls.size)
        unmockkObject(ContextExtensions)
    }
}