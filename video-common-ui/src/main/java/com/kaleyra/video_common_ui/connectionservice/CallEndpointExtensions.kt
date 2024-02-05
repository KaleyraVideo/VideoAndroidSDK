package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import android.telecom.CallEndpoint
import androidx.annotation.RequiresApi

object CallEndpointExtensions {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun CallEndpoint.mapToAudioOutput(): CallAudioOutput? {
        return when (endpointType) {
            CallEndpoint.TYPE_EARPIECE -> CallAudioOutput.Earpiece
            CallEndpoint.TYPE_SPEAKER -> CallAudioOutput.Speaker
            CallEndpoint.TYPE_WIRED_HEADSET -> CallAudioOutput.WiredHeadset
            CallEndpoint.TYPE_BLUETOOTH -> {
                CallAudioOutput.Bluetooth(
                    id = identifier.toString(),
                    name = endpointName.toString()
                )
            }

            else -> null
        }
    }
}