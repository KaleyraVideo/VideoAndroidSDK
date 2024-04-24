package com.kaleyra.video_common_ui.utils

import android.content.Intent

internal const val GCM_RECEIVE_ACTION = "com.google.android.c2dm.intent.RECEIVE"
internal const val GCM_TYPE = "gcm"
internal const val GOOGLE_IID_FROM = "google.com/iid"
internal const val MESSAGE_TYPE_EXTRA_KEY = "message_type"

internal fun isKaleyraPushNotificationPayload(payload: String): Boolean =
    payload.contains("on_call_incoming") || payload.contains("on_message_sent")

internal fun Intent.isFcmIntent(): Boolean {
    if (GCM_RECEIVE_ACTION != action) return false

    if (extras == null) return false

    val messageType = getStringExtra(MESSAGE_TYPE_EXTRA_KEY)
    val isGcmMessageType =  messageType == null || GCM_TYPE == messageType
    if (!isGcmMessageType) return false

    val isMessageFromIid = GOOGLE_IID_FROM == extras?.getString("from")
    if (isMessageFromIid) return false

    return true
}
