package com.kaleyra.video_sdk.call.screennew

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import com.kaleyra.video_sdk.call.screen.view.StreamGrid
import com.kaleyra.video_sdk.call.screen.view.StreamGridDefaults
import com.kaleyra.video_sdk.call.screen.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.view.ScreenShareItem
import com.kaleyra.video_sdk.call.stream.view.StreamItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animateConstraints
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.max

@Stable
internal object StreamContentDefaults {

    const val CompactScreenMaxPin = 2

    const val LargeScreenMaxPin = 6

    const val CompactScreenMaxStreams = 8

    const val LargeScreenMaxStreams = 15

    val LargeScreenThumbnailSize = 180.dp
}

@Composable
internal fun rememberStreamContentState(
    streams: ImmutableList<StreamUi>,
    windowSizeClass: WindowSizeClass,
    scope: CoroutineScope = rememberCoroutineScope()
): StreamContentState {
    val streamsState = rememberUpdatedState(streams)
    val windowSizeClassState = rememberUpdatedState(windowSizeClass)
    return remember(scope) {
        StreamContentState(
            streams = streamsState,
            windowSizeClass = windowSizeClassState,
            scope = scope
        )
    }
}

@Stable
internal class StreamContentState(
    streams: State<ImmutableList<StreamUi>>,
    windowSizeClass: State<WindowSizeClass>,
    scope: CoroutineScope
) {

    val windowSizeClass by windowSizeClass

    val myScreenShareStream by derivedStateOf {
        streams.value.value.fastFirstOrNull { stream -> stream.video?.isScreenShare == true && stream.mine }
    }

    var pinnedStreams = mutableStateMapOf<String, StreamUi>()
        private set

    var fullscreenStream: StreamUi? by mutableStateOf(null)
        private set

    val streams by derivedStateOf {
        listOfNotNull(fullscreenStream).ifEmpty {
            streams.value.value - setOfNotNull(myScreenShareStream)
        }
    }

    init {
        snapshotFlow { streams.value.value.associateBy { it.id } }
            .onEach { streamsMap ->
                // TODO test this
                val fullscreenId = fullscreenStream?.id
                if (fullscreenId != null && !streamsMap.containsKey(fullscreenId)) {
                    fullscreenStream = null
                }
                // TODO test this
                pinnedStreams.entries.removeIf { !streamsMap.containsKey(it.key) }
            }
            .launchIn(scope)

        // TODO test this
        var currentScreenShareStreams = emptySet<StreamUi>()
        snapshotFlow { streams.value.value.filter { it.video?.isScreenShare == true && !it.mine }.toSet() }
            .onEach { screenShareStreams ->
                val newScreenShareStreams = screenShareStreams - currentScreenShareStreams
                newScreenShareStreams.forEach { pin(it) }
                currentScreenShareStreams = newScreenShareStreams }
            .launchIn(scope)
    }

    fun pin(stream: StreamUi) {
        pinnedStreams[stream.id] = stream
    }

    fun unpin(stream: StreamUi) {
        pinnedStreams.remove(stream.id)
    }

    fun setFullscreen(stream: StreamUi) {
        fullscreenStream = stream
    }

    fun cleanFullscreen() {
        fullscreenStream = null
    }
}

// TODO 
//  test enter/exit fullscreen 
//  test pin/unpin 
//  test stream on click 
//  test max stream grid 
//  test lastStreamId

@Composable
internal fun StreamContent(
    state: StreamContentState,
    onStreamClick: (StreamUi) -> Unit,
    modifier: Modifier = Modifier
) {
    val pair by remember(state) {
        derivedStateOf {
            val pinnedStreams = state.pinnedStreams.values
            when {
                pinnedStreams.isEmpty() -> {
                    val streamId = state.streams.getOrNull(maxFeaturedStreamsFor(state.windowSizeClass) - 1)?.id
                    val count = max(0, state.streams.size - maxFeaturedStreamsFor(state.windowSizeClass))
                    streamId to count
                }
                else -> {
                    val thumbnailStreams = state.streams - pinnedStreams.toSet()
                    val streamId = thumbnailStreams.getOrNull(StreamGridDefaults.ThumbnailCount - 1)?.id
                    val count = max(0, state.streams.size - (pinnedStreams.size + StreamGridDefaults.ThumbnailCount))
                    streamId to count
                }
            }
        }
    }
    val (stringId, count) = pair

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        val constraints = constraints
        StreamGrid(
            thumbnailsArrangement = thumbnailsArrangementFor(state.windowSizeClass),
            thumbnailSize = thumbnailsSizeFor(state.windowSizeClass),
            maxFeatured = maxFeaturedStreamsFor(state.windowSizeClass),
            maxPinned = maxPinnedStreamsFor(state.windowSizeClass)
        ) {
            val fullscreenStream = state.fullscreenStream
            val myScreenShareStream = state.myScreenShareStream
            val streamModifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .animateConstraints()
                .animatePlacement(IntOffset(constraints.maxWidth, constraints.maxHeight))
            if (fullscreenStream == null && myScreenShareStream != null) {
                key(myScreenShareStream.id) {
                    ScreenShareItem(
                        onStopClick = {

                        },
                        modifier = streamModifier.pin(true)
                    )
                }
            }
            state.streams.fastForEach { stream ->
                key(stream.id) {
                    val isFullscreen by remember {
                        derivedStateOf {
                            state.fullscreenStream == stream
                        }
                    }
                    val isPinned by remember {
                        derivedStateOf {
                            state.pinnedStreams.containsKey(stream.id)
                        }
                    }
                    Box(
                        modifier = streamModifier
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
                        if (!isFullscreen && stream.id == stringId) {
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
                    }
                }
            }
        }
    }
}

private fun thumbnailsArrangementFor(windowSizeClass: WindowSizeClass): ThumbnailsArrangement =
    when {
        windowSizeClass.isCompactHeight() -> ThumbnailsArrangement.End
        windowSizeClass.isExpandedWidth() -> ThumbnailsArrangement.Start
        else -> ThumbnailsArrangement.Bottom
    }

private fun thumbnailsSizeFor(windowSizeClass: WindowSizeClass): Dp =
    if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) StreamGridDefaults.ThumbnailSize
    else StreamContentDefaults.LargeScreenThumbnailSize

private fun maxFeaturedStreamsFor(windowSizeClass: WindowSizeClass): Int =
    if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) StreamContentDefaults.CompactScreenMaxStreams
    else StreamContentDefaults.LargeScreenMaxStreams

private fun maxPinnedStreamsFor(windowSizeClass: WindowSizeClass): Int =
    if (windowSizeClass.isCompactHeight() || windowSizeClass.isCompactWidth()) StreamContentDefaults.CompactScreenMaxPin
    else StreamContentDefaults.LargeScreenMaxPin

internal fun WindowSizeClass.isCompactHeight(): Boolean {
    return heightSizeClass == WindowHeightSizeClass.Compact
}

internal fun WindowSizeClass.isCompactWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

internal fun WindowSizeClass.isExpandedWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Expanded
}