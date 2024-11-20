package com.kaleyra.video_sdk.extensions

import android.content.res.Resources
import androidx.compose.ui.unit.Dp

internal object DpExtensions {

    internal val Dp.toPixel: Float
        get() = value * Resources.getSystem().displayMetrics.density
}