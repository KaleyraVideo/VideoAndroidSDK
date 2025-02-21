package com.kaleyra.video_sdk.call.stream

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.stream.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.view.core.StreamPreview
import com.kaleyra.video_sdk.call.stream.view.items.ActiveScreenShareIndicator
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasCompactHeight
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasExpandedWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isCompactInAnyDimension
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isLargeScreen

internal val StreamItemExpandedSpacing = 6.dp
internal val StreamItemSpacing = 4.dp

internal object StreamComponentDefaults {

    const val MaxThumbnailStreams = 3

    const val MaxMosaicStreamsCompact = 8
    const val MaxMosaicStreamsExpanded = 15

    const val MaxPinnedStreamsCompact = 2
    const val MaxPinnedStreamsExpanded = 6

    val MaxThumbnailSize = 180.dp

    val CornerRadius = 4.dp

    fun maxMosaicStreamsFor(windowSizeClass: WindowSizeClass): Int {
        return if (windowSizeClass.isCompactInAnyDimension()) {
            MaxMosaicStreamsCompact
        } else MaxMosaicStreamsExpanded
    }

    fun maxPinnedStreamsFor(windowSizeClass: WindowSizeClass): Int {
        return if (windowSizeClass.isCompactInAnyDimension()) {
            MaxPinnedStreamsCompact
        } else MaxPinnedStreamsExpanded
    }

    fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement {
        return when {
            windowSizeClass.hasCompactHeight() -> ThumbnailsArrangement.End
            windowSizeClass.hasExpandedWidth() -> ThumbnailsArrangement.Start
            else -> ThumbnailsArrangement.Bottom
        }
    }
}

@Composable
internal fun StreamComponent(
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    ),
    windowSizeClass: WindowSizeClass,
    onStreamItemClick: (StreamItem.Stream) -> Unit,
    onMoreParticipantClick: () -> Unit,
    selectedStreamId: String? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(windowSizeClass) {
        viewModel.setStreamLayoutConstraints(
            mosaicStreamThreshold = StreamComponentDefaults.maxMosaicStreamsFor(windowSizeClass),
            featuredStreamThreshold = StreamComponentDefaults.maxPinnedStreamsFor(windowSizeClass),
            thumbnailStreamThreshold = StreamComponentDefaults.MaxThumbnailStreams
        )
    }

    StreamComponent(
        uiState = uiState,
        windowSizeClass = windowSizeClass,
        selectedStreamId = selectedStreamId,
        onStreamClick = onStreamItemClick,
        onStopScreenShareClick = viewModel::tryStopScreenShare,
        onMoreParticipantClick = onMoreParticipantClick,
        modifier = modifier
    )
}

