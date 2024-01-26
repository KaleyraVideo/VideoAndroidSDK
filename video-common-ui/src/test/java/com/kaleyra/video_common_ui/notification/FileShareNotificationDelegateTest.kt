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
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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
class FileShareNotificationDelegateTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val fileShareNotificationDelegate = object : FileShareNotificationDelegate {}

    private val call = mockk<Call>()

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
        with(NotificationManager) {
            every { buildIncomingFileNotification(any(), any(), any(), any()) } returns mockk(relaxed = true)
            every { cancel(any()) } returns mockk(relaxed = true)
            every { notify(any(), any()) } returns mockk(relaxed = true)
        }
        every { call.participants } returns MutableStateFlow(participants)
        every { call.sharedFolder } returns sharedFolder
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testNotifyDownloadFile() = runTest {
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        val contextMock = mockk<Context>(relaxed = true)
        fileShareNotificationDelegate.syncFileShareNotification(contextMock, call, this@FileShareNotificationDelegateTest::class.java, this)
        advanceUntilIdle()
        verify(exactly = 1) { NotificationManager.buildIncomingFileNotification(contextMock, "otherUsername", "downloadId", this@FileShareNotificationDelegateTest::class.java) }
        verify(exactly = 1) { NotificationManager.notify("downloadId".hashCode(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testNotificationNotShownIfFileShareIsVisible() = runTest {
        every { FileShareVisibilityObserver.isDisplayed.value } returns true
        fileShareNotificationDelegate.syncFileShareNotification(mockk(relaxed = true), call, this@FileShareNotificationDelegateTest::class.java, this)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testUploadIsNotNotified() = runTest {
        every { FileShareVisibilityObserver.isDisplayed.value } returns false
        every { call.sharedFolder } returns mockk {
            every { files } returns MutableStateFlow(setOf(uploadFile))
        }
        fileShareNotificationDelegate.syncFileShareNotification(mockk(relaxed = true), call, this@FileShareNotificationDelegateTest::class.java, this)
        advanceUntilIdle()
        verify(exactly = 0) { NotificationManager.buildIncomingFileNotification(any(), any(), any(), any()) }
        verify(exactly = 0) { NotificationManager.notify(any(), any()) }
        coroutineContext.cancelChildren()
    }

    @Test
    fun testNotificationIsCancelledOnScopeCancel() = runTest {
        fileShareNotificationDelegate.syncFileShareNotification(mockk(relaxed = true), call, this@FileShareNotificationDelegateTest::class.java, this)
        advanceUntilIdle()
        coroutineContext.cancelChildren()
        coroutineContext.job.children.first().join()
        verify { NotificationManager.cancel("downloadId".hashCode()) }
    }
}