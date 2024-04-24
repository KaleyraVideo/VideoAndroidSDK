package com.kaleyra.video_common_ui.notification.receiver.hms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * This service is useful to let KaleyraVideoHuaweiNotificationReceiver receive hms payloads
 * when a hms service is implemented in the integrating app. If not implemented the broadcast
 * receiver it will never be triggered
 */
internal class HmsStubPushNotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}
