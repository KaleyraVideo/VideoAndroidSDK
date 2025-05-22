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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.stream.view.items.UserLabel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

/**
 * Pointer size in dp
 */
val PointerSize = 24.dp

@Composable
internal fun TextPointer(
    username: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Pointer()
        UserLabel(
            modifier = Modifier.offset(PointerSize / 1f, PointerSize / 1.2f),
            username = username,
            pin = false,
        )
    }
}

@Composable
internal fun Pointer(modifier: Modifier = Modifier) {

    val primaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onSecondary

    Canvas(modifier = modifier.size(PointerSize)) {
        val cornerRadius = 16f
        val border = (size.width / 6f).toDp()

        fun Rect.createPath() = Path().apply {
            moveTo(topCenter.x, topCenter.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottomRight.x - width / 2f, bottomCenter.y - width * 0.18f)
            lineTo(bottomLeft.x + width / 2f, bottomCenter.y - width * 0.18f)
            lineTo(bottomLeft.x, bottomLeft.y)
            close()
        }

        val foregroundPath = Rect(
            Offset(border.toPx() / 2, border.toPx() / 2),
            Size(width = size.width - border.toPx(), height = size.height - border.toPx())
        ).createPath()


        val backgroundPath = Rect(
            Offset(0f, -(border.value / 6f)),
            Size(width = size.width, height = size.height)
        ).createPath()

        // Adding a path effect of rounded corners
        drawIntoCanvas { canvas ->
            canvas.translate(size.width / 2f, size.height / 2f)
            canvas.rotate(-56f)
            canvas.translate(-size.width / 2f, -size.height / 2f)
            canvas.drawPath(backgroundPath.apply {
            }, paint = Paint().apply {
                color = onPrimaryColor
                pathEffect = PathEffect.cornerPathEffect(cornerRadius)
            })

            canvas.drawOutline(
                outline = Outline.Generic(foregroundPath),
                paint = Paint().apply {
                    color = primaryColor
                    pathEffect = PathEffect.cornerPathEffect(cornerRadius / 2f)
                }
            )

        }
    }
}

@MultiConfigPreview
@Composable
internal fun PointerPreview() {
    KaleyraTheme {
        Surface {
            TextPointer(username = "user 1", modifier = Modifier.size(428.dp))
        }
    }
}