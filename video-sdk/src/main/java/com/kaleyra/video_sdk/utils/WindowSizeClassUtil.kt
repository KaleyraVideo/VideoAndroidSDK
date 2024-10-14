package com.kaleyra.video_sdk.utils

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

internal object WindowSizeClassUtil {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    fun currentWindowAdaptiveInfo(configuration: Configuration = LocalConfiguration.current): WindowSizeClass {
        val size = DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
        return WindowSizeClass.calculateFromSize(size)
    }

    fun WindowSizeClass.isAtLeastMediumWidth(): Boolean {
        return widthSizeClass in setOf(WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded)
    }
}
