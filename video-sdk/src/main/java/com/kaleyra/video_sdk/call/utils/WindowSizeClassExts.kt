package com.kaleyra.video_sdk.call.utils

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

internal object WindowSizeClassExts {
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
}