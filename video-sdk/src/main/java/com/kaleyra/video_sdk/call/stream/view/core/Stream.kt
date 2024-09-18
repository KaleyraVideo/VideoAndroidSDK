@file:OptIn(FlowPreview::class)

package com.kaleyra.video_sdk.call.stream.view.core

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeoutOrNull

const val StreamViewTestTag = "StreamViewTestTag"
internal const val RenderingTimeoutDurationMillis = 750L
internal const val MinAvatarAppearanceDurationMillis = 1000L
internal const val StreamViewHiddenAlpha = 0.01f
internal const val StreamViewShownAlpha = 1f


@Composable
internal fun Stream(
    streamView: ImmutableView<VideoStreamView>?,
    username: String,
    avatar: ImmutableUri?,
    showStreamView: Boolean,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
    onClick: (() -> Unit)? = null,
    fadeDuration: Int = LocalContext.current.resources.getInteger(android.R.integer.config_shortAnimTime)
) {
    val isVideoRendering = remember { mutableStateOf(false) }
    val forceDisplayAvatar = remember { mutableStateOf(false) }
    val displayVideo = remember { mutableStateOf(false) }
    displayVideo.value = showStreamView

    if (showStreamView && streamView != null) {
        AndroidView(
            factory = {
                streamView.value.apply {
                    val parentView = parent as? ViewGroup
                    parentView?.removeView(this)
                    setOnClickListener { onClick?.invoke() }
                    setAlphaForState(fadeDuration)
                }
            },
            update = { view ->
                val newLayoutParams = view.layoutParams
                newLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                newLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                view.layoutParams = newLayoutParams
                view.setAlphaForState(fadeDuration)
            },
            modifier = Modifier.testTag(StreamViewTestTag)
        )

        LaunchedEffect(Unit) {
            withTimeoutOrNull(RenderingTimeoutDurationMillis) {
                with(streamView.value) {
                    state.filterIsInstance<StreamView.State.Rendering>().first()
                    isVideoRendering.value = true
                    displayVideo.value = showStreamView
                    streamView.value.setAlphaForState(fadeDuration)
                }
            }

            if (isVideoRendering.value) return@LaunchedEffect

            isVideoRendering.value = false
            displayVideo.value = false
            forceDisplayAvatar.value = true

            delay(MinAvatarAppearanceDurationMillis)

            streamView.value.state.filterIsInstance<StreamView.State.Rendering>()
                .onEach {
                    isVideoRendering.value = true
                    displayVideo.value = showStreamView
                    forceDisplayAvatar.value = false
                    streamView.value.setAlphaForState(fadeDuration)
                }
                .launchIn(this)
                .invokeOnCompletion {
                    displayVideo.value = false
                    isVideoRendering.value = false
                }
        }
    }

    AnimatedVisibility(
        visible = !displayVideo.value || streamView == null || forceDisplayAvatar.value,
        enter = fadeIn(animationSpec = tween(durationMillis = fadeDuration, easing = LinearEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 0, easing = LinearEasing))
    ) {
        StreamAvatar(username, avatar, avatarPlaceholder)
    }
}

private fun VideoStreamView.setAlphaForState(fadeDuration: Int) =
    if (state.value is StreamView.State.NotRendering) alpha = StreamViewHiddenAlpha
    else animate().setDuration(fadeDuration.toLong()).alpha(StreamViewShownAlpha).start()

@Composable
fun StreamAvatar(
    username: String,
    avatar: ImmutableUri?,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar,
) {
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