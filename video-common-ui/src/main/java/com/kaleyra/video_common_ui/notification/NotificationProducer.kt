package com.kaleyra.video_common_ui.notification

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_utils.ContextRetainer

internal abstract class NotificationProducer : ActivityLifecycleCallbacks {

    internal var notificationPresentationHandler: NotificationPresentationHandler? = null

    open fun bind(call: CallUI) {
        (ContextRetainer.context as Application).registerActivityLifecycleCallbacks(this)
    }

    open fun stop() {
        (ContextRetainer.context as Application).unregisterActivityLifecycleCallbacks(this)
        notificationPresentationHandler = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = getNotificationPresentationHandler(activity)

    override fun onActivityResumed(activity: Activity) = getNotificationPresentationHandler(activity)

    override fun onActivityDestroyed(activity: Activity) {
        if (notificationPresentationHandler != null && activity != notificationPresentationHandler) return
        notificationPresentationHandler = null
    }

    private fun getNotificationPresentationHandler(activity: Activity) {
        if (activity !is NotificationPresentationHandler) return
        notificationPresentationHandler = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}