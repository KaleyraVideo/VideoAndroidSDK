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

package com.kaleyra.video_common_ui.call

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Conference
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.mapper.InputMapper.isAppScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isDeviceScreenInputActive
import com.kaleyra.video_common_ui.onCallReady
import com.kaleyra.video_common_ui.overlay.AppViewOverlay
import com.kaleyra.video_common_ui.overlay.StatusBarOverlayView
import com.kaleyra.video_common_ui.overlay.ViewOverlayAttacher
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

/**
 * This should be instantiated BEFORE the call activity in launched
 *
 * @property coroutineScope CoroutineScope
 * @constructor
 */
 class ScreenShareOverlayDelegate(
    private val application: Application,
    private val coroutineScope: CoroutineScope = MainScope()
): ActivityLifecycleCallbacks {

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (DeviceUtils.isSmartGlass) return
        KaleyraVideo.onCallReady(coroutineScope) { call ->
            if (activity::class.java != call.activityClazz) return@onCallReady
            syncScreenShareOverlay(activity, call)
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    private val jobs = mutableListOf<Job>()

    private fun syncScreenShareOverlay(activity: Activity, call: Call) {
        var deviceScreenShareOverlay: AppViewOverlay? = null
        var appScreenShareOverlay: AppViewOverlay? = null

        jobs.forEach { it.cancel() }
        jobs += flowOf(call)
            .isDeviceScreenInputActive()
            .onEach {
                withContext(Dispatchers.Main) {
                    if (it) {
                        getOverlayPermission(activity)
                        deviceScreenShareOverlay = AppViewOverlay(StatusBarOverlayView(activity), ViewOverlayAttacher.OverlayType.GLOBAL)
                        deviceScreenShareOverlay!!.show(activity)
                    } else {
                        deviceScreenShareOverlay?.hide()
                        deviceScreenShareOverlay = null
                    }
                }
            }.onCompletion {
                deviceScreenShareOverlay?.hide()
                deviceScreenShareOverlay = null
            }.launchIn(coroutineScope)

        jobs += flowOf(call)
            .isAppScreenInputActive()
            .onEach {
                withContext(Dispatchers.Main) {
                    if (it) {
                        appScreenShareOverlay = AppViewOverlay(StatusBarOverlayView(activity), ViewOverlayAttacher.OverlayType.CURRENT_APPLICATION)
                        appScreenShareOverlay!!.show(activity)
                    } else {
                        appScreenShareOverlay?.hide()
                        appScreenShareOverlay = null
                    }
                }
            }.onCompletion {
                appScreenShareOverlay?.hide()
                appScreenShareOverlay = null
            }.launchIn(coroutineScope)
    }

    fun dispose() {
        application.unregisterActivityLifecycleCallbacks(this)
        jobs.forEach { it.cancel() }
    }

    private fun getOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(activity.application.applicationContext)) return
        val isAndroid12 = Build.VERSION.SDK_INT > Build.VERSION_CODES.R
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", activity.application.packageName, null))
        if (!isAndroid12) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivityForResult(intent, 233)
    }

}