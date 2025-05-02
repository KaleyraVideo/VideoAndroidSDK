package com.kaleyra.video_common_ui.notification.model

import androidx.core.app.NotificationCompat
import org.junit.Assert
import org.junit.Test

class NotificationPresentationModelTest {

   @Test
   fun testNotificationPresentationModeToNotificationCompatPriority() {
       Assert.assertEquals(NotificationCompat.PRIORITY_HIGH, Notification.PresentationMode.HighPriority.toNotificationCompatPriority())
       Assert.assertEquals(NotificationCompat.PRIORITY_LOW, Notification.PresentationMode.LowPriority.toNotificationCompatPriority())
       Assert.assertEquals(PRIORITY_HIDDEN, Notification.PresentationMode.Hidden.toNotificationCompatPriority())
   }
}
