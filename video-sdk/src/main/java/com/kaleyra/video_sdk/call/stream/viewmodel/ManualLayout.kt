package com.kaleyra.video_sdk.call.stream.viewmodel

import android.util.Log
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

internal interface ManualLayout: StreamLayout {

    val maxAllowedFeaturedStreams: StateFlow<Int>

    val maxAllowedPinnedStreams: StateFlow<Int>

    val maxAllowedThumbnailStreams: StateFlow<Int>

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpinStream(streamId: String)

    fun clearPinnedStreams()

    fun setFullscreenStream(streamId: String)

    fun clearFullscreenStream()
}

internal class ManualLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxAllowedFeaturedStreams: StateFlow<Int>,
    override val maxAllowedPinnedStreams: StateFlow<Int>,
    override val maxAllowedThumbnailStreams: StateFlow<Int>,
    coroutineScope: CoroutineScope,
): ManualLayout {

    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val pinnedStreamIds: List<String> = emptyList(),
        val fullscreenStreamId: String? = null,
        val maxAllowedFeaturedStreams: Int = 0,
        val maxAllowedPinnedStreams: Int = 0,
        val maxAllowedThumbnailStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            maxAllowedFeaturedStreams,
            maxAllowedPinnedStreams,
            maxAllowedThumbnailStreams
        ) { newStreams, maxAllowedFeaturedStreams, maxAllowedPinnedStreams, maxAllowedThumbnailStreams ->
            _internalState.update { state ->
                state.copy(
                    allStreams = newStreams,
                    pinnedStreamIds = retainPinnedStreamIds(state, newStreams, maxAllowedPinnedStreams),
                    fullscreenStreamId = newStreams.firstOrNull { it.id == state.fullscreenStreamId }?.id,
                    maxAllowedFeaturedStreams = max(0, maxAllowedFeaturedStreams),
                    maxAllowedPinnedStreams = max(0, maxAllowedPinnedStreams),
                    maxAllowedThumbnailStreams = max(0, maxAllowedThumbnailStreams),
                )
            }
        }.launchIn(coroutineScope)

        coroutineScope.launch {
            _internalState
                .collectLatest { state ->
                    val streamItemsProvider = when {
                        state.fullscreenStreamId != null -> FullscreenStreamItemProvider(state.allStreams, state.fullscreenStreamId)
                        state.pinnedStreamIds.isNotEmpty() -> FeaturedStreamItemsProvider(
                            streams = state.allStreams,
                            featuredStreamIds = state.pinnedStreamIds,
                            maxAllowedThumbnailStreams = state.maxAllowedThumbnailStreams,
                            featuredStreamItemState = StreamItemState.Featured.Pinned
                        )
                        else -> MosaicStreamItemsProvider(state.allStreams, state.maxAllowedFeaturedStreams)
                    }
                    _streamItems.update { streamItemsProvider.buildStreamItems() }
                }
        }
    }

    override fun pinStream(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        _internalState.update { state ->
            val maxAllowedPinnedStreams = state.maxAllowedPinnedStreams
            val pinnedStreams = state.pinnedStreamIds
            val stream = state.allStreams.find { it.id == streamId } ?: return false

            val canPin = canPinStream(stream, pinnedStreams, maxAllowedPinnedStreams, force)
            if (!canPin) return false

            state.copy(pinnedStreamIds = pinStream(stream.id, state.pinnedStreamIds, maxAllowedPinnedStreams, prepend))
        }
        return true
    }

    override fun unpinStream(streamId: String) {
        _internalState.update { state ->
            state.copy(pinnedStreamIds = state.pinnedStreamIds.filter { it != streamId })
        }
    }

    override fun clearPinnedStreams() {
        _internalState.update { state ->
            state.copy(pinnedStreamIds = emptyList())
        }
    }

    override fun setFullscreenStream(streamId: String) {
        _internalState.update { state ->
            state.copy(fullscreenStreamId = streamId)
        }
    }

    override fun clearFullscreenStream() {
        _internalState.update { state ->
            state.copy(fullscreenStreamId = null)
        }
    }

    private fun retainPinnedStreamIds(currentState: LayoutState, newStreams: List<StreamUi>, maxAllowedPinnedStreams: Int): List<String> {
        if (maxAllowedPinnedStreams < 1) return emptyList()
        return currentState.pinnedStreamIds
            .mapNotNull { streamId -> newStreams.find { it.id == streamId } }
            .take(maxAllowedPinnedStreams)
            .map { it.id }
    }

    private fun canPinStream(
        stream: StreamUi,
        pinnedStreamIds: List<String>,
        maxAllowedPinnedStreams: Int,
        force: Boolean,
    ): Boolean {
        if (maxAllowedPinnedStreams < 1) return false
        if (pinnedStreamIds.any { it == stream.id } || (pinnedStreamIds.size >= maxAllowedPinnedStreams && !force)) return false
        return true
    }

    private fun pinStream(
        streamId: String,
        currentPinnedStreamIds: List<String>,
        maxAllowedPinnedStreams: Int,
        prepend: Boolean = false,
    ): List<String> {
        return if (prepend) {
            (listOf(streamId) + currentPinnedStreamIds).take(maxAllowedPinnedStreams)
        } else {
            (currentPinnedStreamIds + streamId).takeLast(maxAllowedPinnedStreams)
        }
    }

}

