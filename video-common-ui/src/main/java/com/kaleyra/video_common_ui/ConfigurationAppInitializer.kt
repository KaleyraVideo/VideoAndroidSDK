package com.kaleyra.video_common_ui

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigurationAppInitializer: Initializer<Unit> {

    override fun create(context: Context) {
        Log.e("app initializer", "configuring kaleyra video on thread: ${Thread.currentThread()}")
        CoroutineScope(Dispatchers.Main.immediate)
            .launch { requestConfiguration() }
            .invokeOnCompletion {  error ->
                Log.e("app initializer", "has error: ${error?.message ?: ""}")
                Log.e("app initializer", "kaleyra video is configured: ${KaleyraVideo.isConfigured}")
            }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(ContextRetainer::class.java)
}