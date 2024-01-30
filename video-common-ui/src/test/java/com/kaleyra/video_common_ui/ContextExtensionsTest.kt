/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.doesFileExists
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.getAppName
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasBluetoothPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasCanDrawOverlaysPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasContactsPermissions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasManageOwnCallsPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasReadPhoneNumbersPermission
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
class ContextExtensionsTest {

    // TODO do these tests make sense?
    @Test
    fun validUri_doesFileExists_true() {
        val contextMock = mockk<Context>(relaxed = true)
        val contentResolverMock = mockk<ContentResolver>(relaxed = true)
        val matrixCursor = mockk<MatrixCursor>(relaxed = true)
        val uriMock = mockk<Uri>()
        every { contextMock.contentResolver } returns contentResolverMock
        every { contentResolverMock.query(uriMock, null, null, null, null) } returns matrixCursor
        every { matrixCursor.moveToFirst() } returns true
        val result = contextMock.doesFileExists(uriMock)
        assertEquals(true, result)
    }

    @Test
    fun notValidUri_doesFileExists_false() {
        val contextMock = mockk<Context>(relaxed = true)
        val contentResolverMock = mockk<ContentResolver>(relaxed = true)
        val matrixCursor = mockk<MatrixCursor>(relaxed = true)
        val uriMock = mockk<Uri>()
        every { contextMock.contentResolver } returns contentResolverMock
        every { contentResolverMock.query(uriMock, null, null, null, null) } returns matrixCursor
        every { matrixCursor.moveToFirst() } returns false
        val result = contextMock.doesFileExists(uriMock)
        assertEquals(false, result)
    }

    @Test
    fun testGetAppName() {
        val context = ApplicationProvider.getApplicationContext<Context?>()
        TestCase.assertEquals(
            context.applicationInfo.loadLabel(context.packageManager),
            context.getAppName()
        )
    }

    @Test
    fun testHasReadPhoneNumbersPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadow = shadowOf(context as Application)
        shadow.grantPermissions(Manifest.permission.READ_PHONE_NUMBERS)
        assertEquals(true, context.hasReadPhoneNumbersPermission())
        shadow.denyPermissions(Manifest.permission.READ_PHONE_NUMBERS)
        assertEquals(false, context.hasReadPhoneNumbersPermission())
    }

    @Test
    fun testHasManageOwnCallsPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadow = shadowOf(context as Application)
        shadow.grantPermissions(Manifest.permission.MANAGE_OWN_CALLS)
        assertEquals(true, context.hasManageOwnCallsPermission())
        shadow.denyPermissions(Manifest.permission.MANAGE_OWN_CALLS)
        assertEquals(false, context.hasManageOwnCallsPermission())
    }

    @Test
    fun testHasBluetoothPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadow = shadowOf(context as Application)
        shadow.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        assertEquals(true, context.hasBluetoothPermission())
        shadow.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)
        assertEquals(false, context.hasBluetoothPermission())
    }

    @Test
    fun testHasContactsPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadow = shadowOf(context as Application)
        shadow.grantPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        assertEquals(true, context.hasContactsPermissions())
        shadow.denyPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        assertEquals(false, context.hasContactsPermissions())
        shadow.grantPermissions(Manifest.permission.READ_CONTACTS)
        shadow.denyPermissions(Manifest.permission.WRITE_CONTACTS)
        assertEquals(false, context.hasContactsPermissions())
        shadow.grantPermissions(Manifest.permission.WRITE_CONTACTS)
        shadow.denyPermissions(Manifest.permission.READ_CONTACTS)
        assertEquals(false, context.hasContactsPermissions())
    }

    @Test
    @Config(sdk = [22])
    fun testHasCanDrawOverlaysPermissionApi22() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ShadowSettings.setCanDrawOverlays(false)
        assertEquals(true, context.hasCanDrawOverlaysPermission())
    }

    @Test
    fun testHasCanDrawOverlaysPermission() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ShadowSettings.setCanDrawOverlays(false)
        assertEquals(false, context.hasCanDrawOverlaysPermission())
        ShadowSettings.setCanDrawOverlays(true)
        assertEquals(true, context.hasCanDrawOverlaysPermission())
    }

}