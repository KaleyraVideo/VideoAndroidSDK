package com.kaleyra.app_utilities.providers

import android.content.Context
import androidx.startup.Initializer
import com.kaleyra.app_utilities.FlipperManager

/**
 * FlipperInitializer using Flipper to debug app in a easy way
 *
 * For more information visit:
 * https://github.com/facebook/flipper
 */
class FlipperInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        FlipperManager.enable(context)
    }

    override fun dependencies(): MutableList<Class<out androidx.startup.Initializer<*>>> = mutableListOf()
}