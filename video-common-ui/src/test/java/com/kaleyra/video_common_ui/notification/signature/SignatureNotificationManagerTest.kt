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

package com.kaleyra.video_common_ui.notification.signature

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotification
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationExtra
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class SignatureNotificationManagerTest {

    private val signatureNotificationManager = object : SignatureNotificationManager {}

    @Before
    fun setUp() {
        mockkConstructor(SignatureNotification.Builder::class)
        every { anyConstructed<SignatureNotification.Builder>().contentTitle(any()) } answers { self as SignatureNotification.Builder }
        every { anyConstructed<SignatureNotification.Builder>().contentText(any()) } answers { self as SignatureNotification.Builder }
        every { anyConstructed<SignatureNotification.Builder>().contentIntent(any()) } answers { self as SignatureNotification.Builder }
        every { anyConstructed<SignatureNotification.Builder>().setPriority(any()) } answers { self as SignatureNotification.Builder }
        every { anyConstructed<SignatureNotification.Builder>().signIntent(any()) } answers { self as SignatureNotification.Builder }
        every { anyConstructed<SignatureNotification.Builder>().build() } returns mockk(relaxed = true)
    }

    @Test
    fun testBuildIncomingFileNotification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        signatureNotificationManager.buildIncomingSignatureNotification(
            context = context,
            username = "username",
            signId = "signId",
            notificationPriority = 1,
            activityClazz = this::class.java
        )
        verify(exactly = 1) {
            anyConstructed<SignatureNotification.Builder>().contentTitle(
                context.getString(
                    R.string.kaleyra_signature_notification_user_sending_file,
                    "username"
                )
            )
        }
        verify(exactly = 1) {
            anyConstructed<SignatureNotification.Builder>().contentText(
                context.getString(R.string.kaleyra_signature_notification_sign_content_message)
            )
        }
        verify(exactly = 1) {
            anyConstructed<SignatureNotification.Builder>().setPriority(1)
        }
        verify(exactly = 1) {
            anyConstructed<SignatureNotification.Builder>().contentIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
                assertEquals(SignatureNotificationActionReceiver.ACTION_SIGN, intent.getStringExtra(SignatureNotificationExtra.NOTIFICATION_ACTION_EXTRA))
            })
        }
        verify(exactly = 1) {
            anyConstructed<SignatureNotification.Builder>().signIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
                assertEquals(SignatureNotificationActionReceiver.ACTION_SIGN, intent.getStringExtra(SignatureNotificationExtra.NOTIFICATION_ACTION_EXTRA))
                assertEquals("signId", intent.getStringExtra(EXTRA_SIGN_ID))
            })
        }
    }
}
