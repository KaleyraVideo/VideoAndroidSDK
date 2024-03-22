package com.kaleyra.video_common_ui

/**
 * PushNotificationHandlingStrategy represents the option to automatically or manually manage the KaleyraVideo SDK push notification processing.
 * When using PushNotificationHandlingStrategy.Automatic option the KaleyraVideoSDK push payloads will be automatically intercepted by the KaleyraVideoSDK.
 * Whenever a KaleyraVideoSDK push payload is received, by implementing KaleyraVideoService the SDK will invoke
 * onRequestKaleyraVideoConfigure and onRequestKaleyraVideoConnect functions in order to let integrating app configure and connect the SDK at the right time.
 * When using PushNotificationHandlingStrategy.Manual option the KaleyraVideoSDK push payloads will NOT be automatically intercepted by the KaleyraVideoSDK, so
 * it will be necessary in the integrating app to implement FCM/HMS(Huawei) push notification receivers in order to intercept the KaleyraVideoSDK push payloads and
 * manually configure and connect the SDK to process the notifications.
 */
enum class PushNotificationHandlingStrategy {

    /**
     * Automatic Push Notification Interceptor Option
     */
    Automatic,

    /**
     * Manual Push Notification Interceptor Option
     */
    Manual
}