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

package com.kaleyra.video_common_ui.notification

import android.content.Context
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationProducer
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_utils.ContextRetainer
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileShareNotificationProducerTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>(relaxed = true)

    private val call = mockk<CallUI>() {
        every { activityClazz } returns this@FileShareNotificationProducerTest::class.java
    }

    private val otherParticipant = mockk<CallParticipant> {
        every { userId } returns "otherUserId"
        every { displayName } returns MutableStateFlow("otherUsername")
    }
    private val meParticipant = mockk<CallParticipant.Me> {
        every { userId } returns "myUserId"
        every { displayName } returns MutableStateFlow("myUsername")
    }

    private val participants = mockk<CallParticipants> {
        every { others } returns listOf(otherParticipant)
        every { me } returns meParticipant
    }

    private val downloadFile = mockk<SharedFile> {
        every { id } returns "downloadId"
        every { sender.userId } returns "otherUserId"
    }

    private val uploadFile = mockk<SharedFile> {
        every { id } returns "uploadId"
        every { sender.userId } returns "myUserId"
    }

    private val sharedFolder = mockk<SharedFolder> {
        every { files } returns MutableStateFlow(setOf(downloadFile))
    }

    @Before
    fun setUp() {
        mockkObject(FileShareVisibilityObserver)
        mockkObject(NotificationManager)
        mockkObject(ContextRetainer)
        with(NotificationManager) {
            every { buildIncomingFileNotification(any(), any(), any(), any()) } returns mockk(relaxed = true)
            every { cancel(any()) } returns mockk(relaxed = true)
            every { notify(any(), any()) } returns mockk(relaxed = true)
        }
        every { ContextRetainer.context } returns context
        every { call.participants } returns MutableStateFlow(participants)
        every { call.sharedFolder } returns sharedFolder
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNotifyDownloadFile() = runTest {
        val fileShareNotificationProducer = FileShareNotificationProducer(this)
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        fileShareNotificationProducer.bind(call)
        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.buildIncomingFileNotification(context, "otherUsername", "downloadId", this@FileShareNotificationProducerTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("downloadId".hashCode(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testNotificationNotShownIfFileShareIsVisible() = runTest {
        val fileShareNotificationProducer = FileShareNotificationProducer(this)
        every { FileShareVisibilityObserver.isDisplayed.value } returns true
        fileShareNotificationProducer.bind(call)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testUploadIsNotNotified() = runTest {
        val fileShareNotificationProducer = FileShareNotificationProducer(this)
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        every { call.sharedFolder } returns mockk {
            every { files } returns MutableStateFlow(setOf(uploadFile))
        }
        fileShareNotificationProducer.bind(call)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testNotificationIsCancelledOnScopeCancel() = runTest {
        val fileShareNotificationProducer = FileShareNotificationProducer(this)
        fileShareNotificationProducer.bind(call)
        advanceUntilIdle()
        coroutineContext.cancelChildren()
        coroutineContext.job.children.first().join()
        verify { NotificationManager.cancel("downloadId".hashCode()) }
    }

    @Test
    fun testStop() = runTest {
        val fileShareNotificationProducer = spyk(FileShareNotificationProducer(this))
        fileShareNotificationProducer.bind(call)
        fileShareNotificationProducer.stop()
    }
}