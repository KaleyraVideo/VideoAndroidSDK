package com.kaleyra.video_common_ui

import android.os.ParcelUuid
import android.telecom.CallEndpoint
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.CallEndpointExtensions.mapToAudioOutputDevice
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class CallEndpointExtensionsTest {

    @Test
    fun endpointSpeaker_mapToAudioOutput_speakerAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_SPEAKER, ParcelUuid(UUID.randomUUID()))
        val expected = AudioOutputDevice.Loudspeaker()
        assertEquals(
            expected,
            endpoint.mapToAudioOutputDevice()
        )
    }

    @Test
    fun endpointEarpiece_mapToAudioOutput_earpieceAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_EARPIECE, ParcelUuid(UUID.randomUUID()))
        val expected = AudioOutputDevice.Earpiece()
        assertEquals(
            expected,
            endpoint.mapToAudioOutputDevice()
        )
    }

    @Test
    fun endpointWiredHeadset_mapToAudioOutput_wiredHeadsetAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_WIRED_HEADSET, ParcelUuid(UUID.randomUUID()))
        val expected = AudioOutputDevice.WiredHeadset()
        assertEquals(
            expected,
            endpoint.mapToAudioOutputDevice()
        )
    }

    @Test
    fun endpointBluetooth_mapToAudioOutput_bluetoothAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_BLUETOOTH, ParcelUuid(UUID.randomUUID()))
        val expected = AudioOutputDevice.Bluetooth(identifier = endpoint.identifier.toString()).apply {
            bluetoothConnectionStatus = null
            name = endpoint.endpointName.toString()
        }
        assertEquals(
            expected,
            endpoint.mapToAudioOutputDevice()
        )
    }

    @Test
    fun endpointUnknown_mapToAudioOutput_null() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_STREAMING, ParcelUuid(UUID.randomUUID()))
        assertEquals(
            null,
            endpoint.mapToAudioOutputDevice()
        )
    }
}