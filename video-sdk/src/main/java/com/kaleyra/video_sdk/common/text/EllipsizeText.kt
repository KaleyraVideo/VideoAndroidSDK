/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.common.text

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Ellipsize
 * @property value TruncateAt truncate at
 * @constructor
 */
enum class Ellipsize(val value: TextUtils.TruncateAt) {

    /**
     * Start Truncate
     */
    Start(TextUtils.TruncateAt.START),

    /**
     * Middle Truncate
     */
    Middle(TextUtils.TruncateAt.MIDDLE),

    /**
     * End Truncate
     */
    End(TextUtils.TruncateAt.END),

    /**
     * Marquee Truncate
     */
    Marquee(TextUtils.TruncateAt.MARQUEE)
}

// Replace this when compose Text will support overflow middle ellipsize
@Composable
internal fun EllipsizeText(
    text: String,
    color: Color = LocalContentColor.current,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? = LocalTextStyle.current.fontFamily,
    fontSize: TextUnit = 16.sp,
    textAlignment: Int? = null,
    ellipsize: Ellipsize,
    shadow: Shadow? = null,
    modifier: Modifier = Modifier,
) {

    AndroidView(
        factory = { context -> TextView(context) },
        update = { textView ->
            val tf = fontFamily?.let {
                createFontFamilyResolver(textView.context).resolve(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight
                ).value as Typeface
            }

            with(textView) {
                this.text = text

                maxLines = 1
                textSize = fontSize.value
                textAlignment?.let { this.textAlignment = it }
                setTextColor(color.toArgb())
                tf.let { typeface = it }
                this.ellipsize = ellipsize.value

                if (ellipsize == Ellipsize.Marquee) {
                    isSingleLine = true
                    isSelected = true
                    marqueeRepeatLimit = -1
                }

                if (shadow != null) {
                    setShadowLayer(
                        shadow.blurRadius,
                        shadow.offset.x,
                        shadow.offset.y,
                        shadow.color.toArgb()
                    )
                }
            }
        },
        modifier = modifier.semantics { contentDescription = text }
    )
}
