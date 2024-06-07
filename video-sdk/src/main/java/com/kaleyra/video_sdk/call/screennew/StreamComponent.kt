package com.kaleyra.video_sdk.call.screennew

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.hasCompactHeight
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.hasExpandedWidth
import com.kaleyra.video_sdk.call.screennew.WindowSizeClassExts.isCompactInAnyDimension
import com.kaleyra.video_sdk.call.streamnew.model.StreamUi
import com.kaleyra.video_sdk.call.streamnew.view.items.MoreParticipantsItem
import com.kaleyra.video_sdk.call.streamnew.view.items.NonDisplayedParticipantData
import com.kaleyra.video_sdk.call.streamnew.view.items.ScreenShareItem
import com.kaleyra.video_sdk.call.streamnew.view.items.StreamItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal const val MaxVisibleStreamsCompact = 8
internal const val MaxVisibleStreamsExpanded = 15

internal const val MaxThumbnailCount = 3
internal val MaxThumbnailSize = 180.dp

@Composable
internal fun rememberStreamContentController(
    streams: State<ImmutableList<StreamUi>>,
    windowSizeClass: State<WindowSizeClass>,
    scope: CoroutineScope = rememberCoroutineScope(),
) = remember(streams, windowSizeClass) {
    StreamContentController(
        streams,
        windowSizeClass,
        scope
    )
}

