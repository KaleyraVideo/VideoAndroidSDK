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

package com.kaleyra.video_sdk.call.feedback.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

// Default slider thumb radius in the material library
private val ThumbRadius = 10.dp
private val StarSize = 36.dp
private val StarSpacing = 36.dp
private val SliderWidth = 220.dp

/**
 * Star Slider Tag
 */
val StarSliderTag = "StarSliderTag"

@Composable
internal fun StarSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    levels: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thumbSize = remember { with(density) { ThumbRadius.toPx().toInt() } }
    Layout(
        content = {
            Slider(
                onValueChange = onValueChange,
                value = value,
                steps = levels - 2,
                valueRange = 1f.rangeTo(levels.toFloat()),
                modifier = Modifier
                    .alpha(0f)
                    .width(SliderWidth)
                    .testTag(StarSliderTag)
            )
            repeat(levels) { index ->
                Box(modifier = Modifier.size(StarSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_full_star),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier
                            .matchParentSize()
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_full_star),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                alpha = if (index <= value - 1) 1f else 0f
                            }
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val sliderPlaceable = measurables[0].measure(constraints)
        val starPlaceables = measurables
            .drop(1)
            .take(levels)
            .map { it.measure(constraints) }
        val startHeight = starPlaceables[0].height
        val offsetY = sliderPlaceable.height / 2 - startHeight / 2

        layout(sliderPlaceable.width, sliderPlaceable.height) {
            sliderPlaceable.placeRelative(0, 0)

            val offsetX = StarSpacing.toPx().toInt()
            starPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(x = (thumbSize * index) + (offsetX * index), y = offsetY)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun StarSliderPreview() = KaleyraM3Theme {
    StarSlider(
        value = 3f,
        onValueChange = {},
        levels = 5,
    )
}
