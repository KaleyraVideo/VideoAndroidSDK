package com.kaleyra.video_common_ui

/**
 * ConnectionServiceOptions represents the options available to configure the Android connection service capability on the Kaleyra Video SDK.
 **/
enum class ConnectionServiceOption {
    /**
     * The android connection service will be enabled and enforced when targeting at least api 28.
     * In order to place an outgoing call or receive calls the logged user will have to accept the "android.permission.CALL_PHONE" permission as required.
     * If the "android.permission.CALL_PHONE" is denied, no outgoing calls will be placed and the incoming calls will be automatically declined.
     **/
    Enabled,

    /**
     * The android connection service will be used when targeting at least api 28 and the logged user accepts the "android.permission.CALL_PHONE" permission when placing or receiving a call.
     * When the logged user denies the android.permission.CALL_PHONE" permission all the calls will be connected anyway but no one will not be promoted as a system call.
     **/
    Optional,

    /**
     * The android connection service is disabled.
     * No incoming or outgoing calls will be promoted as a system call.
     **/
    Disabled
}