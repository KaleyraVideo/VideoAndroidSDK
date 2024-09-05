package com.kaleyra.video_common_ui

import android.content.Context

/**
 * KaleyraVideoInitializer
 *
 * Representation of a class capable of being started from within KaleyraVideoSDK and being used to configure and connect KaleyraVideoSDK when required.
 */
abstract class KaleyraVideoInitializer {

    /**
     * Application context provided when the class is instantiated from within the SDK. Useful for SDK configuration and connection purposes.
     */
    lateinit var applicationContext: Context
        internal set

    /**
     * Callback that is invoked when the KaleyraVideoSDK is required to be configured in order to let KaleyraVideoSDK function properly.
     */
    abstract fun onRequestKaleyraVideoConfigure()

    /**
     * Callback that is invoked when the KaleyraVideoSDK is required to be connected to let KaleyraVideoSDK function properly.
     */
    abstract fun onRequestKaleyraVideoConnect()
}
