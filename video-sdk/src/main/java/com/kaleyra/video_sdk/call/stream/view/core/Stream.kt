@file:OptIn(FlowPreview::class)

package com.kaleyra.video_sdk.call.stream.view.core

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

const val StreamViewTestTag = "StreamViewTestTag"
internal const val RenderingDebouceMillis = 1000L

@Composable
internal fun Stream(
    streamView: ImmutableView<VideoStreamView>?,
    username: String,
    avatar: ImmutableUri?,
    showStreamView: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
    onClick: (() -> Unit)? = null,
    avatarModifier: Modifier = Modifier
) {
    var forceDisplayAvatar by remember { mutableStateOf(false) }
    var isRendering by remember { mutableStateOf(false) }
    val streamViewAlpha by animateFloatAsState(
        targetValue = if (isRendering) 1f else 0f,
        animationSpec = tween(),
        label = "stream view alpha"
    )

    Box(modifier) {
        // Do not remove the stream view if forceDisplayAvatar is true,
        // otherwise the stream view's rendering state would be always StreamView.State.NotRendering,
        // since the view need to be attached in order to start rendering
        if (showStreamView && streamView != null) {
            AndroidView(
                factory = {
                    streamView.value.apply {
                        val parentView = parent as? ViewGroup
                        parentView?.removeView(this)
                        setOnClickListener { onClick?.invoke() }
                    }
                },
                update = { view ->
                    val newLayoutParams = view.layoutParams
                    newLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    newLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    view.layoutParams = newLayoutParams
                },
                modifier = Modifier
                    .testTag(StreamViewTestTag)
                    .graphicsLayer { alpha = streamViewAlpha }
            )
        }


        AnimatedVisibility(
            visible = !showStreamView || streamView == null || forceDisplayAvatar,
            // Animate only if the avatar is shown because of forceDisplayAvatar true
            enter = if (forceDisplayAvatar) fadeIn(tween()) else EnterTransition.None,
            exit = ExitTransition.None
        ) {
            StreamAvatar(
                username,
                avatar,
                avatarPlaceholder = avatarPlaceholder,
                modifier = avatarModifier
            )
        }
    }

    if (streamView != null) {
        LaunchedEffect(streamView) {
            streamView.value.state
                .map { it is StreamView.State.Rendering }
                .onEach { isRendering = it }
                // This debounce is used for two purposes:
                // 1. If the view doesn't render within the timeout period, the avatar is displayed as a fallback.
                // 2. It also sets a minimum display time for the avatar if it was set as fallback for the previous case,
                // ensuring it remains visible for this duration even if the stream is rendered successfully.
                .debounce(RenderingDebouceMillis)
                .onEach { forceDisplayAvatar = !it }
                .launchIn(this)
        }
    }
}

@Composable
fun StreamAvatar(
    username: String,
    avatar: ImmutableUri?,
    modifier: Modifier = Modifier,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
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