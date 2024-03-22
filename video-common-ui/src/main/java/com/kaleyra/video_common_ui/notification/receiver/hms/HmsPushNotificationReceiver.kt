package com.kaleyra.video_common_ui.notification.receiver.hms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaleyra.video_common_ui.KaleyraVideoSharedPrefs
import com.kaleyra.video_common_ui.PushNotificationHandlingStrategy
import com.kaleyra.video_common_ui.notification.payloadworker.PushNotificationPayloadWorker
import com.kaleyra.video_common_ui.utils.isKaleyraPushNotificationPayload
import com.kaleyra.video_utils.ContextRetainer
import java.nio.charset.StandardCharsets

internal class HmsPushNotificationReceiver : BroadcastReceiver() {

    companion object {
        internal const val PUSH_PAYLOAD_BYTE_ARRAY_KEY = "msg_data"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val huaweiPayload: String? = intent.extras?.getByteArray(PUSH_PAYLOAD_BYTE_ARRAY_KEY)?.toString(StandardCharsets.UTF_8)
        if (huaweiPayload == null) {
            Log.i("KaleyraVideoSDK", "Received hms push notification with empty data...")
            return
        }
        if (!isKaleyraPushNotificationPayload(huaweiPayload)) {
            Log.i("KaleyraVideoSDK", "Received hms push notification is not a KaleyraVideoSDK push payload.")
            return
        }

        if (KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() != PushNotificationHandlingStrategy.Automatic) {
            Log.i("KaleyraVideoSDK", "Automatic push notification processing is currently disabled.")
            return
        }

        Log.i("KaleyraVideoSDK", "Received hms push notification with KaleyraVideoSDK push payload data, enqueuing push payload worker...")

        val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(PushNotificationPayloadWorker::class.java).build()
        WorkManager.getInstance(ContextRetainer.context).enqueue(mRequest)
    }
}
