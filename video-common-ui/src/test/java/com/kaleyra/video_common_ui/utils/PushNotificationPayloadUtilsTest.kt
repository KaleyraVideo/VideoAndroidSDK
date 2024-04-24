package com.kaleyra.video_common_ui.utils

import android.content.Intent
import org.junit.Assert
import org.junit.Test

class PushNotificationPayloadUtilsTest {

    @Test
    fun isFcmIntent_wrongAction_false() {
        val isFcmIntent = Intent().isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun isFcmIntent_emptyExtras_false() {
        val isFcmIntent = Intent(GCM_RECEIVE_ACTION).isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun isFcmIntent_nullMessageType_false() {
        val inputIntent = Intent(GCM_RECEIVE_ACTION)
        val messageTypeString: String? = null
        inputIntent.putExtra(MESSAGE_TYPE_EXTRA_KEY, messageTypeString)
        val isFcmIntent = inputIntent.isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun isFcmIntent_gcmMessageType_false() {
        val inputIntent = Intent(GCM_RECEIVE_ACTION)
        inputIntent.putExtra(MESSAGE_TYPE_EXTRA_KEY, GCM_TYPE)
        val isFcmIntent = inputIntent.isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun isFcmIntent_messageFromGoogleIid_false() {
        val inputIntent = Intent(GCM_RECEIVE_ACTION)
        inputIntent.putExtra("from", GOOGLE_IID_FROM)
        val isFcmIntent = inputIntent.isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun isFcmIntent_messageFromFcm_true() {
        val inputIntent = Intent(GCM_RECEIVE_ACTION)
        inputIntent.putExtra("payload", "test")
        val isFcmIntent = inputIntent.isFcmIntent()
        Assert.assertEquals(false, isFcmIntent)
    }

    @Test
    fun testIsIncomingCallKaleyraPushNotificationPayload() {
        val isKaleyraPushPayload = isKaleyraPushNotificationPayload(">>>>>on_call_incoming>>>>>")
        Assert.assertEquals(true, isKaleyraPushPayload)
    }

    @Test
    fun testIsIncomingChatMessageKaleyraPushNotificationPayload() {
        val isKaleyraPushPayload = isKaleyraPushNotificationPayload(">>>>>on_message_sent>>>>>")
        Assert.assertEquals(true, isKaleyraPushPayload)
    }

    @Test
    fun testOtherPushNotificationPayload() {
        val isKaleyraPushPayload = isKaleyraPushNotificationPayload(">>>>>other_push_payload>>>>>")
        Assert.assertEquals(false, isKaleyraPushPayload)
    }
}
