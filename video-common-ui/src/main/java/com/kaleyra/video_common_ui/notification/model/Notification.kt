package com.kaleyra.video_common_ui.notification.model

import androidx.core.app.NotificationCompat

/**
 * Sealed class representing different types of accessory notifications within the application.
 *
 * This class serves as a base for defining various notification scenarios, allowing for
 * a structured and type-safe approach to handling different notification types.
 */
sealed class Notification {

    /**
     * Represents a notification related to downloading a file.
     *
     * This notification type might be used to inform the user about the progress or completion
     * of a file download operation.
     */
    data object DownloadFile : Notification()

    /**
     * Represents a notification related to signing a document.
     *
     * This notification type could be used to alert the user about documents that require their
     * signature or to provide updates on the signing process.
     */
    data object SignDocument : Notification()

    /**
     * Sealed class representing the presentation mode or priority of a notification.
     *
     * This allows for fine-grained control over how notifications are displayed to the user,
     * ranging from being completely hidden to having high or low priority.
     */
    sealed class PresentationMode {

        /**
         * Indicates that the notification should be hidden from the user.
         *
         * This mode might be used when a notification is no longer relevant or should be suppressed
         * under certain conditions.
         */
        data object Hidden : PresentationMode()

        /**
         * Indicates that the notification should be presented with high priority.
         *
         * High-priority notifications are typically more prominent and may interrupt the user
         * to ensure they are promptly seen.
         */
        data object HighPriority : PresentationMode()

        /**
         * Indicates that the notification should be presented with low priority.
         *
         * Low-priority notifications are less intrusive and might only appear in the status bar
         * or notification shade, avoiding immediate interruption of the user.
         */
        data object LowPriority : PresentationMode()
    }
}

internal const val PRIORITY_HIDDEN = Int.MIN_VALUE

internal fun Notification.PresentationMode.toNotificationCompatPriority(): Int = when (this) {
    Notification.PresentationMode.Hidden -> PRIORITY_HIDDEN
    Notification.PresentationMode.HighPriority -> NotificationCompat.PRIORITY_HIGH
    Notification.PresentationMode.LowPriority -> NotificationCompat.PRIORITY_LOW
}
