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

package com.kaleyra.video_sdk.call.pointer.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.kaleyra.video_sdk.call.pointer.model.PointerUi

/**
 * Movable pointer tag
 */
const val MovablePointerTag = "MovablePointerTag"

@Composable
internal fun MovablePointer(pointer: PointerUi, parentSize: IntSize, scale: FloatArray) {
    val offsetX by animateFloatAsState(targetValue = (pointer.x / 100) * parentSize.width)
    val offsetY by animateFloatAsState(targetValue = (pointer.y / 100) * parentSize.height)

    TextPointer(
        username = pointer.username,
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX /= scale[0]
                scaleY /= scale[1]
            }
            .testTag(MovablePointerTag)
    )
}
