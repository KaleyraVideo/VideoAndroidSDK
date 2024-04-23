package com.kaleyra.video_sdk.common.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
internal fun ReversibleRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    reverseLayout: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val localDirection = LocalLayoutDirection.current
    val direction = if (reverseLayout) {
        when (localDirection) {
            LayoutDirection.Rtl -> LayoutDirection.Ltr
            else -> LayoutDirection.Rtl
        }
    } else localDirection
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Row(modifier, horizontalArrangement, verticalAlignment) {
            CompositionLocalProvider(LocalLayoutDirection provides localDirection) {
                content()
            }
        }
    }
}