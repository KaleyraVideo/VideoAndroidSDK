package com.kaleyra.video_common_ui

import android.content.Context
import com.kaleyra.video_utils.ContextRetainer

internal object KaleyraVideoSharedPrefs {

    internal const val KALEYRA_VIDEO_SHARED_PREFS_NAME = "KALEYRA_VIDEO_SHARED_PREFS"

    private val sharedPreferences by lazy {
        ContextRetainer.context.getSharedPreferences(KALEYRA_VIDEO_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getPushNotificationHandlingStrategy() =
        PushNotificationHandlingStrategy.valueOf(sharedPreferences.getString(PushNotificationHandlingStrategy::class.simpleName, PushNotificationHandlingStrategy.Automatic.name)!!)


    fun putPushNotificationHandlingStrategy(pushNotificationInterceptorOption: PushNotificationHandlingStrategy) =
        sharedPreferences.edit().putString(PushNotificationHandlingStrategy::class.simpleName, pushNotificationInterceptorOption.name).apply()
}
