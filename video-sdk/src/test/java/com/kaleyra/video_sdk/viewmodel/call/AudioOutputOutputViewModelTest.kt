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

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.setAudioOutputDevice
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AudioOutputOutputViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AudioOutputViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = AudioOutputViewModel {
            Configuration.Success(
                conferenceMock,
                mockk(),
                mockk(relaxed = true),
                MutableStateFlow(mockk())
            )
        }
        mockkObject(CollaborationAudioExtensions, ConnectionServiceUtils)
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns false
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.WiredHeadset(), AudioOutputDevice.Earpiece(), AudioOutputDevice.Bluetooth("bluetoothId1"), AudioOutputDevice.Bluetooth("bluetoothId2"), AudioOutputDevice.None()))
        val currentAudioOutputDeviceFlow = MutableSharedFlow<AudioOutputDevice>(replay = 1, extraBufferCapacity = 1)
        every { callMock.currentAudioOutputDevice } returns currentAudioOutputDeviceFlow
        every { callMock.setAudioOutputDevice(any(), any()) } answers { currentAudioOutputDeviceFlow.tryEmit(secondArg()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testAudioOutputUiState_playingDeviceIdIsInitialized() = runTest {
        every { callMock.currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        advanceUntilIdle()
        val new = viewModel.uiState.first().playingDeviceId
        val expected = AudioDeviceUi.LoudSpeaker.id
        Assert.assertEquals(expected, new)
    }

    @Test
    fun testAudioOutputUiState_deviceListUpdated() = runTest {
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.WiredHeadset(), AudioOutputDevice.None()))
        val current = viewModel.uiState.first().audioDeviceList.value
        Assert.assertEquals(listOf<AudioDeviceUi>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().audioDeviceList.value
        val expected = listOf(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.WiredHeadset, AudioDeviceUi.Muted)
        Assert.assertEquals(expected, new)
    }

    @Test
    fun testSetDevice_playingDeviceIdIsUpdated() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        val actual = viewModel.uiState.first().playingDeviceId
        Assert.assertEquals(AudioDeviceUi.LoudSpeaker.id, actual)
    }

    @Test
    fun testSetLoudSpeakerDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Loudspeaker(), any()) }
    }

    @Test
    fun testSetEarpieceDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.EarPiece)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Earpiece(), any()) }
    }

    @Test
    fun testSetWiredHeadsetDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.WiredHeadset)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.WiredHeadset(), any()) }
    }

    @Test
    fun testSetBluetoothDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.Bluetooth(id = "bluetoothId2", connectionState = BluetoothDeviceState.Disconnected, name = null, batteryLevel = null))
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Bluetooth("bluetoothId2"), any()) }
    }

    @Test
    fun testSetDeviceConnectionServiceDisabledFlag() = runTest {
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns false
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Loudspeaker(), false) }
    }

    @Test
    fun testSetDeviceConnectionServiceEnabledFlag() = runTest {
        every { ConnectionServiceUtils.isConnectionServiceEnabled } returns true
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Loudspeaker(), true) }
    }

}