package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * `ManualLayout` is an interface that defines the contract for a layout that allows manual control
 * over stream presentation, including pinning and fullscreen modes.
 *
 * This interface extends `StreamLayout`, inheriting its core stream layout capabilities,
 * and adds specific functions for manual stream management.
 */
internal interface ManualLayout : StreamLayout {

    /**
     * Pins a stream to the layout. Pinned streams are typically displayed prominently.
     *
     * @param streamId The ID of the stream to pin.
     * @param prepend If `true`, the stream is added to the beginning of the pinned list.
     *                If `false` (default), the stream is added to the end.
     * @param force If `true`, the stream will be pinned even if the maximum number of pinned
     *              streams has been reached, potentially unpinning another stream.
     *              If `false` (default), the stream will not be pinned if the maximum is reached.
     * @return `true` if the stream was successfully pinned, `false` otherwise.
     */
    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    /**
     * Unpins a stream from the layout. Unpinned streams are no longer displayed prominently.
     *
     * @param streamId The ID of the stream to unpin.
     */
    fun unpinStream(streamId: String)

    /**
     * Clears all pinned streams from the layout.
     */
    fun clearPinnedStreams()

    /**
     * Sets a stream to fullscreen mode. Only one stream can be in fullscreen at a time.
     *
     * @param streamId The ID of the stream to set to fullscreen.
     */
    fun setFullscreenStream(streamId: String)

    /**
     * Clears the fullscreen stream, if any.
     */
    fun clearFullscreenStream()
}

/**
 * `ManualLayoutImpl` is a concrete implementation of the `ManualLayout` interface.
 * It manages the layout of streams, including pinning, fullscreen, and mosaic views.
 *
 * @property layoutStreams A flow of the list of available streams (`StreamUi`).
 * @property layoutConstraints A flow of the constraints for the stream layout (`StreamLayoutConstraints`).
 * @property mosaicStreamItemsProvider A provider for building stream items in a mosaic layout.
 * @property featuredStreamItemsProvider A provider for building stream items in a featured layout.
 * @property coroutineScope The coroutine scope used for launching coroutines.
 */
internal class ManualLayoutImpl(
    override val layoutStreams: Flow<List<StreamUi>>,
    override val layoutConstraints: Flow<StreamLayoutConstraints>,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider = MosaicStreamItemsProviderImpl(),
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider = FeaturedStreamItemsProviderImpl(),
    coroutineScope: CoroutineScope,
): ManualLayout {

    /**
     * Represents the internal state of the layout.
     *
     * @property allStreams The list of all available streams.
     * @property pinnedStreamIds The list of IDs of streams that are currently pinned.
     * @property fullscreenStreamId The ID of the stream that is currently in fullscreen mode, or null if no stream is in fullscreen.
     * @property streamLayoutConstraints The constraints for the stream layout.
     */
    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val pinnedStreamIds: List<String> = emptyList(),
        val fullscreenStreamId: String? = null,
        val streamLayoutConstraints: StreamLayoutConstraints = StreamLayoutConstraints(),
    )

    /**
     * A mutable state flow that holds the current layout state.
     */
    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    override val streamItems: Flow<List<StreamItem>> = _internalState.map(::mapToStreamItems)

    init {
        combine(
            layoutStreams,
            layoutConstraints
        ) { newStreams, streamConstraints ->
            _internalState.update { state ->
                state.copy(
                    allStreams = newStreams,
                    pinnedStreamIds = retainPinnedStreamIds(state, newStreams, streamConstraints.featuredStreamThreshold),
                    fullscreenStreamId = newStreams.firstOrNull { it.id == state.fullscreenStreamId }?.id,
                    streamLayoutConstraints = streamConstraints,
                )
            }
        }.launchIn(coroutineScope)
    }

    override fun pinStream(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        _internalState.update { state ->
            val maxPinnedStreams = state.streamLayoutConstraints.featuredStreamThreshold
            val pinnedStreams = state.pinnedStreamIds
            val stream = state.allStreams.find { it.id == streamId } ?: return false

            val canPin = canPinStream(stream, pinnedStreams, maxPinnedStreams, force)
            if (!canPin) return false

            state.copy(pinnedStreamIds = pinStream(stream.id, state.pinnedStreamIds, maxPinnedStreams, prepend))
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

    /**
     * Retains only the pinned stream IDs that are present in the new stream list and respects the maximum allowed number of pinned streams.
     *
     * @param currentState The current layout state.
     * @param newStreams The new list of streams.
     * @param maxAllowedPinnedStreams The maximum number of pinned streams allowed.
     * @return A list of retained pinned stream IDs.
     */
    private fun retainPinnedStreamIds(currentState: LayoutState, newStreams: List<StreamUi>, maxAllowedPinnedStreams: Int): List<String> {
        if (maxAllowedPinnedStreams < 1) return emptyList()
        return currentState.pinnedStreamIds
            .mapNotNull { streamId -> newStreams.find { it.id == streamId } }
            .take(maxAllowedPinnedStreams)
            .map { it.id }
    }

    /**
     * Checks if a stream can be pinned.
     *
     * @param stream The stream to check.
     * @param pinnedStreamIds The list of currently pinned stream IDs.
     * @param maxAllowedPinnedStreams The maximum number of pinned streams allowed.
     * @param force Whether to force pinning.
     * @return `true` if the stream can be pinned, `false` otherwise.
     */
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

    /**
     * Pins a stream to the list of pinned streams.
     *
     * @param streamId The ID of the stream to pin.
     * @param currentPinnedStreamIds The current list of pinned stream IDs.
     * @param maxAllowedPinnedStreams The maximum number of pinned streams allowed.
     * @param prepend Whether to add the stream to the beginning of the list.
     * @return The updated list of pinned stream IDs.
     */
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

    /**
     * Maps the current layout state to a list of stream items.
     *
     * @param state The current layout state.
     * @return A list of stream items.
     */
    private fun mapToStreamItems(state: LayoutState): List<StreamItem> {
        return when {
            state.fullscreenStreamId != null -> featuredStreamItemsProvider.buildStreamItems(
                streams = state.allStreams,
                featuredStreamIds = listOf(state.fullscreenStreamId),
                maxNonFeaturedStreams = 0,
                featuredStreamItemState = StreamItemState.Featured.Fullscreen
            )
            state.pinnedStreamIds.isNotEmpty() -> featuredStreamItemsProvider.buildStreamItems(
                streams = state.allStreams,
                featuredStreamIds = state.pinnedStreamIds,
                maxNonFeaturedStreams = state.streamLayoutConstraints.thumbnailStreamThreshold,
                featuredStreamItemState = StreamItemState.Featured.Pinned
            )
            else -> mosaicStreamItemsProvider.buildStreamItems(state.allStreams, state.streamLayoutConstraints.mosaicStreamThreshold)
        }
    }
}





