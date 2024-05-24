package com.kaleyra.video_sdk.call.screennew

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.call.screen.view.StreamGrid
import com.kaleyra.video_sdk.call.screen.view.StreamGridDefaults
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.view.StreamItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

// TODO remember saveable
@Composable
internal fun rememberStreamContentState(
    windowSizeClass: WindowSizeClass,
    streams: ImmutableList<StreamUi>,
    fullscreenStream: StreamUi? = null,
    pinnedStreams: ImmutableList<StreamUi> = ImmutableList()
) = remember(windowSizeClass, streams, fullscreenStream, pinnedStreams) {
    StreamContentState(windowSizeClass, streams, fullscreenStream, pinnedStreams)
}

@Stable
internal class StreamContentState(
    val windowSizeClass: WindowSizeClass,
    val streams: ImmutableList<StreamUi>,
    fullscreenStream: StreamUi?,
    pinnedStreams: ImmutableList<StreamUi>
) {

    var fullscreenStream by mutableStateOf(fullscreenStream)
        private set

    var pinnedStreams by mutableStateOf(pinnedStreams)
        private set

    fun enterFullscreen(stream: StreamUi) {
        fullscreenStream = stream
    }

    fun exitFullscreen() {
        fullscreenStream = null
    }

    fun pinStream(stream: StreamUi): Boolean {
        val maxPinned = if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) CompactScreenMaxPin else LargeScreenMaxPin
        return if (pinnedStreams.count() < maxPinned) {
            pinnedStreams = ImmutableList(pinnedStreams.value + stream)
            true
        } else false
    }

    fun unpinStream(stream: StreamUi) {
        pinnedStreams = ImmutableList(pinnedStreams.value - stream)
    }

}

// test enter/exit fullscreen
// test pin/unpin
// test stream on click
// test max stream grid
// test pinned streams before the others in the list
// test lastStreamId

internal const val CompactScreenMaxPin = 2
internal const val LargeScreenMaxPin = 6

internal const val CompactScreenMaxStreams = 8
internal const val LargeScreenMaxStreams = 15

internal val LargeScreenThumbnailSize = 180.dp

@Composable
internal fun StreamContent(
    windowSizeClass: WindowSizeClass,
    streamContentState: StreamContentState,
    onStreamClick: (StreamUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxStreams = if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) CompactScreenMaxStreams else LargeScreenMaxStreams
    val unpinnedStreams by remember(streamContentState) {
        derivedStateOf {
            streamContentState.streams.value - streamContentState.pinnedStreams.value.toSet()
        }
    }
    val streams: List<StreamUi> by remember(streamContentState) {
        derivedStateOf {
            val fullscreenStream = streamContentState.fullscreenStream
            if (fullscreenStream != null) listOf(fullscreenStream)
            else (streamContentState.pinnedStreams.value + unpinnedStreams).take(maxStreams)
        }
    }
    val otherStreamCount: Int by remember(streamContentState) {
        derivedStateOf {
            val pinnedStreams = streamContentState.pinnedStreams.value
            if (pinnedStreams.isEmpty()) streamContentState.streams.count() - maxStreams
            else streamContentState.streams.count() - (pinnedStreams.size + StreamGridDefaults.thumbnailCount)
        }
    }
    val lastStreamId: String? by remember(streamContentState) {
        derivedStateOf {
            when {
                streams.isEmpty() || streamContentState.streams.value.count() <= maxStreams -> null
                streamContentState.pinnedStreams.value.isEmpty() -> streams.last().id
                else -> {
                    val nonPinnedStreams = streamContentState.streams.value - streamContentState.pinnedStreams.value.toSet()
                    nonPinnedStreams[StreamGridDefaults.thumbnailCount -1].id
                }
            }
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val constraints = constraints
        StreamGrid(
            thumbnailsArrangement = thumbnailsArrangementFor(windowSizeClass),
            thumbnailSize = thumbnailsSizeFor(windowSizeClass)
        ) {
            streams.fastForEachIndexed { index, stream ->
                key(stream.id) {
                    val isPinned by remember(streamContentState) {
                        derivedStateOf { streamContentState.pinnedStreams.value.contains(stream) }
                    }
                    val isFullscreen by remember(streamContentState) {
                        derivedStateOf { streamContentState.fullscreenStream == stream }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .animateConstraints()
                            .animatePlacement(
                                IntOffset(
                                    constraints.maxWidth,
                                    constraints.maxHeight
                                )
                            )
                            .pin(isPinned)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClickLabel = "label",
                                role = Role.Button,
                                onClick = { onStreamClick(stream) }
                            )
                    ) {
                        StreamItem(
                            stream = stream,
                            fullscreen = isFullscreen,
                            pin = isPinned
                        )
                        if (!isFullscreen && stream.id == lastStreamId) {
                            Surface(
                                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = .1f),
                                contentColor = MaterialTheme.colorScheme.inverseSurface,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "$otherStreamCount others",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium.copy(lineHeight = 24.sp),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement = when {
    windowSizeClass.isCompactHeight() -> ThumbnailsArrangement.End
    windowSizeClass.isExpandedWidth() -> ThumbnailsArrangement.Start
    else -> ThumbnailsArrangement.Bottom
}

private fun thumbnailsSizeFor(windowSizeClass: WindowSizeClass): Dp =
    if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) StreamGridDefaults.thumbnailSize
    else LargeScreenThumbnailSize

internal fun WindowSizeClass.isCompactHeight(): Boolean {
    return heightSizeClass == WindowHeightSizeClass.Compact
}

internal fun WindowSizeClass.isCompactWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

internal fun WindowSizeClass.isExpandedWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded
}