@Composable
internal fun StreamComponent(
    uiState: StreamUiState,
    windowSizeClass: WindowSizeClass,
    selectedStreamId: String?,
    onStreamClick: (StreamItem.Stream) -> Unit,
    onStopScreenShareClick: () -> Unit,
    onMoreParticipantClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxThumbnailStreams: Int = StreamComponentDefaults.MaxThumbnailStreams,
    thumbnailsArrangement: ThumbnailsArrangement = remember(windowSizeClass) {
        StreamComponentDefaults.thumbnailsArrangementFor(windowSizeClass)
    },
    maxThumbnailSize: Dp = StreamComponentDefaults.MaxThumbnailSize,
) {
    Box(contentAlignment = Alignment.Center) {
        if (uiState.preview != null) {
            val video = uiState.preview.video
            if (video?.view != null || !uiState.preview.isStartingWithVideo) {
                LookaheadScope {
                    StreamPreview(
                        streamView = video?.view?.preCallStreamViewSettings(),
                        userInfos = uiState.preview.userInfos,
                        showStreamView = video?.view != null && video.isEnabled,
                        avatarModifier = modifier,
                        modifier = Modifier
                            .animateConstraints()
                            .animatePlacement(this)
                    )
                }
            }
        } else {
            Column(modifier) {
                val itemSpacing = if (windowSizeClass.isLargeScreen()) StreamItemExpandedSpacing else StreamItemSpacing
                AnimatedVisibility(
                    visible = uiState.isScreenShareActive,
                    content = {
                        Box(Modifier.padding(itemSpacing)) {
                            ActiveScreenShareIndicator(onStopClick = onStopScreenShareClick)
                        }
                    }
                )
                BoxWithConstraints(contentAlignment = Alignment.Center) {
                    // Calculate thumbnail size based on available space
                    val thumbnailSize = remember(maxWidth, maxHeight, maxThumbnailStreams, maxThumbnailSize) {
                        calculateThumbnailsSize(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            maxThumbnailsStreams = maxThumbnailStreams,
                            maxThumbnailSize = maxThumbnailSize
                        )
                    }

                    LookaheadScope {
                        val itemModifier = Modifier
                            .fillMaxSize()
                            .padding(itemSpacing)
                            .animateConstraints()
                            .animatePlacement(this@LookaheadScope)

                        AdaptiveStreamLayout(
                            thumbnailsArrangement = thumbnailsArrangement,
                            thumbnailSize = thumbnailSize,
                            thumbnailsCount = maxThumbnailStreams
                        ) {
                            uiState.streamItems.value.fastForEach { streamItem ->
                                key(streamItem.id) {
                                    val isDimmed = remember(streamItem, selectedStreamId) {
                                        selectedStreamId != null && streamItem.id != selectedStreamId
                                    }

                                    val onClick = remember(
                                        streamItem,
                                        onStreamClick
                                    ) {
                                        onClick@{
                                            when (streamItem) {
                                                is StreamItem.Stream -> onStreamClick(streamItem)
                                                is StreamItem.MoreStreams -> onMoreParticipantClick()
                                            }
                                        }
                                    }

                                    val isFeatured = remember(streamItem) {
                                        val cast = streamItem as? StreamItem.Stream
                                        cast?.state is StreamItemState.Featured
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(StreamComponentDefaults.CornerRadius),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                                        modifier = itemModifier
                                            .pin(isFeatured)
                                            .streamDim(isDimmed)
                                            .streamClickable(
                                                onClick = onClick,
                                                label = if (streamItem is StreamItem.MoreStreams) stringResource(
                                                    id = R.string.kaleyra_stream_show_actions
                                                ) else stringResource(id = R.string.kaleyra_stream_show_participants)
                                            )
                                            .testTag(streamItem.id)
                                    ) {
                                        Box {
                                            when (streamItem) {
                                                is StreamItem.MoreStreams -> MoreStreamsItem(streamItem)

                                                is StreamItem.Stream -> {
                                                    val hasFeaturedStreamItems = remember(uiState) { uiState.streamItems.value.any { it.isFeatured() } }
                                                    val statusIconsAlignment = remember(streamItem, hasFeaturedStreamItems) {
                                                        if (streamItem.state !is StreamItemState.Featured && hasFeaturedStreamItems) Alignment.TopEnd else Alignment.BottomEnd
                                                    }

                                                    StreamItem(
                                                        stream = streamItem.stream,
                                                        fullscreen = streamItem.isFullscreen(),
                                                        pin = streamItem.isPinned(),
                                                        statusIconsAlignment = statusIconsAlignment,
                                                        onClick = onClick
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
}

private fun calculateThumbnailsSize(
    maxWidth: Dp,
    maxHeight: Dp,
    maxThumbnailsStreams: Int,
    maxThumbnailSize: Dp
): Dp {
    val maxAvailableSize = min(maxHeight, maxWidth) * 0.9f
    return if (maxAvailableSize < maxThumbnailSize) maxAvailableSize / maxThumbnailsStreams
    else min(maxAvailableSize / maxThumbnailsStreams, maxThumbnailSize)
}

private fun Modifier.streamClickable(
    onClick: () -> Unit,
    label: String,
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClickLabel = label,
        role = Role.Button,
        enabled = true,
        onClick = onClick
    )
}

private fun Modifier.streamDim(enabled: Boolean): Modifier = composed {
    if (enabled) {
        val description = stringResource(id = R.string.kaleyra_stream_dimmed)
        this
            .alpha(.3f)
            .semantics {
                contentDescription = description
            }

    } else this
}


@MultiConfigPreview
@Composable
internal fun StreamComponentPreview() {
    KaleyraTheme {
        Surface {
            StreamComponent(
                uiState = StreamUiState(streamItems = previewStreams),
                windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current),
                selectedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
        }
    }
}

@MultiConfigPreview
@Composable
internal fun StreamComponentFeaturedPreview() {
    KaleyraTheme {
        Surface {
            StreamComponent(
                uiState = StreamUiState(
                    streamItems = (previewStreams.value + listOf(
                        StreamItem.Stream("id8", streamUiMock, state = StreamItemState.Featured),
                        StreamItem.Stream("id9", streamUiMock, state = StreamItemState.Featured)
                    )).toImmutableList()
                ),
                windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current),
                selectedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
        }
    }
}

@MultiConfigPreview
@Composable
internal fun StreamComponentActiveScreenSharePreview() {
    KaleyraTheme {
        Surface {
            StreamComponent(
                uiState = StreamUiState(
                    streamItems = previewStreams,
                    isScreenShareActive = true
                ),
                windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current),
                selectedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun StreamComponentPreCallPreview() {
    KaleyraTheme {
        Surface {
            StreamComponent(
                uiState = StreamUiState(preview = com.kaleyra.video_sdk.call.stream.model.StreamPreview()),
                windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current),
                selectedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
        }
    }
}

private val previewStreams = ImmutableList(
    listOf(
        StreamItem.Stream("id1", streamUiMock),
        StreamItem.Stream("id2", streamUiMock),
        StreamItem.Stream("id3", streamUiMock),
        StreamItem.Stream("id4", streamUiMock),
        StreamItem.Stream("id5", streamUiMock),
        StreamItem.Stream("id6", streamUiMock),
        StreamItem.Stream("id7", streamUiMock)
    )
)