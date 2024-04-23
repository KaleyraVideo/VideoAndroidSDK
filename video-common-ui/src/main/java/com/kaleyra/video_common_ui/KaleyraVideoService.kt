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

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.kaleyra.video.State
import com.kaleyra.video_common_ui.common.BoundService
import com.kaleyra.video_common_ui.common.BoundServiceBinder
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * KaleyraVideoService
 * An abstract service that once implemented is capable of being started from within KaleyraVideoSDK and being used to configure KaleyraVideoSDK when required
 */
abstract class KaleyraVideoService : BoundService() {

    /**
     * KaleyraVideoService companion object
     **/
    companion object {

        /**
         * Returns the service that is used to configure KaleyraVideoSDK.
         **/
        suspend fun get(): KaleyraVideoService? = getKaleyraVideoService()
    }

    /**
     * Abstract callback that is called when the KaleyraVideoSDK is required to be configured in order to let KaleyraVideoSDK function properly
     */
    abstract fun onRequestKaleyraVideoConfigure()

    /**
     * Abstract callback that is called when the KaleyraVideoSDK is required to be connected to let KaleyraVideoSDK function properly
     */
    abstract fun onRequestKaleyraVideoConnect()
}

@SuppressLint("QueryPermissionsNeeded")
private fun getService(context: Context): ResolveInfo? {
    val serviceIntent = Intent().setAction("kaleyra_video_sdk_configure").setPackage(context.packageName)
    val resolveInfo = context.packageManager.queryIntentServices(serviceIntent, PackageManager.GET_RESOLVED_FILTER)
    if (resolveInfo.size < 1) return null
    return resolveInfo[0]
}

private suspend fun getKaleyraVideoService(): KaleyraVideoService? = with(ContextRetainer.context) {
    val name = getService(this)?.serviceInfo?.name ?: return null
    val intent = Intent(this, Class.forName(name))
    startService(intent)
    return withTimeoutOrNull(1000L) {
        suspendCancellableCoroutine<KaleyraVideoService> { continuation ->
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val kaleyraVideoService = (service as BoundServiceBinder).getService<KaleyraVideoService>()
                    continuation.resumeWith(Result.success(kaleyraVideoService))
                }

                override fun onServiceDisconnected(name: ComponentName?) = Unit
            }, 0)
            continuation.invokeOnCancellation {
                Log.e("KaleyraVideoSDK", "KaleyraVideoSDK was required to be configured via KaleyraVideoService implementation, but no configuration has been received." +
                    "\nPlease implement KaleyraVideoService as requested in order to configure KaleyraVideoSDK when needed." +
                    "\nFor further info please refer to: https://github.com/KaleyraVideo/VideoAndroidSDK/wiki/Configure-KaleyraVideoSDK#kaleyravideoservice")
            }
        }
    }
}

/**
 * Request a new Configuration via KaleyraVideoService implementation
 * @return CollaborationViewModel.Configuration returns the required configuration if the procedure succeed, a failure error otherwise.
 * @return Boolean true if configure has been called, false otherwise
 */
suspend fun requestConfiguration(): Boolean {
    if (!KaleyraVideo.isConfigured) KaleyraVideoService.get()?.onRequestKaleyraVideoConfigure()
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

    KaleyraVideoService.get()?.onRequestKaleyraVideoConnect()

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
