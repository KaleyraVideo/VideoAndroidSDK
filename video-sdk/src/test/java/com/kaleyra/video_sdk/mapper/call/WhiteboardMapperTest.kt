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

package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.isWhiteboardLoading
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.toWhiteboardUploadUi
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhiteboardMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val whiteboardMock = mockk<Whiteboard>(relaxed = true)

    private val sharedFileMock = mockk<SharedFile>(relaxed = true)

    @Before
    fun setUp() {
        every { callMock.whiteboard } returns whiteboardMock
    }

    @Test
    fun whiteboardStateUnloaded_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded)
        
        val actual = callMock.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateLoaded_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loaded)
        
        val actual = callMock.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateLoading_isLoading_true() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loading)
        
        val actual = callMock.isWhiteboardLoading().first()
        assertEquals(true, actual)
    }

    @Test
    fun whiteboardStateCacheError_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded.Error.Cache)
        
        val actual = callMock.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test
    fun whiteboardStateUnknownError_isLoading_false() = runTest {
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Unloaded.Error.Unknown(""))
        
        val actual = callMock.isWhiteboardLoading().first()
        assertEquals(false, actual)
    }

    @Test

    fun sharedFileStateAvailable_toWhiteboardUploadUi_whiteboardUploadUiUploading() = runTest {
        every { sharedFileMock.state } returns MutableStateFlow(SharedFile.State.Available)
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = WhiteboardUploadUi.Uploading(0f)
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStatePending_toWhiteboardUploadUi_whiteboardUploadUiUploading() = runTest {
        every { sharedFileMock.state } returns MutableStateFlow(SharedFile.State.Pending)
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = WhiteboardUploadUi.Uploading(0f)
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStateCancelled_toWhiteboardUploadUi_null() = runTest {
        every { sharedFileMock.state } returns MutableStateFlow(SharedFile.State.Cancelled)
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = null
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStateError_toWhiteboardUploadUi_whiteboardUploadUiError() = runTest {
        every { sharedFileMock.state } returns MutableStateFlow(SharedFile.State.Error(Throwable()))
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = WhiteboardUploadUi.Error
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStateSuccess_toWhiteboardUploadUi_whiteboardUploadUiUploading() = runTest {
        every { sharedFileMock.state } returns MutableStateFlow(SharedFile.State.Success(id = "", uri = mockk()))
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = WhiteboardUploadUi.Uploading(1f)
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStateInProgress_toWhiteboardUploadUi_whiteboardUploadUiUploading() = runTest {
        with(sharedFileMock) {
            every { size } returns 4000L
            every { state } returns MutableStateFlow(SharedFile.State.InProgress(progress = 1000L))
        }
        val actual = sharedFileMock.toWhiteboardUploadUi().first()
        val expected = WhiteboardUploadUi.Uploading(.25f)
        assertEquals(expected, actual)
    }
}