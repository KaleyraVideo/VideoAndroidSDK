package com.kaleyra.video_common_ui.notification.receiver.fcm


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaleyra.video_common_ui.KaleyraVideoSharedPrefs
import com.kaleyra.video_common_ui.PushNotificationHandlingStrategy
import com.kaleyra.video_common_ui.notification.payloadworker.PushNotificationPayloadWorker
import com.kaleyra.video_common_ui.utils.isFcmIntent
import com.kaleyra.video_common_ui.utils.isKaleyraPushNotificationPayload
import com.kaleyra.video_common_ui.utils.toJSONObject
import com.kaleyra.video_utils.ContextRetainer

internal class FcmPushNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // check if is from gcm/fcm, else return because it is not a notification
        if (!intent.isFcmIntent()) {
            Log.i("KaleyraVideoSDK", "Received fcm push notification with no KaleyraVideoSDK push payload data.")
            if (isOrderedBroadcast) runCatching { resultCode = Activity.RESULT_OK }
            return
        }

        val fcmPayload = intent.extras?.toJSONObject()?.toString()?.takeIf { isKaleyraPushNotificationPayload(it) }

        if (fcmPayload == null) {
            Log.i("KaleyraVideoSDK", "Received fcm push notification with empty KaleyraVideoSDK push payload data.")
            runCatching { resultCode = Activity.RESULT_OK }
            return
        }

        if (!isKaleyraPushNotificationPayload(fcmPayload)) {
            Log.i("KaleyraVideoSDK", "Received fcm push notification with no KaleyraVideoSDK push payload data.")
            runCatching { resultCode = Activity.RESULT_OK }
            return
        }

        if (KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() != PushNotificationHandlingStrategy.Automatic) {
            Log.i("KaleyraVideoSDK", "Automatic push notification processing is currently disabled.")
            return
        }

        Log.i("KaleyraVideoSDK", "Received fcm push notification with KaleyraVideoSDK push payload data, enqueuing push payload worker.")
        val mRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(PushNotificationPayloadWorker::class.java).build()
        WorkManager.getInstance(ContextRetainer.context).enqueue(mRequest)

        if (isOrderedBroadcast) abortBroadcast()
        runCatching { resultCode = Activity.RESULT_OK }
    }
}
