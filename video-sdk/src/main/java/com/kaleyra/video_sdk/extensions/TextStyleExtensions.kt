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

package com.kaleyra.video_sdk.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheet.toPixel

internal object TextStyleExtensions {

    private var shadowAmountFontSizeMultiplier = 0.05f

    @Composable
    fun TextStyle.shadow(color: Color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.7f)): TextStyle {
        val shadowAmount: Float = when(fontSize.type.toString()) {
            "Sp", "Em" -> (fontSize.times(shadowAmountFontSizeMultiplier)).value.dp.toPixel
            else -> 0.7.dp.toPixel
        }
        return copy(
            shadow = Shadow(
                blurRadius = shadowAmount,
                color = color,
                offset = Offset(x = shadowAmount, y = shadowAmount)
            )
        )
    }
}
