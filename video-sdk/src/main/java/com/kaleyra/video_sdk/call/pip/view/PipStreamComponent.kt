package com.kaleyra.video_sdk.call.pip.view

import android.util.Rational
import android.util.Size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.MathUtils
import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamStatusIcons
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings

internal val DefaultPipAspectRatio = Rational(9, 16)
internal const val PipStreamComponentTag = "PipStreamComponentTag"

@Composable
internal fun PipStreamComponent(
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    ),
    onPipAspectRatio: (Rational) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setStreamLayoutConstraints(
            mosaicStreamThreshold = 2,
            featuredStreamThreshold = 2,
            thumbnailStreamThreshold = 0
        )
    }

    PipStreamComponent(
        uiState = uiState,
        onPipAspectRatio = onPipAspectRatio,
        modifier = modifier
    )
}

@Composable
internal fun PipStreamComponent(
    uiState: StreamUiState,
    onPipAspectRatio: (Rational) -> Unit,
    modifier: Modifier = Modifier
) {
    val streamItems = remember(uiState) { uiState.streamItems.value }
    val aspectRatioView = remember(streamItems) {
        val streamItem = streamItems.takeIf { it.size == 1 }?.firstOrNull() as? StreamItem.Stream
        streamItem?.stream?.video?.view?.value
    }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(aspectRatioView) {
        if (aspectRatioView == null) onPipAspectRatio.invoke(DefaultPipAspectRatio)
        else {
            aspectRatioView.videoSize.collect { size ->
                onPipAspectRatio.invoke(computePipAspectRatio(size))
            }
        }
    }

    // Ensures a smooth layout transition between the default screen and PiP mode.
    // It prevents visual artifacts on the streams when entering PiP mode.
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)),
        exit = fadeOut()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag(PipStreamComponentTag)
        ) {
            if (uiState.preview != null) {
                val video = uiState.preview.video
                Stream(
                    modifier = Modifier,
                    streamView = video?.view?.preCallStreamViewSettings(),
                    avatar = uiState.preview.avatar,
                    username = uiState.preview.username ?: "",
                    showStreamView = video?.view != null && video.isEnabled
                )

                StreamStatusIcons(
                    uiState.preview.audio,
                    uiState.preview.video,
                    fullscreen = false,
                    mine = true,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            } else {
                val streamsLayoutPadding = remember(streamItems.size) { if (streamItems.size > 1) 3.dp else 0.dp }
                val streamCornerRadius = remember(streamItems.size) { if (streamItems.size > 1) 4.dp else 0.dp }

                Box(modifier = modifier.padding(all = streamsLayoutPadding)) {
                    AdaptiveStreamLayout(
                        thumbnailSize = 0.dp,
                        thumbnailsCount = 0
                    ) {
                        streamItems.fastForEachIndexed { index, streamItem ->
                            key(streamItem.id) {
                                Surface(
                                    shape = RoundedCornerShape(streamCornerRadius),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier
                                        .testTag(streamItem.id)
                                        .padding(bottom = if (streamItems.size > 1 && index < (streamItems.size - 1)) 3.dp else 0.dp)
                                ) {
                                    Box {
                                        when (streamItem) {
                                            is StreamItem.MoreStreams -> MoreStreamsItem(streamItem)

                                            is StreamItem.Stream -> {
                                                StreamItem(
                                                    stream = streamItem.stream,
                                                    fullscreen = false,
                                                    pin = false,
                                                    statusIconsAlignment = Alignment.TopEnd,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computePipAspectRatio(size: Size): Rational {
    val gcd = MathUtils.findGreatestCommonDivisor(size.width, size.height)
    return if (gcd != 0) Rational(size.width / gcd, size.height / gcd)
    else Rational.NaN
}