internal interface StreamItemsProvider {

    val streams: List<StreamUi>

    fun buildStreamItems(): List<StreamItem>
}

//    private val sortStreamsByPriority: (List<StreamUi>) -> List<StreamUi> = { value ->
//        value
//            .sortedWith(
//                compareByDescending<StreamUi> { it.video != null && it.video.isScreenShare }
//                    .thenByDescending { it.video != null && it.video.isEnabled }
//            )
//    }
//            val sortedStreams = sortStreamsByPriority(streams)

internal class MosaicStreamItemsProvider(
    override val streams: List<StreamUi>,
    private val maxAllowedFeaturedStreams: Int,
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        if (streams.isEmpty()) return emptyList()

        return if (streams.size <= maxAllowedFeaturedStreams) {
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            val (localCameraStream, otherStreams) = streams.partition { it.isMine }
            val maxOtherFeaturedStreams = maxAllowedFeaturedStreams - localCameraStream.size
            val (featuredStreams, remainingStreams) = otherStreams.withIndex().partition { it.index < maxOtherFeaturedStreams - 1 }

            val streamItems = featuredStreams.map { indexedValue -> StreamItem.Stream(indexedValue.value.id, indexedValue.value) } + localCameraStream.map { StreamItem.Stream(it.id, it) }
            val moreItem = StreamItem.More(
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            streamItems + moreItem
        }
    }
}

internal class FeaturedStreamItemsProvider(
    override val streams: List<StreamUi>,
    private val featuredStreamIds: List<String>,
    private val maxAllowedThumbnailStreams: Int,
    private val featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        if (featuredStreamIds.isEmpty()) return emptyList()

        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        return if (nonFeaturedStreams.size <= maxAllowedThumbnailStreams) {
            (sortedFeaturedStreams + nonFeaturedStreams).map { stream ->
                StreamItem.Stream(
                    id = stream.id,
                    stream = stream,
                    state = if (stream in featuredStreams) featuredStreamItemState else StreamItemState.Thumbnail
                )
            }
        } else {
            val (thumbnailStreams, remainingStreams) = nonFeaturedStreams.withIndex().partition { it.index < maxAllowedThumbnailStreams - 1 }

            val featuredStreamItems = sortedFeaturedStreams.map { StreamItem.Stream(it.id, it, state = featuredStreamItemState) }
            val thumbnailStreamItems = thumbnailStreams.map { indexedValue ->
                StreamItem.Stream(indexedValue.value.id, indexedValue.value, state = StreamItemState.Thumbnail)
            }
            val moreItem = StreamItem.More(
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            featuredStreamItems + thumbnailStreamItems + moreItem
        }
    }
}



