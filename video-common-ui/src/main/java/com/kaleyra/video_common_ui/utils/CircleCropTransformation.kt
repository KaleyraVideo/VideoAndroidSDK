package com.kaleyra.video_common_ui.utils

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import coil.size.Size
import coil.transform.Transformation

/**
 * A [Transformation] that crops an image using a centered circle as the mask.
 *
 * If you're using Jetpack Compose, use `Modifier.clip(CircleShape)` instead of this transformation
 * as it's more efficient.
 *
 * @param backgroundColor backgroundColor of the resulting cropped bitmap
 */
internal class CircleCropTransformation(val backgroundColor: Int) : Transformation {

    override val cacheKey: String = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        backgroundPaint.color = backgroundColor
        val cropPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val minSize = minOf(input.width, input.height)
        val radius = minSize / 2f
        val output = createBitmap(minSize, minSize, input.config ?: Bitmap.Config.ARGB_8888)
        output.applyCanvas {
            drawCircle(radius, radius, radius, backgroundPaint)
            cropPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            drawBitmap(input, radius - input.width / 2f, radius - input.height / 2f, cropPaint)
        }

        return output
    }

    override fun equals(other: Any?) = other is CircleCropTransformation

    override fun hashCode() = javaClass.hashCode()
}