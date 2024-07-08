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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo

internal const val MaxFeaturedStreamsCompact = 8
internal const val MaxFeaturedStreamsExpanded = 15

internal const val MaxPinnedStreamsCompact = 2
internal const val MaxPinnedStreamsExpanded = 6

internal object StreamComponentDefaults {

    const val MaxThumbnailStreams = 3

    val MaxThumbnailSize = 180.dp

    fun maxFeaturedStreams(windowSizeClass: WindowSizeClass): Int {
        return if (windowSizeClass.isCompactInAnyDimension()) {
            MaxFeaturedStreamsCompact
        } else MaxFeaturedStreamsExpanded
    }

    fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement {
        return when {
            windowSizeClass.hasCompactHeight() -> ThumbnailsArrangement.End
            windowSizeClass.hasExpandedWidth() -> ThumbnailsArrangement.Start
            else -> ThumbnailsArrangement.Bottom
        }
    }
}

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
    onMoreParticipantClick: () -> Unit,
    highlightedStreamId: String? = null,
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
        highlightedStreamId = highlightedStreamId,
        onStreamClick = onStreamClick,
        onStopScreenShareClick = onStopScreenShareClick,
        onMoreParticipantClick = onMoreParticipantClick,
        modifier = modifier
    )
}

@Composable
internal fun StreamComponent(
    streamUiState: StreamUiState,
    windowSizeClass: WindowSizeClass,
    highlightedStreamId: String?,
    onStreamClick: (StreamUi) -> Unit,
    onStopScreenShareClick: () -> Unit,
    onMoreParticipantClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxFeaturedStreams: Int = remember(windowSizeClass) { StreamComponentDefaults.maxFeaturedStreams(windowSizeClass) },
    maxThumbnailStreams: Int = StreamComponentDefaults.MaxThumbnailStreams,
    thumbnailsArrangement: ThumbnailsArrangement = remember(windowSizeClass) { StreamComponentDefaults.thumbnailsArrangementFor(windowSizeClass) },
    maxThumbnailSize: Dp = StreamComponentDefaults.MaxThumbnailSize,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val streamsToDisplay = remember(streamUiState, maxFeaturedStreams, maxThumbnailStreams) {
            streamsToDisplayFor(streamUiState, maxFeaturedStreams, maxThumbnailStreams)
        }

        val nonDisplayedParticipantsData = remember(streamUiState, streamsToDisplay) {
            val nonDisplayedStreams = streamUiState.streams.value - streamsToDisplay.toSet()
            nonDisplayedStreams.map { NonDisplayedParticipantData(it.id, it.username, it.avatar) }.toImmutableList()
        }

        // Calculate thumbnail size based on available space
        val thumbnailSize = remember(maxWidth, maxHeight, maxThumbnailStreams, maxThumbnailSize) {
            calculateThumbnailsSize(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                maxThumbnailsStreams = maxThumbnailStreams,
                maxThumbnailSize = maxThumbnailSize
            )
        }

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
            thumbnailsArrangement = thumbnailsArrangement,
            thumbnailSize = thumbnailSize,
            thumbnailsCount = maxThumbnailStreams
        ) {
            streamsToDisplay.fastForEachIndexed { index, stream ->
                key(stream.id) {
                    val streamItemState: StreamItemState = remember(stream, highlightedStreamId, streamUiState) {
                        streamItemStateFor(stream, highlightedStreamId, streamUiState)
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
                                enabled = !streamItemState.isLocalScreenShare,
                                onClick = {
                                    if (displayAsMoreParticipantsItem) onMoreParticipantClick()
                                    else onStreamClick(stream)
                                }
                            )
                            .testTag(stream.id)
                    ) {
                        Box {
                            when {
                                displayAsMoreParticipantsItem -> {
                                    // Add this participant to the list of non displayed participants
                                    val participants = listOf(NonDisplayedParticipantData(stream.id, stream.username, stream.avatar)) + nonDisplayedParticipantsData.value
                                    MoreParticipantsItem(participants.toImmutableList())
                                }
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
    maxFeaturedStreams: Int,
    maxThumbnailsStreams: Int
): List<StreamUi> {
    val streams = streamUiState.streams.value
    val pinnedStreams = streamUiState.pinnedStreams.value
    val fullscreenStream = streamUiState.fullscreenStream

    return when {
        fullscreenStream != null -> listOf(fullscreenStream)
        pinnedStreams.isNotEmpty() -> {
            val thumbnailStreams = streams
                .filterNot { pinnedStreams.contains(it) }
                .take(maxThumbnailsStreams)
            pinnedStreams + thumbnailStreams
        }

        else -> {
            streams.take(maxFeaturedStreams)
        }
    }
}

private fun streamItemStateFor(
    stream: StreamUi,
    highlightedStreamId: String?,
    state: StreamUiState,
): StreamItemState {
    return StreamItemState(
        isHighlighted = stream.id == highlightedStreamId,
        isLocalScreenShare = stream.video?.isScreenShare == true && stream.isMine,
        isFullscreen = state.fullscreenStream?.id == stream.id,
        isPinned = state.pinnedStreams.value.fastAny { it.id == stream.id }
    )
}

private fun calculateThumbnailsSize(
    maxWidth: Dp,
    maxHeight: Dp,
    maxThumbnailsStreams: Int,
    maxThumbnailSize: Dp
): Dp {
    val maxAvailableSize = min(maxHeight, maxWidth) * 0.9f
    return if (maxAvailableSize < maxThumbnailSize) maxAvailableSize
    else min(maxAvailableSize / maxThumbnailsStreams, maxThumbnailSize)
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
        val description = stringResource(id = R.string.kaleyra_stream_selected)
        this
            .border(
                color = MaterialTheme.colorScheme.primary,
                width = 2.dp,
                shape = RoundedCornerShape(4.dp)
            )
            .semantics {
                contentDescription = description
            }

    } else this
}



@MultiConfigPreview
@Composable
internal fun StreamComponentPreview() {
    KaleyraM3Theme {
        Surface {
            StreamComponent(
                streamUiState = StreamUiState(streams = previewStreams),
                windowSizeClass = currentWindowAdaptiveInfo(),
                highlightedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
        }
    }
}

@MultiConfigPreview
@Composable
internal fun StreamComponentPinPreview() {
    KaleyraM3Theme {
        Surface {
            StreamComponent(
                streamUiState = StreamUiState(
                    streams = previewStreams,
                    pinnedStreams = ImmutableList(listOf(streamUiMock.copy(id = "id1"), streamUiMock.copy(id = "id2")))
                ),
                windowSizeClass = currentWindowAdaptiveInfo(),
                highlightedStreamId = null,
                onStreamClick = {},
                onStopScreenShareClick = {},
                onMoreParticipantClick = {}
            )
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