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

package com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/**
 * Terms And Conditions Broadcast Receiver
 */
abstract class TermsAndConditionBroadcastReceiver : BroadcastReceiver() {

    /**
     * Terms And Conditions Broadcast Receiver Instance
     */
    companion object {

        /**
         * Accept Action
         */
        const val ACTION_ACCEPT = "com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.ACTION_ACCEPT"

        /**
         * Decline Action
         */
        const val ACTION_DECLINE = "com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.ACTION_DECLINE"

        /**
         * Cancel Action
         */
        const val ACTION_CANCEL = "com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.ACTION_CANCEL"
    }

    /**
     * Register the broadcast receiver
     * @param context Context required context to register the broadcast receiver
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun registerForTermAndConditionAction(context: Context) {
        val filter = IntentFilter().apply {
            addAction(ACTION_ACCEPT)
            addAction(ACTION_DECLINE)
            addAction(ACTION_DECLINE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) context.registerReceiver(this, filter, RECEIVER_NOT_EXPORTED)
        else context.registerReceiver(this, filter)
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ACCEPT && intent.action != ACTION_DECLINE && intent.action != ACTION_CANCEL) return
        when (intent.action) {
            ACTION_ACCEPT -> onActionAccept()
            ACTION_DECLINE -> onActionDecline()
            ACTION_CANCEL -> onActionCancel()
        }
        context.unregisterReceiver(this)
    }

    /**
     * Callback invoked when the action has been accepted
     */
    abstract fun onActionAccept()

    /**
     * Callback invoked when the action has been declined
     */
    abstract fun onActionDecline()

    /**
     * Callback invoked when the action has been canceled
     */
    abstract fun onActionCancel()

}