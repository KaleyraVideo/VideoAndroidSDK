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

package com.kaleyra.video_sdk.viewmodel.call

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.Participant
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions.getFileSize
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickProvider
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel.Companion.MaxFileUploadBytes
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FileShareViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FileShareViewModel

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val senderMock = mockk<Participant>()

    private val meMock = mockk<CallParticipant.Me>()

    private val sharedFolderMock = mockk<SharedFolder>(relaxed = true)

    private val sharedFileMock1 = mockk<SharedFile>(relaxed = true)

    private val sharedFileMock2 = mockk<SharedFile>(relaxed = true)

    private val sharedFileMock3 = mockk<SharedFile>(relaxed = true)

    private val sharedFileUi1 = SharedFileUi(id = "sharedFileId", name = "sharedFileName", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 1234L, state = SharedFileUi.State.Available, isMine = false)

    private val sharedFileUi2 = SharedFileUi(id = "sharedFileId2", name = "sharedFileName2", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 2345L, state = SharedFileUi.State.Available, isMine = false)

    private val sharedFileUi3 = SharedFileUi(id = "sharedFileId3", name = "sharedFileName3", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 3456L, state = SharedFileUi.State.Available, isMine = false)

    @Before
    fun setUp() {
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
        mockkObject(UriExtensions)
        mockkObject(ContactDetailsManager)
        every { any<Uri>().getFileSize() } returns 0
        mockkObject(CallUserMessagesProvider)
        viewModel = FileShareViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            filePickProvider = object : FilePickProvider {
                override val fileUri: Flow<Uri> = MutableStateFlow(uriMock)
            }
        )
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { sharedFolder } returns sharedFolderMock
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns meMock
            })
        }
        with(meMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
        }
        with(senderMock) {
            every { userId } returns "userId"
            every { combinedDisplayName } returns MutableStateFlow("displayName")
        }
        with(sharedFileMock1) {
            every { id } returns "sharedFileId"
            every { name } returns "sharedFileName"
            every { size } returns 1024L
            every { creationTime } returns 1234L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
        with(sharedFileMock2) {
            every { id } returns "sharedFileId2"
            every { name } returns "sharedFileName2"
            every { size } returns 1024L
            every { creationTime } returns 2345L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
        with(sharedFileMock3) {
            every { id } returns "sharedFileId3"
            every { name } returns "sharedFileName3"
            every { size } returns 1024L
            every { creationTime } returns 3456L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testFileShareUiState_sharedFilesUpdated() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1))
        val current = viewModel.uiState.first().sharedFiles
        assertEquals(ImmutableList(listOf<SharedFileUi>()), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().sharedFiles
        val expected = ImmutableList(listOf(sharedFileUi1))
        assertEquals(expected, new)
    }

    @Test
    fun testFileShareUiState_sharedFilesAreOrderedByUpdatedTime() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock2, sharedFileMock1, sharedFileMock3))
        advanceUntilIdle()
        val new = viewModel.uiState.first().sharedFiles
        val expected = ImmutableList(listOf(sharedFileUi3, sharedFileUi2, sharedFileUi1))
        assertEquals(expected, new)
    }

    @Test
    fun testFileShareUiState_showFileSizeLimitUpdated() = runTest {
        every { any<Uri>().getFileSize() } returns MaxFileUploadBytes + 1L
        advanceUntilIdle()
        val new = viewModel.uiState.first().showFileSizeLimit
        assertEquals(true, new)
    }

    @Test
    fun testUploadOnFilePick() = runTest {
        var onFileSelectedInvoked = false
        viewModel.setOnFileSelected {
            onFileSelectedInvoked = true
        }
        advanceUntilIdle()
        verify { sharedFolderMock.upload(uriMock) }
        assertEquals(true, onFileSelectedInvoked)
    }

    @Test
    fun testUpload() = runTest {
        val uriMock = mockk<Uri>()
        advanceUntilIdle()
        viewModel.upload(uriMock)
        verify { sharedFolderMock.upload(uriMock) }
    }

    @Test
    fun testDownload() = runTest {
        advanceUntilIdle()
        viewModel.download("id")
        verify { sharedFolderMock.download("id") }
    }

    @Test
    fun testCancel() = runTest {
        advanceUntilIdle()
        viewModel.cancel("id")
        verify { sharedFolderMock.cancel("id") }
    }

    @Test
    fun testDismissUploadLimit() = runTest {
        viewModel.dismissUploadLimit()
        assertEquals(false,  viewModel.uiState.first().showFileSizeLimit)
    }
}
