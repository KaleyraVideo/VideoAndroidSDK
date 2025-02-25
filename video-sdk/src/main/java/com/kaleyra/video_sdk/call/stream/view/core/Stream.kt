@file:OptIn(FlowPreview::class)

package com.kaleyra.video_sdk.call.stream.view.core

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

const val StreamViewTestTag = "StreamViewTestTag"
internal const val RenderingDebounceMillis = 1000L
private const val StreamElevationShadowAnimationDuration = 750
private val StreamMaxElevation = 30.dp

@Composable
internal fun Stream(
    streamView: ImmutableView<VideoStreamView>?,
    userInfo: UserInfo?,
    showStreamView: Boolean,
    isMine: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    avatarModifier: Modifier = Modifier
) {
    val shadowElevation by animateDpAsState(
        targetValue = if (isSpeaking) StreamMaxElevation else 0.dp,
        animationSpec = tween(StreamElevationShadowAnimationDuration),
        label = "shadowElevation"
    )

    StreamLayout(
        modifier = modifier.fillMaxSize(),
        streamView = streamView,
        showStreamView = showStreamView,
        onClick = onClick,
        avatar = {
            val shadowColor = MaterialTheme.colorScheme.surfaceContainerLow
            val userInfos = remember(userInfo) { ImmutableList(listOfNotNull(userInfo)) }
            StreamAvatar(
                userInfos = userInfos,
                avatarCount = 1,
                isMine = isMine,
                isSpeaking = isSpeaking,
                modifier = avatarModifier
                    .drawWithContent {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(shadowColor, Color.Transparent),
                                radius = (size.width /2f) + shadowElevation.toPx()
                            ),
                            radius = (size.width /2f) + shadowElevation.toPx(),
                        )
                        drawContent()
                    }
            )
        }
    )
}

@Composable
internal fun StreamLayout(
    modifier: Modifier,
    streamView: ImmutableView<VideoStreamView>?,
    showStreamView: Boolean,
    onClick: (() -> Unit)? = null,
    avatar: @Composable () -> Unit
) {
    var forceDisplayAvatar by remember { mutableStateOf(false) }
    var isRendering by remember { mutableStateOf(false) }
    val streamViewAlpha by animateFloatAsState(
        targetValue = if (isRendering) 1f else 0f,
        animationSpec = tween(),
        label = "stream view alpha"
    )

    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
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
            avatar()
        }
    }

    if (streamView != null) {
        LaunchedEffect(streamView) {
            streamView.value.state
                .map { it is StreamView.State.Rendering }
                .onEach { isRendering = it }
                // If the view doesn't render within the timeout period, the avatar is displayed as a fallback.
                .debounce { if (it) 0 else RenderingDebounceMillis }
                .onEach { forceDisplayAvatar = !it }
                .launchIn(this)
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun StreamPreview() {
    KaleyraTheme {
        Surface {
            Stream(
                streamView = null,
                showStreamView = true,
                isMine = false,
                isSpeaking = false,
                userInfo = UserInfo("userId1", "John", ImmutableUri(Uri.EMPTY))
            )
        }
    }
}

