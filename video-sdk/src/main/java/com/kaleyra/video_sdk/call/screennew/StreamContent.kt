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
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.call.screen.view.StreamGrid
import com.kaleyra.video_sdk.call.screen.view.StreamGridScope.pin
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.view.ScreenShareItem
import com.kaleyra.video_sdk.call.stream.view.StreamItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

const val CompactScreenMaxPin = 2

const val LargeScreenMaxPin = 6

const val CompactScreenMaxFeatured = 8

const val LargeScreenMaxFeatured = 15

const val MaxThumbnailCount = 3

val MaxThumbnailSize = 180.dp

internal fun WindowSizeClass.isCompactHeight(): Boolean {
    return heightSizeClass == WindowHeightSizeClass.Compact
}

internal fun WindowSizeClass.isCompactWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

internal fun WindowSizeClass.isExpandedWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded
}

// TODO 
//  test enter/exit fullscreen 
//  test pin/unpin 
//  test stream on click 
//  test max stream grid 
//  test lastStreamId

@Composable
internal fun StreamContent(
    windowSizeClass: WindowSizeClass,
    streams: ImmutableList<StreamUi>,
    onStreamClick: (StreamUi) -> Unit,
    onStopScreenShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullscreenStream: StreamUi? = null,
    pinnedStreams: ImmutableList<StreamUi> = ImmutableList(),
    highlightedStream: StreamUi? = null,
    maxPinnedCount: Int = Int.MAX_VALUE,
    maxFeaturedCount: Int = Int.MAX_VALUE,
    maxThumbnailCount: Int = MaxThumbnailCount,
) {
    val (localScreenShareStream: StreamUi?, streamFeed: List<StreamUi>) = calculateStreamsToShow(
        streams,
        fullscreenStream,
        pinnedStreams,
        maxPinnedCount,
        maxFeaturedCount,
        maxThumbnailCount
    )

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val itemModifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .animateConstraints()
            .animatePlacement(IntOffset(constraints.maxWidth, constraints.maxHeight))

        StreamGrid(
            thumbnailsArrangement = thumbnailsArrangementFor(windowSizeClass),
            thumbnailSize = calculateThumbnailsSize(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                maxThumbnailCount = maxThumbnailCount
            ),
            thumbnailsCount = maxThumbnailCount
        ) {
            if (localScreenShareStream != null && fullscreenStream == null) {
                key(localScreenShareStream.id) {
                    ScreenShareItem(
                        onStopClick = onStopScreenShareClick,
                        modifier = itemModifier.pin(true)
                    )
                }
            }

            streamFeed.fastForEachIndexed { index, stream ->
                key(stream.id) {
                    val (isFullscreen, isPinned) = remember(fullscreenStream, pinnedStreams) {
                        val isFullscreen = fullscreenStream?.id == stream.id
                        val isPinned = pinnedStreams.value.fastAny { it.id == stream.id }
                        isFullscreen to isPinned
                    }

                    StreamItemWithBorder(
                        stream = stream,
                        isFullscreen = isFullscreen,
                        isPinned = isPinned,
                        isHighlighted = highlightedStream?.id == stream.id,
                        onStreamClick = onStreamClick
                    )

                    if (!isFullscreen && index == streamFeed.size - 1) {
                        val moreStreamsCount = remember(streams) { streams.value.size - streamFeed.size }
                        if (moreStreamsCount != 0) {
                            MoreStreamsIndicator(moreStreamsCount)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamItemWithBorder(
    stream: StreamUi,
    isFullscreen: Boolean,
    isPinned: Boolean,
    isHighlighted: Boolean,
    onStreamClick: (StreamUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .pin(isPinned)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "show stream sub menu",
                role = Role.Button,
                onClick = { onStreamClick(stream) }
            )
            .border(
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Transparent,
                width = 2.dp,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        StreamItem(
            stream = stream,
            fullscreen = isFullscreen,
            pin = isPinned
        )
    }
}

@Composable
private fun MoreStreamsIndicator(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .1f),
        contentColor = MaterialTheme.colorScheme.inverseSurface,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = "$count others",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(lineHeight = 24.sp),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun calculateStreamsToShow(
    streams: ImmutableList<StreamUi>,
    fullscreenStream: StreamUi?,
    pinnedStreams: ImmutableList<StreamUi>,
    maxPinnedCount: Int,
    maxFeaturedCount: Int,
    maxThumbnailCount: Int
): Pair<StreamUi?, List<StreamUi>> {
    return remember(
        streams,
        fullscreenStream,
        pinnedStreams,
        maxPinnedCount,
        maxFeaturedCount,
        maxThumbnailCount
    ) {
        // Find the local screen share stream (if any)
        val localScreenShareStream = streams.value.find { it.video?.isScreenShare == true && it.mine }

        // Create a list of streams excluding the local screen share
        val nonLocalScreenShareStreams = streams.value.subtract(setOfNotNull(localScreenShareStream))

        // Calculate the lists of featured and thumbnail streams
        val featuredStreams = nonLocalScreenShareStreams.take(maxFeaturedCount)
        val thumbnailStreams = nonLocalScreenShareStreams
            .filterNot { pinnedStreams.value.contains(it) }
            .take(maxThumbnailCount)

        // Build the list of revised pinned streams, prioritizing the local screen share stream
        val revisedPinnedStreams = buildList {
            localScreenShareStream?.let { add(it) } // Add screen share if available
            // Add other pinned streams, adjusting the count if screen share is present
            addAll(pinnedStreams.value.take(maxPinnedCount).drop(if (localScreenShareStream != null) 1 else 0))
        }

        // Determine the final list of streams to display
        val streamFeed = when {
            fullscreenStream != null -> listOfNotNull(fullscreenStream)
            revisedPinnedStreams.isNotEmpty() -> revisedPinnedStreams + thumbnailStreams
            else -> featuredStreams
        }

        localScreenShareStream to streamFeed
    }
}

@Composable
private fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement {
    return remember(windowSizeClass) {
        when {
            windowSizeClass.isCompactHeight() -> ThumbnailsArrangement.End
            windowSizeClass.isExpandedWidth() -> ThumbnailsArrangement.Start
            else -> ThumbnailsArrangement.Bottom
        }
    }
}

private fun calculateThumbnailsSize(maxWidth: Dp, maxHeight: Dp, maxThumbnailCount: Int): Dp {
    val availableSpace = min(maxHeight, maxWidth) * .9f
    return min(availableSpace / maxThumbnailCount, MaxThumbnailSize)
}
