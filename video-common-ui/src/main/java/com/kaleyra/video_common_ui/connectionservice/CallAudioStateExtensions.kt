package com.kaleyra.video_common_ui.connectionservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.telecom.CallAudioState
import androidx.annotation.RequiresApi
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasBluetoothPermission
import com.kaleyra.video_utils.ContextRetainer

object CallAudioStateExtensions {

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.supportCallAudioStateRoute(route: Int) = (supportedRouteMask and route) == route

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.mapToAvailableAudioOutputs(): List<CallAudioOutput> {
        return mutableListOf<CallAudioOutput>(CallAudioOutput.Muted).apply {
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_EARPIECE)) add(CallAudioOutput.Earpiece)
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_SPEAKER)) add(CallAudioOutput.Speaker)
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_WIRED_HEADSET)) add(CallAudioOutput.WiredHeadset)
            if (supportCallAudioStateRoute(CallAudioState.ROUTE_BLUETOOTH)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val bluetoothRoutes = supportedBluetoothDevices.map { it.mapToBluetoothOutput() }
                    addAll(bluetoothRoutes)
                } else {
                    add(CallAudioOutput.Bluetooth(""))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun CallAudioState.mapToCurrentAudioOutput(): CallAudioOutput? {
        return if (isMuted) CallAudioOutput.Muted
        else {
            when (route) {
                CallAudioState.ROUTE_EARPIECE -> CallAudioOutput.Earpiece
                CallAudioState.ROUTE_SPEAKER -> CallAudioOutput.Speaker
                CallAudioState.ROUTE_WIRED_HEADSET -> CallAudioOutput.WiredHeadset
                CallAudioState.ROUTE_BLUETOOTH -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    activeBluetoothDevice?.mapToBluetoothOutput()
                } else {
                    CallAudioOutput.Bluetooth("")
                }

                else -> null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun BluetoothDevice.mapToBluetoothOutput(): CallAudioOutput {
        val context = ContextRetainer.context
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || context.hasBluetoothPermission()) {
            CallAudioOutput.Bluetooth(id = address, name = name)
        } else CallAudioOutput.Bluetooth(id = address)
    }
}