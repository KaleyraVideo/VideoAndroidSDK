package com.kaleyra.video_sdk.call.utils

import android.app.Service
import android.content.Intent
import android.os.Binder

/**
 * Fixes an issue in android 12 when a task cannot be resumed from an activity that has been reduced into pip and then resumed fullscreen:
 * https://issuetracker.google.com/issues/207397151#comment17
 */
internal class Android12CallActivityTasksFixService : Service() {

    override fun onBind(intent: Intent?) = StubBinder()

    inner class StubBinder : Binder() {
        fun getService() = this@Android12CallActivityTasksFixService
    }
}

/**
 * Fixes an issue in android 12 when a task cannot be resumed from an activity that has been reduced into pip and then resumed fullscreen:
 * https://issuetracker.google.com/issues/207397151#comment17
 */
internal class Android12ChatActivityTasksFixService : Service() {

    override fun onBind(intent: Intent?) = StubBinder()

    inner class StubBinder : Binder() {
        fun getService() = this@Android12ChatActivityTasksFixService
    }
}