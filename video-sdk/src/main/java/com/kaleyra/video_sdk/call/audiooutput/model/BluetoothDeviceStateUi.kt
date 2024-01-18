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

package com.kaleyra.video_sdk.call.audiooutput.model

/**
 * Bluetooth Audio Device State
 */
enum class BluetoothDeviceState {
    /**
     * Available Bluetooth Audio Device State
     */
    Available,

    /**
     * Connecting Bluetooth Audio Device State
     */
    Connecting,

    /**
     * Connecting Audio Bluetooth Audio Device State
     */
    ConnectingAudio,

    /**
     * Playing Audio Bluetooth Audio Device State
     */
    PlayingAudio,

    /**
     * Connected Bluetooth Audio Device State
     */
    Connected,

    /**
     * Activating Bluetooth Audio Device State
     */
    Activating,

    /**
     * Active Bluetooth Audio Device State
     */
    Active,

    /**
     * Deactivating Bluetooth Audio Device State
     */
    Deactivating,

    /**
     * Disconnected Bluetooth Audio Device State
     */
    Disconnected,

    /**
     * Failed Bluetooth Audio Device State
     */
    Failed
}

internal fun BluetoothDeviceState.isConnecting() =
    this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.ConnectingAudio || this == BluetoothDeviceState.Activating

internal fun BluetoothDeviceState.isConnectedOrPlaying() =
    this == BluetoothDeviceState.Active || this == BluetoothDeviceState.Connected || this == BluetoothDeviceState.PlayingAudio || this == BluetoothDeviceState.Activating || this == BluetoothDeviceState.Connecting || this == BluetoothDeviceState.ConnectingAudio