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

package com.kaleyra.video_common_ui.utils.extensions

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.requestOverlayPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasCanDrawOverlaysPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isDeviceSecure
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isScreenLocked

/**
 * ActivityExtensions
 */
object ActivityExtensions {

    private const val SCREEN_SHARE_REQUEST_CODE = 233

    private var dismissKeyguardRequested = false

    /**
     * Turn and keep the screen on
     *
     * @receiver Activity
     */
    @Suppress("DEPRECATION")
    fun Activity.turnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Remove the turn and keep the screen on setting
     *
     * @receiver Activity
     */
    @Suppress("DEPRECATION")
    fun Activity.turnScreenOff() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) setTurnScreenOn(false)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    /**
     * Move the current activity back to front
     *
     * @receiver Activity
     */
    fun Activity.moveToFront() =
        startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })

    fun Activity.requestOverlayPermission() {
        if (hasCanDrawOverlaysPermission()) return
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", application.packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivityForResult(intent, SCREEN_SHARE_REQUEST_CODE)
    }

    fun Activity.unlockDevice(onUnlocked: (() -> Unit)? = null, onDismiss: (() -> Unit)? = null) {
        if (!isScreenLocked()) {
            onUnlocked?.invoke()
            return
        }
        if (dismissKeyguardRequested) return
        dismissKeyguardRequested = true
        val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        when {
            // requestDismissKeyguard should be applied only for O_MR1 and higher, on android O it does not work correctly
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> keyguardManager.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissCancelled() {
                        dismissKeyguardRequested = false
                        onDismiss?.invoke()
                    }

                    override fun onDismissSucceeded() {
                        super.onDismissSucceeded()
                        dismissKeyguardRequested = false
                        onUnlocked?.invoke()
                    }
                })

            else -> {
                dismissKeyguardRequested = false
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
                onUnlocked?.invoke()
            }
        }
    }
}