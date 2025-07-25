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
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Effects
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.mapper.InputMapper
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.virtualbackground.state.VirtualBackgroundStateManagerImpl
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import io.mockk.coEvery
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_utils.dispatcher.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class VirtualBackgroundViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: VirtualBackgroundViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.My>(relaxed = true)

    private val effectsMock = mockk<Effects>(relaxed = true)

    private val uri = mockk<Uri>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val dispatcherProvider = object : DispatcherProvider {
        override val default: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val main: CoroutineDispatcher = testDispatcher
        override val mainImmediate: CoroutineDispatcher = testDispatcher
    }

    private val virtualBackgroundManager = VirtualBackgroundStateManagerImpl.createForTesting(dispatcherProvider)

    @Before
    fun setUp() {
        mockkObject(InputMapper)
        viewModel = VirtualBackgroundViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            virtualBackgroundStateManager = virtualBackgroundManager
        )
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        with(myStreamMock) {
            every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideoMock)
        }
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.None)
        every { callMock.effects } returns effectsMock
        every { callMock.toMyCameraStream() } returns MutableStateFlow(myStreamMock)
        with(effectsMock) {
            every { preselected } returns MutableStateFlow(Effect.Video.Background.Image(id = "imageId", image = uri))
            every { available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(id = "blurId", factor = 1f)))
        }
        mockkObject(CallExtensions)
        with(CallExtensions) {
            coEvery { callMock.isCpuThrottling(any()) } returns MutableStateFlow(false)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testVirtualBackgroundUiState_backgroundsUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().backgroundList.value
        assertEquals(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("blurId"), VirtualBackgroundUi.Image("imageId", ImmutableUri(uri))), actual)
    }

    @Test
    fun testSetNoneEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.None)

        val uiStateBackground = viewModel.uiState.first().currentBackground
        val isVirtualBackgroundEnabled = virtualBackgroundManager.isVirtualBackgroundEnabled.value
        assertEquals(VirtualBackgroundUi.None, uiStateBackground)
        assertEquals(false, isVirtualBackgroundEnabled)
        verify { myVideoMock.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun testSetBlurEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.Blur("blurId"))

        val uiStateBackground = viewModel.uiState.first().currentBackground
        val isVirtualBackgroundEnabled = virtualBackgroundManager.isVirtualBackgroundEnabled.value
        assertEquals(VirtualBackgroundUi.Blur("blurId"), uiStateBackground)
        assertEquals(true, isVirtualBackgroundEnabled)
        verify {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Blur)
            })
        }
    }

    @Test
    fun testSetImageEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.Image("imageId"))

        val uiStateBackground = viewModel.uiState.first().currentBackground
        val isVirtualBackgroundEnabled = virtualBackgroundManager.isVirtualBackgroundEnabled.value
        assertEquals(VirtualBackgroundUi.Image("imageId"), uiStateBackground)
        assertEquals(true, isVirtualBackgroundEnabled)
        verify {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Image)
            })
        }
    }

    @Test
    fun effectAppliedAfterStreamVideoIsSet() = runTest {
        val videoFlow = MutableStateFlow<Input.Video.My?>(null)
        every { myStreamMock.video } returns videoFlow

        val viewModel = VirtualBackgroundViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            virtualBackgroundStateManager = virtualBackgroundManager
        )
        advanceUntilIdle()

        viewModel.setEffect(VirtualBackgroundUi.Blur("blurId"))
        verify(exactly = 0) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Blur)
            })
        }

        videoFlow.value = myVideoMock
        advanceUntilIdle()

        verify(exactly = 1) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Blur)
            })
        }
    }

    @Test
    fun testInitialVirtualBackgroundLoading() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur("blurId", .4f))

        val viewModel = VirtualBackgroundViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            virtualBackgroundStateManager = virtualBackgroundManager
        )
        advanceUntilIdle()

        assertEquals(
            VirtualBackgroundUi.Blur("blurId", .4f),
            viewModel.uiState.value.currentBackground
        )
        assertEquals(
            true,
            virtualBackgroundManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun testInitialVirtualBackgroundLoadingWithVideoNull() = runTest {
        every { myStreamMock.video } returns MutableStateFlow(null)

        val viewModel = VirtualBackgroundViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            virtualBackgroundStateManager = virtualBackgroundManager
        )
        advanceUntilIdle()

        assertEquals(
            VirtualBackgroundUi.None,
            viewModel.uiState.value.currentBackground
        )
        assertEquals(
            false,
            virtualBackgroundManager.isVirtualBackgroundEnabled.value
        )
    }
}