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

import android.app.Activity
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.notification.NotificationPresentationHandler
import com.kaleyra.video_common_ui.notification.model.Notification
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SignatureNotificationProducerTest {

    private val callMock = mockk<CallUI>()

    private val otherParticipantMock = mockk<CallParticipant>()

    private val meParticipantMock = mockk<CallParticipant.Me>()

    private val participantsMock = mockk<CallParticipants>()

    private val signDocumentMock = mockk<SignDocument>()

    private val sharedFolderMock = mockk<SharedFolder>()

    @Before
    fun setUp() {
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
        mockkObject(SignDocumentsVisibilityObserver)
        mockkObject(NotificationManager)
        mockkObject(ContactDetailsManager)
        with(NotificationManager) {
            every { buildIncomingSignatureNotification(any(), any(), any(), 1, any()) } returns mockk(relaxed = true)
            every { cancel(any()) } returns mockk(relaxed = true)
            every { notify(any(), any()) } returns mockk(relaxed = true)
        }
        coEvery { ContactDetailsManager.refreshContactDetails(*anyVararg()) } returns Unit
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { sharedFolder } returns sharedFolderMock
            every { activityClazz } returns this@SignatureNotificationProducerTest::class.java
        }
        with(otherParticipantMock) {
            every { userId } returns "otherUserId"
            every { combinedDisplayName } returns MutableStateFlow("otherUsername")
        }
        with(meParticipantMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myUsername")
        }
        with(participantsMock) {
            every { others } returns listOf(otherParticipantMock)
            every { me } returns meParticipantMock
        }
        with(signDocumentMock) {
            every { id } returns "signId"
            every { sender.userId } returns "otherUserId"
        }
        with(sharedFolderMock) {
            every { signDocuments } returns MutableStateFlow(setOf(signDocumentMock))
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNotifySignDocument() = runTest(UnconfinedTestDispatcher()) {
        val signatureNotificationProducer = SignatureNotificationProducer(backgroundScope)
        every { SignDocumentsVisibilityObserver.isDisplayed.value } returns false
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingSignatureNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "signId", NotificationCompat.PRIORITY_HIGH, this@SignatureNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("signId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerLowPriorityNotifySignFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.LowPriority
        }
        val signatureNotificationProducer = SignatureNotificationProducer(backgroundScope)
        signatureNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { SignDocumentsVisibilityObserver.isDisplayed.value } returns false
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingSignatureNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "signId", NotificationCompat.PRIORITY_LOW, this@SignatureNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("signId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerHighPriorityNotifySignFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.HighPriority
        }
        val signatureNotificationProducer = SignatureNotificationProducer(backgroundScope)
        signatureNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { SignDocumentsVisibilityObserver.isDisplayed.value } returns false
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingSignatureNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "signId", NotificationCompat.PRIORITY_HIGH, this@SignatureNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("signId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerHiddenNotifySignFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.Hidden
        }
        val signatureNotificationProducer = SignatureNotificationProducer(backgroundScope)
        signatureNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { SignDocumentsVisibilityObserver.isDisplayed.value } returns false
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 0) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 0) { NotificationManager.buildIncomingSignatureNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "signId", NotificationCompat.PRIORITY_HIGH, this@SignatureNotificationProducerTest::class.java) }
        verify(exactly = 0) { NotificationManager.notify("signId".hashCode(), any()) }
    }


    @Test
    fun testNotificationNotShownIfSignDocumentsIsVisible() = runTest(UnconfinedTestDispatcher()) {
        val signatureNotificationProducer = SignatureNotificationProducer(backgroundScope)
        every { SignDocumentsVisibilityObserver.isDisplayed.value } returns true
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingSignatureNotification(any(), any(), any(), 1, any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
    }

    @Test
    fun testNotificationIsCancelledOnScopeCancel() = runTest {
        val signatureNotificationProducer = SignatureNotificationProducer(this)
        signatureNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coroutineContext.cancelChildren()
        coroutineContext.job.children.first().join()
        verify { NotificationManager.cancel("signId".hashCode()) }
    }

    @Test
    fun testStop() = runTest {
        val signatureNotificationProducer = spyk(SignatureNotificationProducer(this))
        signatureNotificationProducer.bind(callMock)
        signatureNotificationProducer.stop()
    }
}
