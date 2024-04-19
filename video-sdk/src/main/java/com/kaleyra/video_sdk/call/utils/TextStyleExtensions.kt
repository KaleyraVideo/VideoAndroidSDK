package com.kaleyra.video_sdk.call.utils

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

internal object TextStyleExtensions {

    fun TextStyle.clearFontPadding(): TextStyle {
        return copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    }
}