@Stable
internal class StreamContentController(
    streamsState: State<ImmutableList<StreamUi>>,
    windowSizeClassState: State<WindowSizeClass>,
    scope: CoroutineScope,
) {

    val streams by streamsState

    val windowSizeClass by windowSizeClassState

    var fullscreenStream: StreamUi? by mutableStateOf(null)
        private set

    private val mutablePinnedStreams = mutableStateListOf<StreamUi>()
    val pinnedStreams: List<StreamUi> = mutablePinnedStreams

    private val maxPinnedStreams = derivedStateOf {
        if (windowSizeClass.isCompactInAnyDimension()) MAX_PINNED_STREAMS_COMPACT
        else MAX_PINNED_STREAMS_EXPANDED
    }

    init {
        snapshotFlow { streamsState.value }
            .onEach { streams ->
                val newStreams = streams.value
                // Update pinnedStreams and fullscreenStream based on new streams
                mutablePinnedStreams.retainAll(newStreams)
                fullscreenStream = newStreams.find { it.id == fullscreenStream?.id }

                // Find local screen share and pin it if available
                val localScreenShare =
                    newStreams.find { it.video?.isScreenShare == true && it.mine }
                localScreenShare?.let {
                    // Pin to the beginning of the list
                    mutablePinnedStreams.add(0, it)
                    // Ensure the number of pinned streams does not exceed the maximum
                    if (mutablePinnedStreams.size > maxPinnedStreams.value) {
                        mutablePinnedStreams.removeLast()
                    }
                }
            }
            .launchIn(scope)
    }

    fun fullscreen(stream: StreamUi?) {
        fullscreenStream = stream
    }

    fun pinStream(stream: StreamUi): Boolean {
        if (pinnedStreams.size >= maxPinnedStreams.value) return false
        mutablePinnedStreams.add(stream)
        return true
    }

    fun unpinStream(stream: StreamUi) {
        mutablePinnedStreams.remove(stream)
    }

    companion object {
        private const val MAX_PINNED_STREAMS_COMPACT = 2
        private const val MAX_PINNED_STREAMS_EXPANDED = 6
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
    streamContentController: StreamContentController,
    onStreamClick: (StreamUi) -> Unit,
    onStopScreenShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlightedStream: StreamUi? = null,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // TODO test displayedItems
        val streamsToDisplay by streamsToDisplayFor(streamContentController)
        // TODO the non displayed participant data
        val nonDisplayedParticipantsData by remember(streamContentController) {
            derivedStateOf {
                val nonDisplayedStreams = streamContentController.streams.value - streamsToDisplay.toSet()
                nonDisplayedStreams.map { NonDisplayedParticipantData(it.id, it.username, it.avatar) }.toImmutableList()
            }
        }

        // Calculate thumbnail size based on available space
        val thumbnailSize = calculateThumbnailsSize(
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
        val isNonDisplayedParticipantsDataEmpty = nonDisplayedParticipantsData.value.isEmpty()

        val itemPadding = 4.dp
        val itemModifier = Modifier
            .fillMaxSize()
            .padding(itemPadding)
            .animateConstraints()
            .animatePlacement(IntOffset(constraints.maxWidth, constraints.maxHeight))

        // TODO test thumbnails arrangement
        AdaptiveStreamLayout(
            thumbnailsArrangement = thumbnailsArrangementFor(streamContentController.windowSizeClass),
            thumbnailSize = thumbnailSize,
            thumbnailsCount = MaxThumbnailCount
        ) {
            // TODO test displayed items
            streamsToDisplay.fastForEachIndexed { index, stream ->
                key(stream.id) {
                    // TODO test fullscreen, pinned, highlight and local screen share
                    val streamItemState: StreamItemState by streamItemStateFor(
                        stream, highlightedStream, streamContentController
                    )
                    // TODO test when fullscreen and when there are no participant data
                    val displayAsMoreParticipantsItem = !isNonDisplayedParticipantsDataEmpty && !streamItemState.isFullscreen && index == streamsToDisplay.size - 1

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        tonalElevation = 1.dp,
                        modifier = itemModifier
                            .pin(streamItemState.isPinned)
                            .streamHighlight(streamItemState.isHighlighted)
                            // TODO test onclick
                            .streamClickable(
                                // TODO test on local screen share and more participant items is disabled
                                enabled = !displayAsMoreParticipantsItem && !streamItemState.isLocalScreenShare,
                                onClick = { onStreamClick(stream) }
                            )
                    ) {
                        Box {
                            // TODO test the right item is displayed
                            when {
                                displayAsMoreParticipantsItem -> MoreParticipantsItem(nonDisplayedParticipantsData)
                                // TODO test onStopScreenShare click
                                streamItemState.isLocalScreenShare -> ScreenShareItem(onStopScreenShareClick)
                                else -> {
                                    // TODO test status icon alignment on thumbnail and featured
                                    val statusIconsAlignment = if (streamContentController.fullscreenStream == stream || streamContentController.pinnedStreams.isEmpty() || streamItemState.isPinned) {
                                        Alignment.BottomEnd
                                    } else Alignment.TopEnd

                                    // TODO test fullscreen, pin and audio muted
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

@Composable
private fun streamsToDisplayFor(streamContentController: StreamContentController) =
    remember(streamContentController) {
        derivedStateOf {
            val streams = streamContentController.streams
            val pinnedStreams = streamContentController.pinnedStreams
            val fullscreenStream = streamContentController.fullscreenStream

            when {
                fullscreenStream != null -> listOf(fullscreenStream)
                pinnedStreams.isNotEmpty() -> {
                    val thumbnailStreams = streams.value
                        .filterNot { pinnedStreams.contains(it) }
                        .take(MaxThumbnailCount)
                    pinnedStreams + thumbnailStreams
                }

                else -> {
                    val maxStreams =
                        if (streamContentController.windowSizeClass.isCompactInAnyDimension()) MaxVisibleStreamsCompact
                        else MaxVisibleStreamsExpanded
                    streams.value.take(maxStreams)
                }
            }
        }
    }


@Composable
private fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement {
    return remember(windowSizeClass) {
        when {
            windowSizeClass.hasCompactHeight() -> ThumbnailsArrangement.End
            windowSizeClass.hasExpandedWidth() -> ThumbnailsArrangement.Start
            else -> ThumbnailsArrangement.Bottom
        }
    }
}

@Composable
private fun streamItemStateFor(
    stream: StreamUi,
    highlightedStream: StreamUi?,
    streamContentController: StreamContentController,
): State<StreamItemState> {
    return remember(stream, highlightedStream) {
        derivedStateOf {
            StreamItemState(
                isHighlighted = stream.id == highlightedStream?.id,
                isLocalScreenShare = stream.video?.isScreenShare == true && stream.mine,
                isFullscreen = streamContentController.fullscreenStream?.id == stream.id,
                isPinned = streamContentController.pinnedStreams.fastAny { it.id == stream.id }
            )
        }
    }
}

private fun calculateThumbnailsSize(maxWidth: Dp, maxHeight: Dp): Dp {
    val maxAvailableSize = min(maxHeight, maxWidth) * 0.9f
    return if (maxAvailableSize < MaxThumbnailSize) maxAvailableSize
    else min(maxAvailableSize / MaxThumbnailCount, MaxThumbnailSize)
}

private fun Modifier.streamClickable(
    enabled: Boolean,
    onClick: () -> Unit
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
