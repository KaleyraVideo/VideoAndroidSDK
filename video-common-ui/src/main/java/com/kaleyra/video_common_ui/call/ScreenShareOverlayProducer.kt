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
import android.os.Bundle
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.mapper.InputMapper.isAppScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isDeviceScreenInputActive
import com.kaleyra.video_common_ui.overlay.AppViewOverlay
import com.kaleyra.video_common_ui.overlay.StatusBarOverlayView
import com.kaleyra.video_common_ui.overlay.ViewOverlayAttacher
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.requestOverlayPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 *
 * @property coroutineScope CoroutineScope
 * @constructor
 */
internal class ScreenShareOverlayProducer(
    private val application: Application,
    private val coroutineScope: CoroutineScope = MainScope()
): ActivityLifecycleCallbacks {

    private var call: CallUI? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val call = call ?: return
        if (activity::class.java != call.activityClazz) return
        syncScreenShareOverlay(activity, call)
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    private val jobs = mutableListOf<Job>()

    /**
     *  This should be called BEFORE the call activity in launched
     *
     * @param call CallUI
     */
    fun bind(call: CallUI) {
        this.call = call
        application.registerActivityLifecycleCallbacks(this)
    }

    fun stop() {
        application.unregisterActivityLifecycleCallbacks(this)
        jobs.forEach { it.cancel() }
    }

    private fun syncScreenShareOverlay(activity: Activity, call: Call) {
        var deviceScreenShareOverlay: AppViewOverlay? = null
        var appScreenShareOverlay: AppViewOverlay? = null

        jobs.forEach { it.cancel() }
        jobs += call
            .isDeviceScreenInputActive()
            .onEach {
                withContext(Dispatchers.Main) {
                    if (it) {
                        activity.requestOverlayPermission()
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

        jobs += call
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

}