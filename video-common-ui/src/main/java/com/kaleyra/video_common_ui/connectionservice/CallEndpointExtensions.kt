package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import android.telecom.CallEndpoint
import androidx.annotation.RequiresApi
import com.bandyer.android_audiosession.model.AudioOutputDevice

object CallEndpointExtensions {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun CallEndpoint.mapToAudioOutputDevice(): AudioOutputDevice? {
        return when (endpointType) {
            CallEndpoint.TYPE_EARPIECE -> AudioOutputDevice.Earpiece()
            CallEndpoint.TYPE_SPEAKER -> AudioOutputDevice.Loudspeaker()
            CallEndpoint.TYPE_WIRED_HEADSET -> AudioOutputDevice.WiredHeadset()
            CallEndpoint.TYPE_BLUETOOTH -> {
                AudioOutputDevice.Bluetooth(identifier = identifier.toString()).apply {
                    bluetoothConnectionStatus = null
                    name = endpointName.toString()
                }
            }

            else -> null
        }
    }
}