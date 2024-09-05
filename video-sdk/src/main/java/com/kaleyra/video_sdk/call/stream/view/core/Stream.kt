package com.kaleyra.video_sdk.call.stream.view.core

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar

const val StreamViewTestTag = "StreamViewTestTag"

@Composable
internal fun Stream(
    streamView: ImmutableView?,
    username: String,
    avatar: ImmutableUri?,
    showStreamView: Boolean,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
    onClick: (() -> Unit)? = null
) {
    if (showStreamView && streamView != null) {
        key(streamView) {
            AndroidView(
                factory = {
                    streamView.value.also {
                        val parentView = it.parent as? ViewGroup
                        parentView?.removeView(it)
                        it.setOnClickListener { onClick?.invoke() }
                    }
                },
                update = { view ->
                    val newLayoutParams = view.layoutParams
                    newLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    newLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    view.layoutParams = newLayoutParams
                },
                modifier = Modifier.testTag(StreamViewTestTag)
            )
        }
    } else {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val min = min(maxWidth, maxHeight)
            val size = (min / 2).coerceIn(48.dp, 96.dp)
            Avatar(
                uri = avatar,
                username = username,
                placeholder = avatarPlaceholder,
                size = size,
            )
        }
    }
}