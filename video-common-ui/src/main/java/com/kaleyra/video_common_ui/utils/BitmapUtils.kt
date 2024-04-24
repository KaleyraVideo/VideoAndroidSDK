package com.kaleyra.video_common_ui.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.kaleyra.video_utils.ContextRetainer
import java.io.IOException

internal object BitmapUtils {

    private val imageLoader by lazy { ImageLoader(ContextRetainer.context) }

    internal suspend fun Uri.toBitmap(backgroundColor: Int = Color.LTGRAY): Result<Bitmap?> =
        when (
            val result = imageLoader.execute(
                ImageRequest.Builder(ContextRetainer.context)
                    .data(this)
                    .transformations(CircleCropTransformation(backgroundColor))
                    .build()
            )
        ) {
            is SuccessResult -> Result.success((result.drawable as? BitmapDrawable)?.bitmap)
            is ErrorResult -> Result.failure(result.throwable)
            else -> Result.failure(IOException("Cannot download bitmap from uri: ${toString()}"))
        }
}
