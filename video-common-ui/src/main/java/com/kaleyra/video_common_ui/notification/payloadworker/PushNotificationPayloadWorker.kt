package com.kaleyra.video_common_ui.notification.payloadworker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_common_ui.requestConnect
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class PushNotificationPayloadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.i("KaleyraVideoSDK", "Enqueued KaleyraVideoSDK push payload worker...")
        Log.i("KaleyraVideoSDK", "Requesting KaleyraVideoSDK configure...")
        requestConfiguration()
        MainScope().launch {
            Log.i("KaleyraVideoSDK", "Requesting KaleyraVideoSDK connection...")
            runCatching { requestConnect() }
        }
        return Result.success()
    }
}
