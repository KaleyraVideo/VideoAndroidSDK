package com.kaleyra.video_sdk.call.screennew

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.kaleyra.video_sdk.call.screen.view.StreamGrid
import com.kaleyra.video_sdk.call.screen.view.StreamGridDefaults
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import kotlin.random.Random

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
    val streams by remember(streamContentState) {
        derivedStateOf {
            val new = streamContentState.fullscreenStream?.let { listOf(it) }
                ?: (streamContentState.pinnedStreams.value + streamContentState.streams.value.minus(
                    streamContentState.pinnedStreams.value.toSet()
                ))
            new.take(maxStreams)
        }
    }
    val lastStreamId by remember(streamContentState) {
        derivedStateOf {
            when {
                streams.isEmpty() || streamContentState.streams.value.count() <= maxStreams -> null
                streamContentState.pinnedStreams.value.isEmpty() -> streams.last().id
                else -> {
                    val nonPinnedStreams = streamContentState.streams.value - streamContentState.pinnedStreams.value.toSet()
                    nonPinnedStreams[2].id
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
                    val isPinned by remember {
                        derivedStateOf {
                            streamContentState.pinnedStreams.value.contains(stream)
                        }
                    }
                    val color = rememberSaveable { Color.random().toArgb() }

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
                            .border(BorderStroke(3.dp, Color.Red))
                            .pin(isPinned)
                            .clickable { onStreamClick(stream) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color(color))
                        ) {
                            if (stream.id == lastStreamId) {
                                Text(text = "last stream")
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

fun Color.Companion.random(): Color {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color(red, green, blue)
}