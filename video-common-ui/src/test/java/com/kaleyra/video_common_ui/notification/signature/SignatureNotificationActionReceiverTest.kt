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
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SignatureNotificationActionReceiverTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val sharedFolderMock = mockk<SharedFolder>(relaxed = true)

    private val contextMock = mockk<Context>()

    private val signatureNotificationActionReceiver = spyk(SignatureNotificationActionReceiver(mainDispatcherRule.testDispatcher))

    @Before
    fun setUp() {
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        mockkObject(ContextExtensions)
        mockkObject(NotificationManager)
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoKt")
        every { KaleyraVideo.onCallReady(any(), captureCoroutine()) } answers { coroutine<suspend (CallUI) -> Unit>().coInvoke(callMock) }
        every { contextMock.goToLaunchingActivity() } returns Unit
        every { callMock.sharedFolder } returns sharedFolderMock
        every { sharedFolderMock.sign(any()) } returns Unit
        every { NotificationManager.cancel(any()) } returns Unit
        coEvery { signatureNotificationActionReceiver.goAsync() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnReceiveWithCollaborationConfigured() = runTest {
        val signId = "signId"
        val intent = Intent().apply {
            putExtra(SignatureNotificationExtra.NOTIFICATION_ACTION_EXTRA, SignatureNotificationActionReceiver.ACTION_SIGN)
            putExtra(EXTRA_SIGN_ID, signId)
        }
        val signDocument = mockk<SignDocument> {
            every { id } returns signId
        }
        every { sharedFolderMock.signDocuments } returns MutableStateFlow(setOf(signDocument))
        signatureNotificationActionReceiver.onReceive(mockk(relaxed = true), intent)
        advanceUntilIdle()
        verify { sharedFolderMock.sign(signDocument) }
        verify { NotificationManager.cancel(signId.hashCode()) }
    }

    @Test
    fun testOnReceiveWithCollaborationNotConfigured() = runTest {
        mockkObject(KaleyraVideo)
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoInitializationProviderKt")
        coEvery { requestConfiguration() } returns false
        every { KaleyraVideo.isConfigured } returns false
        signatureNotificationActionReceiver.onReceive(contextMock, mockk(relaxed = true))
        advanceUntilIdle()
        verify { contextMock.goToLaunchingActivity() }
    }
}
