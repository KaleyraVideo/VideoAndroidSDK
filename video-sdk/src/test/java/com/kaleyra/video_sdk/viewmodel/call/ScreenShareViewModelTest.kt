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

import androidx.fragment.app.FragmentActivity
import com.kaleyra.video.conference.*
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ScreenShareViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ScreenShareViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>()

    private val inputsMock = mockk<Inputs>()

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val screenShareStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val context = mockk<FragmentActivity>()

    @Before
    fun setUp() {
        viewModel = spyk(ScreenShareViewModel { Configuration.Success(conferenceMock, mockk(),  mockk(relaxed = true), MutableStateFlow(mockk())) })
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { inputs } returns inputsMock
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns meMock
            })
            every { setDisplayMode(any()) } returns true
        }
        every { meMock.streams } returns MutableStateFlow(listOf(screenShareStreamMock))
        with(screenShareStreamMock) {
            every { id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
            every { video } returns MutableStateFlow(null)
        }
    }

    @Test
    fun testShareDeviceScreen() = runTest {
        val videoDeviceMock = mockk<Input.Video.Screen.My>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        var isScreenSharingStarted = true
        viewModel.shareDeviceScreen(context, onScreenSharingStarted = { isScreenSharingStarted = true }, onScreenSharingAborted =  {})
        advanceUntilIdle()
        verify(exactly = 1) { videoDeviceMock.tryEnable() }
        verify(exactly = 1) { screenShareStreamMock.open() }
        verify(exactly = 1) { callMock.setDisplayMode(CallUI.DisplayMode.PictureInPicture) }
        assertEquals(videoDeviceMock, screenShareStreamMock.video.first())
        assertEquals(true, isScreenSharingStarted)
    }

    @Test
    fun testShareApplicationScreen() = runTest {
        val videoAppMock = mockk<Input.Video.Application>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Application) } returns Inputs.RequestResult.Success(videoAppMock)
        advanceUntilIdle()
        viewModel.shareApplicationScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  {})
        advanceUntilIdle()
        verify(exactly = 1) { videoAppMock.tryEnable() }
        verify(exactly = 1) { screenShareStreamMock.open() }
        verify(exactly = 1) { callMock.setDisplayMode(CallUI.DisplayMode.PictureInPicture) }
        assertEquals(videoAppMock, screenShareStreamMock.video.first())
    }

    @Test
    fun screenShareStreamDoesNotExists_shareApplicationScreen_streamIsAdded() = runTest {
        every { meMock.streams } returns MutableStateFlow(listOf())
        val videoDeviceMock = mockk<Input.Video.Application>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Application) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareApplicationScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  {})
        advanceUntilIdle()
        verify(exactly = 1) { meMock.addStream(ScreenShareViewModel.SCREEN_SHARE_STREAM_ID) }
    }

    @Test
    fun screenShareStreamDoesNotExists_shareDeviceScreen_streamIsAdded() = runTest {
        every { meMock.streams } returns MutableStateFlow(listOf())
        val videoDeviceMock = mockk<Input.Video.Screen.My>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareDeviceScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  {})
        advanceUntilIdle()
        verify(exactly = 1) { meMock.addStream(ScreenShareViewModel.SCREEN_SHARE_STREAM_ID) }
    }

    @Test
    fun screenShareStreamInputNotAvailable_shareDeviceScreen_screenSharingAborted() = runTest {
        every { meMock.streams } returns MutableStateFlow(listOf())
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns mockk(relaxed = true)
        advanceUntilIdle()
        var isScreenSharingAborted = false
        viewModel.shareDeviceScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  { isScreenSharingAborted = true })
        advanceUntilIdle()
        Assert.assertEquals(true, isScreenSharingAborted)
    }

    @Test
    fun screenShareStreamCallIsNull_shareDeviceScreen_screenSharingAborted() = runTest {
        every { conferenceMock.call } returns MutableSharedFlow<CallUI>(replay = 1)
        advanceUntilIdle()
        var isScreenSharingAborted = false
        viewModel.shareDeviceScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  { isScreenSharingAborted = true })
        advanceUntilIdle()
        Assert.assertEquals(true, isScreenSharingAborted)
    }

    @Test
    fun screenShareStreamMeParticipantIsNull_shareDeviceScreen_screenSharingAborted() = runTest {
        with(callMock) {
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns null
            })
        }
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns mockk(relaxed = true)
        advanceUntilIdle()
        var isScreenSharingAborted = false
        viewModel.shareDeviceScreen(context, onScreenSharingStarted = {}, onScreenSharingAborted =  { isScreenSharingAborted = true })
        advanceUntilIdle()
        Assert.assertEquals(true, isScreenSharingAborted)
    }
}
