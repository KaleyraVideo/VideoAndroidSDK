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
 * Sign Document View Visibility Observer
 */
class SignDocumentViewVisibilityObserver internal constructor(): BroadcastReceiver() {

    /**
     * @suppress
     */
    companion object {

        /**
         * Sign Document View Displayed
         */
        const val ACTION_SIGN_VIEW_DISPLAYED = "com.kaleyra.video_common_ui.SIGN_VIEW_DISPLAYED"

        const val SIGN_DOCUMENT_ID_DISPLAYED = "com.kaleyra.video_common_ui.SIGN_VIEW_DISPLAYED"

        /**
         * Sign Document View Not Displayed
         */
        const val ACTION_SIGN_VIEW_NOT_DISPLAYED = "com.kaleyra.video_common_ui.SIGN_VIEW_NOT_DISPLAYED"

        private val _isDisplayed: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isDisplayed: StateFlow<Boolean> = _isDisplayed

        private val _signDocumentIdDisplayed: MutableStateFlow<String?> = MutableStateFlow(null)
        val signDocumentIdDisplayed: StateFlow<String?> = _signDocumentIdDisplayed
    }

    /**
     * @suppress
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SIGN_VIEW_DISPLAYED -> {
                _isDisplayed.value = true
                _signDocumentIdDisplayed.value = intent.getStringExtra(SIGN_DOCUMENT_ID_DISPLAYED)
            }
            ACTION_SIGN_VIEW_NOT_DISPLAYED -> {
                _isDisplayed.value = false
                _signDocumentIdDisplayed.value = null
            }
            else -> Unit
        }
    }
}