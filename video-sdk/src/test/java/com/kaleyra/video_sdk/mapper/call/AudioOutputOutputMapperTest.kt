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

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.audiooutput.model.BluetoothDeviceState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.mapToAudioDeviceUi
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.mapToAudioOutputDevice
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.mapToBluetoothDeviceState
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toAvailableAudioDevicesUi
import com.kaleyra.video_sdk.call.mapper.AudioOutputMapper.toCurrentAudioDeviceUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AudioOutputOutputMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emptyAudioOutputList_toAvailableAudioDevicesUi_emptyAudioDeviceUiList() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().audioOutputDevicesList } returns MutableStateFlow(listOf())
        val result = mockk<CallUI>().toAvailableAudioDevicesUi()
        val actual = result.first()
        Assert.assertEquals(listOf<AudioDeviceUi>(), actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun audioOutputList_toAvailableAudioDevicesUi_mappedAudioDeviceUiList() = runTest {
        mockkObject(CollaborationAudioExtensions)
        val devices = listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.Earpiece(), AudioOutputDevice.None())
        every { any<Call>().audioOutputDevicesList } returns MutableStateFlow(devices)
        val result = mockk<CallUI>().toAvailableAudioDevicesUi()
        val actual = result.first()
        val expected = listOf<AudioDeviceUi>(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.EarPiece, AudioDeviceUi.Muted)
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun loudSpeakerAudioOutputDevice_toCurrentAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        val result = mockk<CallUI>().toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.LoudSpeaker
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun earPieceAudioOutputDevice_toCurrentAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Earpiece())
        val result = mockk<CallUI>().toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.EarPiece
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_toCurrentAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.WiredHeadset())
        val result = mockk<CallUI>().toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.WiredHeadset
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun mutedAudioOutputDevice_toCurrentAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.None())
        val result = mockk<CallUI>().toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.Muted
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun bluetoothAudioOutputDevice_toCurrentAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Bluetooth())
        val result = mockk<CallUI>().toCurrentAudioDeviceUi()
        val actual = result.first()?.javaClass
        val expected = AudioDeviceUi.Bluetooth::class.java
        Assert.assertEquals(expected, actual)
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun loudSpeakerAudioOutputDevice_mapToAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Loudspeaker()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.LoudSpeaker, actual)
    }

    @Test
    fun earPieceAudioOutputDevice_mapToAudioDeviceUimapToAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Earpiece()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.EarPiece, actual)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_mapToAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.WiredHeadset()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.WiredHeadset, actual)
    }

    @Test
    fun noneAudioOutputDevice_mapToAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.None()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.Muted, actual)
    }

    @Test
    fun bluetoothAudioOutputDevice_mapToAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Bluetooth(
            identifier = "id"
        ).apply {
            name = "name"
            bluetoothConnectionStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED
            batteryLevel = 50
        }
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        val expected = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "name",
            connectionState = BluetoothDeviceState.Connected,
            batteryLevel = 50
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun bluetoothStatusFailed_mapToBluetoothDeviceState_bluetoothDeviceStateFailed() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.FAILED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Failed, actual)
    }

    @Test
    fun bluetoothStatusActive_mapToBluetoothDeviceState_bluetoothDeviceStateActive() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVE
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Active, actual)
    }

    @Test
    fun bluetoothStatusDisconnected_mapToBluetoothDeviceState_bluetoothDeviceStateDisconnected() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DISCONNECTED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Disconnected, actual)
    }

    @Test
    fun bluetoothStatusAvailable_mapToBluetoothDeviceState_bluetoothDeviceStateAvailable() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.AVAILABLE
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Available, actual)
    }

    @Test
    fun bluetoothStatusDeactivating_mapToBluetoothDeviceState_bluetoothDeviceStateDeactivating() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DEACTIVATING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Deactivating, actual)
    }

    @Test
    fun bluetoothStatusConnecting_mapToBluetoothDeviceState_bluetoothDeviceStateConnecting() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Connecting, actual)
    }

    @Test
    fun bluetoothStatusConnected_mapToBluetoothDeviceState_bluetoothDeviceStateConnected() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Connected, actual)
    }

    @Test
    fun bluetoothStatusActivating_mapToBluetoothDeviceState_bluetoothDeviceStateActivating() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVATING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Activating, actual)
    }

    @Test
    fun bluetoothStatusConnectingAudio_mapToBluetoothDeviceState_bluetoothDeviceStateConnectingAudio() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING_AUDIO
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.ConnectingAudio, actual)
    }

    @Test
    fun bluetoothStatusPlayingAudio_mapToBluetoothDeviceState_bluetoothDeviceStatePlayingAudio () = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.PLAYING_AUDIO
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.PlayingAudio, actual)
    }

    @Test
    fun earpieceAudioDevice_mapToCurrentAudioDeviceUi_earpieceAudioOutputDevice() = runTest {
        val device = AudioDeviceUi.EarPiece
        assertEquals(
            AudioOutputDevice.Earpiece(),
            device.mapToAudioOutputDevice(mockk())
        )
    }

    @Test
    fun loudSpeakerAudioDevice_mapToCurrentAudioDeviceUi_loudSpeakerAudioOutputDevice() = runTest {
        val device = AudioDeviceUi.LoudSpeaker
        assertEquals(
            AudioOutputDevice.Loudspeaker(),
            device.mapToAudioOutputDevice(mockk())
        )
    }

    @Test
    fun mutedAudioDevice_mapToCurrentAudioDeviceUi_noneAudioOutputDevice() = runTest {
        val device = AudioDeviceUi.Muted
        assertEquals(
            AudioOutputDevice.None(),
            device.mapToAudioOutputDevice(mockk())
        )
    }

    @Test
    fun wiredHeadsetAudioDevice_mapToCurrentAudioDeviceUi_wiredHeadsetAudioOutputDevice() = runTest {
        val device = AudioDeviceUi.WiredHeadset
        assertEquals(
            AudioOutputDevice.WiredHeadset(),
            device.mapToAudioOutputDevice(mockk())
        )
    }

    @Test
    fun bluetoothAudioDevice_mapToCurrentAudioDeviceUi_bluetoothAudioOutputDevice() = runTest {
        mockkObject(CollaborationAudioExtensions)
        val device = AudioDeviceUi.Bluetooth("id", null, BluetoothDeviceState.Activating, null)
        val callMock = mockk<Call>()
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Bluetooth("id2"), AudioOutputDevice.Bluetooth("id")))
        assertEquals(
            AudioOutputDevice.Bluetooth("id"),
            device.mapToAudioOutputDevice(callMock)
        )
        unmockkObject(CollaborationAudioExtensions)
    }

    @Test
    fun bluetoothAudioDeviceWithInvalidId_mapToCurrentAudioDeviceUi_null() = runTest {
        mockkObject(CollaborationAudioExtensions)
        val device = AudioDeviceUi.Bluetooth("id3", null, BluetoothDeviceState.Activating, null)
        val callMock = mockk<Call>()
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Bluetooth("id2"), AudioOutputDevice.Bluetooth("id")))
        assertEquals(
            null,
            device.mapToAudioOutputDevice(callMock)
        )
        unmockkObject(CollaborationAudioExtensions)
    }
}