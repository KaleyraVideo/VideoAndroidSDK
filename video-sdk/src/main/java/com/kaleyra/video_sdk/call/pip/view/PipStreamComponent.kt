package com.kaleyra.video_sdk.call.pip.view

import android.util.Rational
import android.util.Size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.MathUtils
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.stream.view.core.StreamPreview
import com.kaleyra.video_sdk.call.stream.view.core.StreamPreviewAvatarCount
import com.kaleyra.video_sdk.call.stream.view.core.computeStreamAvatarSize
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamStatusIcons
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.transform

internal val PipStreamViewSizeSampleTime = 250L
internal val DefaultPipSize = Size(9, 16)
internal const val PipStreamComponentTag = "PipStreamComponentTag"

internal val PipFadeInDelay = 500

internal val MaxPipStreamAvatarSize = 72.dp

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

    DisposableEffect(Unit) {
        viewModel.switchToPipStreamLayout()
        onDispose {
            viewModel.switchToDefaultStreamLayout()
        }
    }

    PipStreamComponent(
        uiState = uiState,
        onPipAspectRatio = onPipAspectRatio,
        modifier = modifier
    )
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Composable
internal fun PipStreamComponent(
    uiState: StreamUiState,
    onPipAspectRatio: (Rational) -> Unit,
    modifier: Modifier = Modifier
) {
    val streamItems = remember(uiState) { uiState.streamItems.value }
    val aspectRatioView = remember(streamItems, uiState.isScreenShareActive) {
        val streamItem = streamItems.takeIf { it.size == 1 && !uiState.isScreenShareActive }?.firstOrNull() as? StreamItem.Stream
        streamItem?.stream?.video?.view?.value
    }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(aspectRatioView) {
        var lastEmittedAspectRatio = 0f
        snapshotFlow { aspectRatioView }
            .sample(PipStreamViewSizeSampleTime)
            .flatMapLatest {
                it?.videoSize?.takeIf { !uiState.isScreenShareActive } ?: flowOf(DefaultPipSize)
            }
            .transform { size ->
                // Compute the aspect ratio of the stream view.
                val aspectRatio = size.width.toFloat() / size.height
                // Round the aspect ratio to two decimal places.
                val roundedAspectRatio = (aspectRatio * 100).toInt() / 100f
                // Check if the rounded aspect ratio is different from the last emitted one.
                // This prevents emitting duplicate or redundant aspect ratio changes.
                if (roundedAspectRatio != lastEmittedAspectRatio) emit(size)
                lastEmittedAspectRatio = roundedAspectRatio
            }
            .collect { size -> onPipAspectRatio.invoke(computePipAspectRatio(size)) }
    }

    // Ensures a smooth layout transition between the default screen and PiP mode.
    // It prevents visual artifacts on the streams when entering PiP mode.
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(PipFadeInDelay)),
        exit = fadeOut()
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag(PipStreamComponentTag)
        ) {
            if (uiState.preview != null) {
                val video = uiState.preview.video
                StreamPreview(
                    streamView = video?.view?.preCallStreamViewSettings(),
                    userInfos = uiState.preview.userInfos,
                    showStreamView = video?.view != null && video.isEnabled,
                    avatarSize = computeStreamAvatarSize(
                        maxWidth = maxWidth,
                        maxHeight = maxHeight,
                        maxAvatarSize = MaxPipStreamAvatarSize,
                        sizeRatio = StreamPreviewAvatarCount
                    ),
                    avatarModifier = Modifier.fillMaxSize()
                )

                StreamStatusIcons(
                    uiState.preview.audio,
                    uiState.preview.video,
                    fullscreen = false,
                    mine = true,
                    isSpeaking = false,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            } else {
                val streamsLayoutPadding = remember(streamItems.size, uiState.isScreenShareActive) { if (streamItems.size > 1 || uiState.isScreenShareActive) 3.dp else 0.dp }
                val streamCornerRadius = remember(streamItems.size, uiState.isScreenShareActive) { if (streamItems.size > 1 || uiState.isScreenShareActive) 4.dp else 0.dp }

                Column (modifier = modifier.padding(all = streamsLayoutPadding)) {
                    AdaptiveStreamLayout(
                        modifier = Modifier.weight(1f),
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
                                        .padding(
                                            bottom =
                                                if (uiState.isScreenShareActive) 3.dp
                                                else if (streamItems.size > 1 && index < (streamItems.size - 1)) 3.dp
                                                else 0.dp
                                        )
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
                    if (uiState.isScreenShareActive) LocalScreenSharePipItem()
                }
            }
        }
    }
}

@Composable
fun LocalScreenSharePipItem() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {

        Row (modifier = Modifier.wrapContentWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.drawable.ic_kaleyra_stream_screenshare),
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = stringResource(id = R.string.kaleyra_participants_component_screenshare)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                text = stringResource(id = R.string.kaleyra_stream_you),
            )
        }
    }
}

private fun computePipAspectRatio(size: Size): Rational {
    val gcd = MathUtils.findGreatestCommonDivisor(size.width, size.height)
    return if (gcd != 0) Rational(size.width / gcd, size.height / gcd)
    else Rational.NaN
}

@Preview
@MultiConfigPreview
@Composable
fun LocalScreenSharePipItemPreview() = KaleyraTheme {
    LocalScreenSharePipItem()
}