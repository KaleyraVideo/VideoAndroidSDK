package com.kaleyra.video_sdk.call.streamnew

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.hasCompactHeight
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.hasExpandedWidth
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.isCompactInAnyDimension
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.streamUiMock
import com.kaleyra.video_sdk.call.streamnew.view.items.MoreParticipantsItem
import com.kaleyra.video_sdk.call.streamnew.view.items.NonDisplayedParticipantData
import com.kaleyra.video_sdk.call.streamnew.view.items.ScreenShareItem
import com.kaleyra.video_sdk.call.streamnew.view.items.StreamItem
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

internal const val MaxVisibleStreamsCompact = 8
internal const val MaxVisibleStreamsExpanded = 15

internal const val MaxPinnedStreamsCompact = 2
internal const val MaxPinnedStreamsExpanded = 6

internal const val MaxThumbnailCount = 3
internal val MaxThumbnailSize = 180.dp

private data class StreamItemState(
    val isHighlighted: Boolean,
    val isLocalScreenShare: Boolean,
    val isFullscreen: Boolean,
    val isPinned: Boolean,
)

@Composable
internal fun StreamComponent(
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    ),
    windowSizeClass: WindowSizeClass,
    onStreamClick: (StreamUi) -> Unit,
    onStopScreenShareClick: () -> Unit,
    highlightedStream: StreamUi? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(windowSizeClass) {
        viewModel.setMaxPinnedStreams(
            if (windowSizeClass.isCompactInAnyDimension()) MaxPinnedStreamsCompact
            else MaxPinnedStreamsExpanded
        )
    }

    StreamComponent(
        streamUiState = uiState,
        windowSizeClass = windowSizeClass,
        highlightedStream = highlightedStream,
        onStreamClick = onStreamClick,
        onStopScreenShareClick = onStopScreenShareClick,
        modifier = modifier
    )
}

