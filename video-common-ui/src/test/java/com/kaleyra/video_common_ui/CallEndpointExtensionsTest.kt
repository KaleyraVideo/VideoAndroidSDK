package com.kaleyra.video_common_ui

import android.os.ParcelUuid
import android.telecom.CallEndpoint
import com.kaleyra.video_common_ui.connectionservice.CallAudioOutput
import com.kaleyra.video_common_ui.connectionservice.CallEndpointExtensions.mapToAudioOutput
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
        val expected = CallAudioOutput.Speaker
        assertEquals(
            expected,
            endpoint.mapToAudioOutput()
        )
    }

    @Test
    fun endpointEarpiece_mapToAudioOutput_earpieceAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_EARPIECE, ParcelUuid(UUID.randomUUID()))
        val expected = CallAudioOutput.Earpiece
        assertEquals(
            expected,
            endpoint.mapToAudioOutput()
        )
    }

    @Test
    fun endpointWiredHeadset_mapToAudioOutput_wiredHeadsetAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_WIRED_HEADSET, ParcelUuid(UUID.randomUUID()))
        val expected = CallAudioOutput.WiredHeadset
        assertEquals(
            expected,
            endpoint.mapToAudioOutput()
        )
    }

    @Test
    fun endpointBluetooth_mapToAudioOutput_bluetoothAudioOutput() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_BLUETOOTH, ParcelUuid(UUID.randomUUID()))
        val expected = CallAudioOutput.Bluetooth(
            id = endpoint.identifier.toString(),
            name = endpoint.endpointName.toString()
        )
        assertEquals(
            expected,
            endpoint.mapToAudioOutput()
        )
    }

    @Test
    fun endpointUnknown_mapToAudioOutput_null() {
        val endpoint = CallEndpoint("endpointName", CallEndpoint.TYPE_STREAMING, ParcelUuid(UUID.randomUUID()))
        assertEquals(
            null,
            endpoint.mapToAudioOutput()
        )
    }
}