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

package com.kaleyra.video_common_ui.notification.signature

import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.KaleyraVideoBroadcastReceiver
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationExtra
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Signature Notification Action Receiver
 * @property dispatcher CoroutineDispatcher coroutine dispatcher to be used for the action processing
 * @constructor
 */
class SignatureNotificationActionReceiver internal constructor(val dispatcher: CoroutineDispatcher = Dispatchers.IO): KaleyraVideoBroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {
        /**
         * Action Sign
         */
        const val ACTION_SIGN = "com.kaleyra.video_common_ui.SIGN"
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(dispatcher).launch {
            requestConfigure().let {
                if (!it) return@let context.goToLaunchingActivity()
                KaleyraVideo.onCallReady(MainScope()) { call ->
                    when (intent.extras?.getString(FileShareNotificationExtra.NOTIFICATION_ACTION_EXTRA)) {
                        ACTION_SIGN -> {
                            val signId = intent.getStringExtra(EXTRA_SIGN_ID) ?: return@onCallReady
                            val signDocument = call.sharedFolder.signDocuments.value.firstOrNull { it.id == signId }
                            signDocument?.let { call.sharedFolder.sign(it) }
                            NotificationManager.cancel(signId.hashCode())
                        }
                        else -> Unit
                    }
                }
            }
            pendingResult.finish()
        }
    }
}