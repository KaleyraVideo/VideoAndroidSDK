package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_sdk.call.mapper.toCustomAlertMessage
import org.junit.Assert
import org.junit.Test

class FloatingMessageMapperTest {

   @Test
   fun testFloatingMessageToAlertCustomMessageMapping() {
       val action = {}
       val floatingMessage = FloatingMessage(body = "body", FloatingMessage.Button("text", 42, action))

       val customAlertMessage = floatingMessage.toCustomAlertMessage()

       Assert.assertEquals(floatingMessage.body, customAlertMessage.body)
       Assert.assertEquals(floatingMessage.button?.text!!, customAlertMessage.button?.text!!)
       Assert.assertEquals(floatingMessage.button?.icon!!, customAlertMessage.button?.icon!!)
       Assert.assertEquals(floatingMessage.button?.action!!, customAlertMessage.button?.action!!)
   }
}