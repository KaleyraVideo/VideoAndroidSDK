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

package com.kaleyra.video_common_ui.notification.fileshare

import android.app.Activity
import android.app.Application
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.MainDispatcherRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FileShareNotificationProducerTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>()

    private val otherParticipantMock = mockk<CallParticipant>()

    private val meParticipantMock = mockk<CallParticipant.Me>()

    private val participantsMock = mockk<CallParticipants>()

    private val downloadFileMock = mockk<SharedFile>()

    private val uploadFileMock = mockk<SharedFile>()

    private val sharedFolderMock = mockk<SharedFolder>()

    @Before
    fun setUp() {
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
        mockkObject(FileShareVisibilityObserver)
        mockkObject(NotificationManager)
        mockkObject(ContactDetailsManager)
        with(NotificationManager) {
            every { buildIncomingFileNotification(any(), any(), any(), 1, any()) } returns mockk(relaxed = true)
            every { cancel(any()) } returns mockk(relaxed = true)
            every { notify(any(), any()) } returns mockk(relaxed = true)
        }
        coEvery { ContactDetailsManager.refreshContactDetails(*anyVararg()) } returns Unit
        with(callMock) {
            every { participants } returns MutableStateFlow(participantsMock)
            every { sharedFolder } returns sharedFolderMock
            every { activityClazz } returns this@FileShareNotificationProducerTest::class.java
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
        with(downloadFileMock) {
            every { id } returns "downloadId"
            every { sender.userId } returns "otherUserId"
        }
        with(uploadFileMock) {
            every { id } returns "uploadId"
            every { sender.userId } returns "myUserId"
        }
        with(sharedFolderMock) {
            every { files } returns MutableStateFlow(setOf(downloadFileMock))
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testHighPriorityNotifyDownloadFile() = runTest(UnconfinedTestDispatcher()) {
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingFileNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "downloadId", NotificationCompat.PRIORITY_HIGH, this@FileShareNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("downloadId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerLowPriorityNotifyDownloadFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.LowPriority
        }
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        fileShareNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingFileNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "downloadId", NotificationCompat.PRIORITY_LOW, this@FileShareNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("downloadId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerHighPriorityNotifyDownloadFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.HighPriority
        }
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        fileShareNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 1) { NotificationManager.buildIncomingFileNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "downloadId", NotificationCompat.PRIORITY_HIGH, this@FileShareNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("downloadId".hashCode(), any()) }
    }

    @Test
    fun testNotificationPresentationHandlerHiddenNotifyDownloadFile() = runTest(UnconfinedTestDispatcher()) {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        every { (notificationPresentationHandler as NotificationPresentationHandler).notificationPresentationHandler } returns {
            Notification.PresentationMode.Hidden
        }
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        fileShareNotificationProducer.onActivityCreated(notificationPresentationHandler, mockk())
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coVerify(exactly = 0) { ContactDetailsManager.refreshContactDetails(*listOf("otherUserId").toTypedArray()) }
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(ApplicationProvider.getApplicationContext(), "otherUsername", "downloadId", NotificationCompat.PRIORITY_HIGH, this@FileShareNotificationProducerTest::class.java) }
        verify(exactly = 0) { NotificationManager.notify("downloadId".hashCode(), any()) }
    }

    @Test
    fun testNotificationNotShownIfFileShareIsVisible() = runTest(UnconfinedTestDispatcher()) {
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        every { FileShareVisibilityObserver.isDisplayed.value } returns true
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), 1, any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
    }

    @Test
    fun testUploadIsNotNotified() = runTest(UnconfinedTestDispatcher()) {
        val fileShareNotificationProducer = FileShareNotificationProducer(backgroundScope)
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        every { callMock.sharedFolder } returns mockk {
            every { files } returns MutableStateFlow(setOf(uploadFileMock))
        }
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), 1, any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
    }

    @Test
    fun testNotificationIsCancelledOnScopeCancel() = runTest {
        val fileShareNotificationProducer = FileShareNotificationProducer(this)
        fileShareNotificationProducer.bind(callMock)
        advanceUntilIdle()
        coroutineContext.cancelChildren()
        coroutineContext.job.children.first().join()
        verify { NotificationManager.cancel("downloadId".hashCode()) }
    }

    @Test
    fun testStop() = runTest {
        val fileShareNotificationProducer = spyk(FileShareNotificationProducer(this))
        fileShareNotificationProducer.bind(callMock)
        fileShareNotificationProducer.stop()
    }
}