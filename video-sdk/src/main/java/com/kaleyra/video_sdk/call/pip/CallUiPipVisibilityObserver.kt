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

package com.kaleyra.video_sdk.call.pip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Observes the visibility of the Call UI Picture-in-Picture (PIP) window
 * by listening for broadcast intents.
 *
 * This class is designed to be registered as a [BroadcastReceiver] to react
 * to specific actions broadcast by the Call UI when its PIP state changes.
 *
 * @constructor Creates an instance of [CallUiPipVisibilityObserver].
 * It is marked as `internal` to restrict instantiation within its module.
 */
class CallUiPipVisibilityObserver internal constructor(): BroadcastReceiver() {

    companion object {

        /**
         * Broadcast intent action indicating that the Call UI PIP window is now displayed.
         */
        const val ACTION_CALL_UI_PIP_DISPLAYED = "com.kaleyra.video_sdk.ACTION_CALL_UI_PIP_DISPLAYED"

        /**
         * Broadcast intent action indicating that the Call UI PIP window is no longer displayed.
         */
        const val ACTION_CALL_UI_PIP_NOT_DISPLAYED = "com.kaleyra.ACTION_CALL_UI_PIP_DISPLAYED.ACTION_CALL_UI_PIP_NOT_DISPLAYED"

        /**
         * A mutable state flow holding the current display status of the Call UI PIP window.
         * It is updated based on received broadcast intents.
         */
        private val _isDisplayed: MutableStateFlow<Boolean> = MutableStateFlow(false)

        /**
         * A read-only state flow exposing the current display status of the Call UI PIP window.
         * Consumers can collect from this flow to observe visibility changes.
         */
        val isDisplayed: StateFlow<Boolean> = _isDisplayed
    }

    /**
     * Called when a broadcast intent is received.
     *
     * This method updates the [_isDisplayed] state flow based on the action
     * of the received [intent]. It specifically looks for [ACTION_CALL_UI_PIP_DISPLAYED]
     * and [ACTION_CALL_UI_PIP_NOT_DISPLAYED].
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CALL_UI_PIP_DISPLAYED -> _isDisplayed.value = true
            ACTION_CALL_UI_PIP_NOT_DISPLAYED -> _isDisplayed.value = false
            else -> Unit // Ignore other intents
        }
    }
}