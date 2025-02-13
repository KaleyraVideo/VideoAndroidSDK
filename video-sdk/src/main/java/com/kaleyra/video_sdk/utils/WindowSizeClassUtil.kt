package com.kaleyra.video_sdk.utils

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

internal object WindowSizeClassUtil {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    fun currentWindowAdaptiveInfo(configuration: Configuration): WindowSizeClass {
        val size = DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
        return WindowSizeClass.calculateFromSize(size)
    }

    fun WindowSizeClass.isAtLeastMediumWidth(): Boolean {
        return widthSizeClass in setOf(WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded)
    }

    fun WindowSizeClass.isAtLeastExpandedWidth(): Boolean {
        return widthSizeClass in setOf(WindowWidthSizeClass.Expanded)
    }

    fun WindowSizeClass.hasCompactHeight(): Boolean {
        return heightSizeClass == WindowHeightSizeClass.Compact
    }

    fun WindowSizeClass.hasCompactWidth(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Compact
    }

    fun WindowSizeClass.hasExpandedWidth(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Expanded
    }

    fun WindowSizeClass.hasMediumWidth(): Boolean {
        return widthSizeClass == WindowWidthSizeClass.Medium
    }

    fun WindowSizeClass.isCompactInAnyDimension(): Boolean {
        return hasCompactHeight() || hasCompactWidth()
    }

    fun WindowSizeClass.isLargeScreen(): Boolean {
        return isAtLeastMediumWidth() && !hasCompactHeight()
    }
}
