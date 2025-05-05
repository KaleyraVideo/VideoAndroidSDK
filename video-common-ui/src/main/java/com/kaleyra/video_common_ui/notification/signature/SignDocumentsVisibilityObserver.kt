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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sign Documents Visibility Observer
 */
class SignDocumentsVisibilityObserver internal constructor(): BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {

        /**
         * Sign Documents Displayed
         */
        const val ACTION_SIGN_DOCUMENTS_DISPLAYED = "com.kaleyra.video_common_ui.SIGN_DOCUMENTS_DISPLAYED"

        /**
         * Sign Documents Not Displayed
         */
        const val ACTION_SIGN_DOCUMENTS_NOT_DISPLAYED = "com.kaleyra.video_common_ui.SIGN_DOCUMENTS_NOT_DISPLAYED"

        private val _isDisplayed: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isDisplayed: StateFlow<Boolean> = _isDisplayed
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SIGN_DOCUMENTS_DISPLAYED -> _isDisplayed.value = true
            ACTION_SIGN_DOCUMENTS_NOT_DISPLAYED -> _isDisplayed.value = false
            else -> Unit
        }
    }
}