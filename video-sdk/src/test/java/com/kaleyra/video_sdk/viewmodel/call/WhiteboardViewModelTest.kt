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
import com.kaleyra.video.conference.Call
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WhiteboardViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WhiteboardViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val sharedFileMock = mockk<SharedFile>()

    private val whiteboardMock = mockk<Whiteboard>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.whiteboard } returns whiteboardMock
        every { callMock.actions } returns MutableStateFlow(emptySet())
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Available)
        }
        viewModel = spyk(WhiteboardViewModel(configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) }, whiteboardView = mockk(relaxed = true)))
    }

    @Test
    fun testWhiteboardLoaded() = runTest {
        advanceUntilIdle()
        verify(exactly = 1) { whiteboardMock.load() }
    }

    @Test
    fun testWhiteboardViewSetUp() = runTest {
        every { whiteboardMock.view } returns MutableStateFlow(null)
        advanceUntilIdle()
        val result = viewModel.uiState.first().whiteboardView
        assertNotEquals(null, result)
        assertEquals(result, whiteboardMock.view.value)
    }

    @Test
    fun testWhiteboardUnloadedOnCallEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        advanceUntilIdle()
        verify(exactly = 1) { whiteboardMock.unload() }
    }

    @Test
    fun testWhiteboardUiState_isLoadingUpdated() = runTest {
        assertEquals(false, viewModel.uiState.first().isLoading)
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loading)
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.first().isLoading)
    }

    @Test
    fun testWhiteboardUiState_isSharingScreenSupportedUpdated() = runTest {
        assertEquals(true, viewModel.uiState.first().isFileSharingSupported)
        every { callMock.buttons } returns MutableStateFlow(setOf(CallUI.Button.FileShare))
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.first().isFileSharingSupported)
    }

    @Test
    fun testOnReloadClick() = runTest {
        advanceUntilIdle()
        verify(exactly = 1) { whiteboardMock.load() }
        viewModel.onReloadClick()
        verify(exactly = 2) { whiteboardMock.load() }
    }

    @Test
    fun testUploadMediaFile_addMediaFileInvoked() = runTest {
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)
        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)
        verify(exactly = 1) { whiteboardMock.addMediaFile(uriMock) }
    }

    @Test
    fun testUploadMediaFile_whiteboardUploadStateUpdated() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.InProgress(500L))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceUntilIdle()
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Uploading(.5f)
        assertEquals(expected, actual)
    }

    @Test
    fun testUploadMediaFileError_whiteboardUploadStateResetAfter3000Ms() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Error(Throwable()))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceTimeBy(1000L)
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Error
        assertEquals(expected, actual)

        advanceTimeBy(3000L)
        val newActual = viewModel.uiState.first().upload
        assertEquals(null, newActual)
    }

    @Test
    fun testUploadMediaFileSuccess_whiteboardUploadStateResetAfter300Ms() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Success(id = "", uri = mockk()))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceTimeBy(100L)
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Uploading(1f)
        assertEquals(expected, actual)

        advanceTimeBy(300L)
        val newActual = viewModel.uiState.first().upload
        assertEquals(null, newActual)
    }
}