// TODO
//  test displayedItems
//  the non displayed participant data
//  test thumbnails arrangement
//  test displayed items
//  test fullscreen, pinned, highlight and local screen share
//  test when fullscreen and when there are no participant data
//  test onclick
//  test on local screen share and more participant items is disabled
//  test the right item is displayed
//  test onStopScreenShare click
//  test status icon alignment on thumbnail and featured
//  test fullscreen, pin and audio muted
@Composable
internal fun StreamComponent(
    streamUiState: StreamUiState,
    windowSizeClass: WindowSizeClass,
    highlightedStream: StreamUi?,
    onStreamClick: (StreamUi) -> Unit,
    onStopScreenShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val streamsToDisplay = remember(streamUiState, windowSizeClass) {
            streamsToDisplayFor(streamUiState, windowSizeClass)
        }

        val nonDisplayedParticipantsData = remember(streamUiState, streamsToDisplay) {
            val nonDisplayedStreams = streamUiState.streams.value - streamsToDisplay.toSet()
            nonDisplayedStreams.map { NonDisplayedParticipantData(it.id, it.username, it.avatar) }.toImmutableList()
        }

        // Calculate thumbnail size based on available space
        val thumbnailSize = calculateThumbnailsSize(
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
        val isNonDisplayedParticipantsDataEmpty = nonDisplayedParticipantsData.isEmpty()

        val itemPadding = 4.dp
        val itemModifier = Modifier
            .fillMaxSize()
            .padding(itemPadding)
            .let { modifier ->
                // Disable animation for preview
                if (!LocalInspectionMode.current) {
                    modifier
                        .animateConstraints()
                        .animatePlacement(IntOffset(constraints.maxWidth, constraints.maxHeight))
                } else modifier
            }


        AdaptiveStreamLayout(
            thumbnailsArrangement = rememberThumbnailsArrangementFor(windowSizeClass),
            thumbnailSize = thumbnailSize,
            thumbnailsCount = MaxThumbnailCount
        ) {
            streamsToDisplay.fastForEachIndexed { index, stream ->
                key(stream.id) {
                    val streamItemState: StreamItemState = remember(stream, highlightedStream, streamUiState) {
                        streamItemStateFor(stream, highlightedStream, streamUiState)
                    }
                    val displayAsMoreParticipantsItem =
                        !isNonDisplayedParticipantsDataEmpty && !streamItemState.isFullscreen && index == streamsToDisplay.size - 1

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        tonalElevation = 1.dp,
                        modifier = itemModifier
                            .pin(streamItemState.isPinned)
                            .streamHighlight(streamItemState.isHighlighted)
                            .streamClickable(
                                enabled = !displayAsMoreParticipantsItem && !streamItemState.isLocalScreenShare,
                                onClick = { onStreamClick(stream) }
                            )
                    ) {
                        Box {
                            when {
                                displayAsMoreParticipantsItem -> MoreParticipantsItem(nonDisplayedParticipantsData)
                                streamItemState.isLocalScreenShare -> ScreenShareItem(onStopScreenShareClick)

                                else -> {
                                    val statusIconsAlignment =
                                        if (streamUiState.fullscreenStream == stream || streamUiState.pinnedStreams.isEmpty() || streamItemState.isPinned) {
                                            Alignment.BottomEnd
                                        } else Alignment.TopEnd

                                    StreamItem(
                                        stream = stream,
                                        fullscreen = streamItemState.isFullscreen,
                                        pin = streamItemState.isPinned,
                                        statusIconsAlignment = statusIconsAlignment
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

private fun streamsToDisplayFor(
    streamUiState: StreamUiState,
    windowSizeClass: WindowSizeClass,
): List<StreamUi> {
    val streams = streamUiState.streams.value
    val pinnedStreams = streamUiState.pinnedStreams.value
    val fullscreenStream = streamUiState.fullscreenStream

    return when {
        fullscreenStream != null -> listOf(fullscreenStream)
        pinnedStreams.isNotEmpty() -> {
            val thumbnailStreams = streams
                .filterNot { pinnedStreams.contains(it) }
                .take(MaxThumbnailCount)
            pinnedStreams + thumbnailStreams
        }

        else -> {
            val maxStreams = if (windowSizeClass.isCompactInAnyDimension()) {
                MaxVisibleStreamsCompact
            } else MaxVisibleStreamsExpanded
            streams.take(maxStreams)
        }
    }
}

@Composable
private fun rememberThumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement {
    return remember(windowSizeClass) {
        when {
            windowSizeClass.hasCompactHeight() -> ThumbnailsArrangement.End
            windowSizeClass.hasExpandedWidth() -> ThumbnailsArrangement.Start
            else -> ThumbnailsArrangement.Bottom
        }
    }
}

private fun streamItemStateFor(
    stream: StreamUi,
    highlightedStream: StreamUi?,
    state: StreamUiState,
): StreamItemState {
    return StreamItemState(
        isHighlighted = stream.id == highlightedStream?.id,
        isLocalScreenShare = stream.video?.isScreenShare == true && stream.mine,
        isFullscreen = state.fullscreenStream?.id == stream.id,
        isPinned = state.pinnedStreams.value.fastAny { it.id == stream.id }
    )
}

private fun calculateThumbnailsSize(maxWidth: Dp, maxHeight: Dp): Dp {
    val maxAvailableSize = min(maxHeight, maxWidth) * 0.9f
    return if (maxAvailableSize < MaxThumbnailSize) maxAvailableSize
    else min(maxAvailableSize / MaxThumbnailCount, MaxThumbnailSize)
}

private fun Modifier.streamClickable(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClickLabel = stringResource(id = R.string.kaleyra_stream_show_actions),
        role = Role.Button,
        enabled = enabled,
        onClick = onClick
    )
}

private fun Modifier.streamHighlight(enabled: Boolean): Modifier = composed {
    if (enabled) {
        this.border(
            color = MaterialTheme.colorScheme.primary,
            width = 2.dp,
            shape = RoundedCornerShape(4.dp)
        )
    } else this
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MultiConfigPreview
@Composable
internal fun StreamComponentPreview() {
    KaleyraM3Theme {
        Surface {
            BoxWithConstraints {
                StreamComponent(
                    streamUiState = StreamUiState(streams = previewStreams),
                    windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight)),
                    highlightedStream = null,
                    onStreamClick = {},
                    onStopScreenShareClick = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@MultiConfigPreview
@Composable
internal fun StreamComponentPinPreview() {
    KaleyraM3Theme {
        Surface {
            BoxWithConstraints {
                StreamComponent(
                    streamUiState = StreamUiState(
                        streams = previewStreams,
                        pinnedStreams = ImmutableList(listOf(streamUiMock.copy(id = "id1"), streamUiMock.copy(id = "id2")))
                    ),
                    windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight)),
                    highlightedStream = null,
                    onStreamClick = {},
                    onStopScreenShareClick = {},
                )
            }
        }
    }
}

private val previewStreams = ImmutableList(
    listOf(
        streamUiMock.copy(id = "id1"),
        streamUiMock.copy(id = "id2"),
        streamUiMock.copy(id = "id3"),
        streamUiMock.copy(id = "id4"),
        streamUiMock.copy(id = "id5"),
        streamUiMock.copy(id = "id6"),
        streamUiMock.copy(id = "id7")
    )
)