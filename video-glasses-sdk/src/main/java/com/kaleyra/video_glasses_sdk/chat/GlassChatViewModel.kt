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

package com.kaleyra.video_glasses_sdk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.*
import com.kaleyra.video_utils.battery_observer.BatteryInfo
import com.kaleyra.video_utils.network_observer.WiFiInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Glass Chat View Model
 *
 * @property deviceStatusObserver DeviceStatusObserver device status observer
 * @property conversationState SharedFlow<State> conversation state
 * @property battery SharedFlow<BatteryInfo> flow of BatteryInfo
 * @property wifi SharedFlow<WiFiInfo> flow of WifiInfo
 * @constructor
 */
class GlassChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure) {

    private val deviceStatusObserver = DeviceStatusObserver().apply { start() }

    val conversationState = conversation.flatMapLatest { it.state }.shareInEagerly(viewModelScope)

    val battery: SharedFlow<BatteryInfo> = deviceStatusObserver.battery

    val wifi: SharedFlow<WiFiInfo> = deviceStatusObserver.wifi

    override fun onCleared() {
        super.onCleared()
        deviceStatusObserver.stop()
    }

    /**
     * Glass Chat View Model Instance
     */
    companion object {

        /**
         * Retrieve the Glass Chat View Model Factory
         *
         * @param configure SuspendFunction0<Configuration> callback called when the configuration is needed to build the factory
         * @return NewInstanceFactory the resulting Glass Chat View Model Factory
         */
        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GlassChatViewModel(configure) as T
            }
        }
    }
}
