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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getScale
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getSize
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getTranslation
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Composable
internal fun PointerStreamWrapper(
    modifier: Modifier = Modifier,
    streamView: ImmutableView?,
    pointerList: ImmutableList<PointerUi>?,
    stream: @Composable (hasPointers: Boolean) -> Unit
) {
    val size by getSize(streamView)
    val translation by getTranslation(streamView)
    val scale by getScale(streamView)
    Box(contentAlignment = Alignment.Center) {
        stream(!pointerList.isNullOrEmpty())
        Box(
            modifier = modifier
                .size(size.toDpSize())
                .graphicsLayer {
                    translationX = translation[0]
                    translationY = translation[1]
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale[0]
                    scaleY = scale[1]
                }
        ) {
            pointerList?.value?.forEach { MovablePointer(it, size, scale) }
        }
    }
}

@Composable
internal fun IntSize.toDpSize() = with(LocalDensity.current) {
    DpSize(width.toDp(), height.toDp())
}

@Composable
private fun getSize(view: ImmutableView?): State<IntSize> {
    val view = view?.value as? VideoStreamView
    val defaultValue = IntSize(0,0)
    val fallback = remember { mutableStateOf(defaultValue) }
    return view?.getSize()?.collectAsStateWithLifecycle(defaultValue) ?: fallback
}

@Composable
private fun getTranslation(view: ImmutableView?): State<FloatArray> {
    val view = view?.value as? VideoStreamView
    val defaultValue = floatArrayOf(0f, 0f)
    val fallback = remember { mutableStateOf(defaultValue) }
    return view?.getTranslation()?.collectAsStateWithLifecycle(defaultValue) ?: fallback
}

@Composable
private fun getScale(view: ImmutableView?): State<FloatArray> {
    val view = view?.value as? VideoStreamView
    val defaultValue = floatArrayOf(1f, 1f)
    val fallback = remember { mutableStateOf(defaultValue) }
    return view?.getScale()?.collectAsStateWithLifecycle(defaultValue) ?: fallback
}

private fun ImmutableList<PointerUi>?.isNullOrEmpty() = this == null || this.count() == 0