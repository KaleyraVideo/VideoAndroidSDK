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

package com.kaleyra.video_common_ui

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.kaleyra.video.State
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.IntegrationInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * KaleyraVideoInitializer
 * An abstract initializer that once implemented is capable of being started from within KaleyraVideoSDK and being used to configure KaleyraVideoSDK when required
 */
abstract class KaleyraVideoInitializer : Initializer<Unit> {

    /**
     * KaleyraVideoService companion object
     **/
    internal companion object {
        internal var instance: KaleyraVideoInitializer? = null
    }

    lateinit var applicationContext: Context

    override fun create(context: Context) {
        applicationContext = context
        instance = this
        Log.e("app initializer", "configuring kaleyra video on thread: ${Thread.currentThread()}")
        onRequestKaleyraVideoConfigure()
        Log.e("app initializer", "kaleyra video is configured: ${KaleyraVideo.isConfigured}")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(ContextRetainer::class.java, IntegrationInfo::class.java)

    /**
     * Abstract callback that is called when the KaleyraVideoSDK is required to be configured in order to let KaleyraVideoSDK function properly
     */
    abstract fun onRequestKaleyraVideoConfigure()

    /**
     * Abstract callback that is called when the KaleyraVideoSDK is required to be connected to let KaleyraVideoSDK function properly
     */
    abstract fun onRequestKaleyraVideoConnect()
}

/**
 * Request a new Configuration via KaleyraVideoService implementation
 * @return CollaborationViewModel.Configuration returns the required configuration if the procedure succeed, a failure error otherwise.
 * @return Boolean true if configure has been called, false otherwise
 */
fun requestConfiguration(): Boolean {
    if (!KaleyraVideo.isConfigured) KaleyraVideoInitializer.instance?.onRequestKaleyraVideoConfigure()
    if (!KaleyraVideo.isConfigured) Log.e("KaleyraVideoSDK", "KaleyraVideoSDK was required to be configured via KaleyraVideoService implementation, but no configuration has been received." +
        "\nPlease implement KaleyraVideoService as requested in order to configure KaleyraVideoSDK when needed." +
        "\nFor further info please refer to: https://github.com/KaleyraVideo/VideoAndroidSDK/wiki/Configure-KaleyraVideoSDK#kaleyravideoservice")
    return KaleyraVideo.isConfigured
}

/**
 * Request a new connection via KaleyraVideoService implementation
 * @param userId String? optional user id of the user to be connected via KaleyraVideo sdk
 * @return Boolean true if connection has been called, false otherwise
 */
suspend fun requestConnect(userId: String? = null): Boolean {
    if (KaleyraVideo.state.value !is State.Disconnected) return true

    KaleyraVideoInitializer.instance?.onRequestKaleyraVideoConnect()

    val result = withTimeoutOrNull(1000) {
        KaleyraVideo.state.first { it !is State.Disconnected }
    }

    if (result == null) {
        Log.e("KaleyraVideoSDK", "KaleyraVideoSDK was required to be connected with userId $userId via KaleyraVideoService implementation, but no connect api has been called." +
            "\nPlease implement KaleyraVideoService as requested in order to connect KaleyraVideoSDK when needed." +
            "\nFor further info please refer to: https://github.com/KaleyraVideo/VideoAndroidSDK/wiki/Configure-KaleyraVideoSDK#kaleyravideoservice")
        KaleyraVideo.disconnect()
        return false
    }

    KaleyraVideo.state.first { it is State.Connected }

    if (userId != null && KaleyraVideo.connectedUser.value?.userId != userId) {
        Log.e("KaleyraVideoSDK", "KaleyraVideoSDK was required to be connected with userId $userId via KaleyraVideoService implementation, connect api has been called with another userId." +
            "\nPlease implement KaleyraVideoService as requested in order to connect KaleyraVideoSDK when needed." +
            "\nFor further info please refer to: https://github.com/KaleyraVideo/VideoAndroidSDK/wiki/Configure-KaleyraVideoSDK#kaleyravideoservice")
        KaleyraVideo.disconnect()
        return false
    }

    return true
}
