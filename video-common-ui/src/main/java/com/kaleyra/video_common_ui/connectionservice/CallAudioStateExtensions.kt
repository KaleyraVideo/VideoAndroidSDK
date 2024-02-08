package com.kaleyra.video_common_ui.connectionservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.telecom.CallAudioState
import androidx.annotation.RequiresApi
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasBluetoothPermission
import com.kaleyra.video_utils.ContextRetainer

object CallAudioStateExtensions {

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.supportCallAudioStateRoute(route: Int) = (supportedRouteMask and route) == route

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.mapToAvailableAudioOutputDevices(): List<AudioOutputDevice> {
        return mutableListOf<AudioOutputDevice>().apply {
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_EARPIECE)) add(AudioOutputDevice.Earpiece())
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_SPEAKER)) add(AudioOutputDevice.Loudspeaker())
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_WIRED_HEADSET)) add(AudioOutputDevice.WiredHeadset())
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_BLUETOOTH)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val bluetoothRoutes = supportedBluetoothDevices.map { it.mapToBluetoothDevice(supportedBluetoothDevices) }
                    addAll(bluetoothRoutes)
                } else {
                    add(AudioOutputDevice.Bluetooth(""))
                }
            }
            add(AudioOutputDevice.None())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.mapToCurrentAudioOutputDevice(): AudioOutputDevice? {
        return if (isMuted) AudioOutputDevice.None()
        else {
            when (route) {
                CallAudioState.ROUTE_EARPIECE -> AudioOutputDevice.Earpiece()
                CallAudioState.ROUTE_SPEAKER -> AudioOutputDevice.Loudspeaker()
                CallAudioState.ROUTE_WIRED_HEADSET -> AudioOutputDevice.WiredHeadset()
                CallAudioState.ROUTE_BLUETOOTH -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    activeBluetoothDevice?.mapToBluetoothDevice(supportedBluetoothDevices)
                } else {
                    AudioOutputDevice.Bluetooth("")
                }

                else -> null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun BluetoothDevice.mapToBluetoothDevice(supportedBluetoothDevices: Collection<BluetoothDevice>): AudioOutputDevice {
        val context = ContextRetainer.context
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || context.hasBluetoothPermission()) {
            AudioOutputDevice.Bluetooth(identifier = address).apply { name = this@mapToBluetoothDevice.name }
        } else {
            val bluetooth = context.getString(com.bandyer.android_audiosession.R.string.bandyer_audio_device_type_bluetooth)
            AudioOutputDevice.Bluetooth(identifier = address).apply { name = "$bluetooth ${supportedBluetoothDevices.indexOf(this@mapToBluetoothDevice) + 1}" }
        }
    }
}