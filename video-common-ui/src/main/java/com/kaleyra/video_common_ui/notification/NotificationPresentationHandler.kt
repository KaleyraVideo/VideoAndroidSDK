package com.kaleyra.video_common_ui.notification

import com.kaleyra.video_common_ui.notification.model.Notification

/**
 * Interface for activities that handle the presentation of notifications.
 *
 * This interface defines a property that allows customization of how notifications
 * are displayed within an activity.  Activities implementing this interface
 * can control the appearance and behavior of notifications by providing a handler
 * function.
 */
interface NotificationPresentationHandler {

    /**
     * A handler function that determines the presentation mode for notifications.
     *
     * This property should be set to a function that takes a [Notification] object
     * (representing the notification to be displayed) and returns a
     * [Notification.PresentationMode] enum value.  The returned value indicates the
     * desired presentation style for the notification, such as HighPriority, LowPriority,
     * or Hidden.
     *
     * Implementing activities can provide a default handler, and other components
     * interacting with the activity can override this handler to customize notification
     * behavior as needed.
     *
     * Example:
     *
     *  notificationPresentationHandler = { notification ->
     *      when (notification) {
     *          is MyCustomNotification -> Notification.PresentationMode.HighPriority
     *          else -> Notification.PresentationMode.LowPriority
     *      }
     *  }
     */
    var notificationPresentationHandler: (Notification) -> Notification.PresentationMode
}
