package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
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

    val maxPinnedStreams: StateFlow<Int>

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider

    val fullscreenStreamItemProvider: FullscreenStreamItemProvider

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpinStream(streamId: String)

    fun clearPinnedStreams()

    fun setFullscreenStream(streamId: String)

    fun clearFullscreenStream()
}

internal class ManualLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxPinnedStreams: StateFlow<Int>,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider,
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider,
    override val fullscreenStreamItemProvider: FullscreenStreamItemProvider,
    coroutineScope: CoroutineScope,
): ManualLayout {

    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val pinnedStreamIds: List<String> = emptyList(),
        val fullscreenStreamId: String? = null,
        val maxPinnedStreams: Int = 0,
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            maxPinnedStreams
        ) { newStreams, maxPinnedStreams ->
            _internalState.update { state ->
                state.copy(
                    allStreams = newStreams,
                    pinnedStreamIds = retainPinnedStreamIds(state, newStreams, maxPinnedStreams),
                    fullscreenStreamId = newStreams.firstOrNull { it.id == state.fullscreenStreamId }?.id,
                    maxPinnedStreams = max(0, maxPinnedStreams),
                )
            }
        }.launchIn(coroutineScope)

        coroutineScope.launch {
            _internalState
                .collectLatest { state ->
                    val streamItems = when {
                        state.fullscreenStreamId != null -> fullscreenStreamItemProvider.buildStreamItems(state.allStreams, state.fullscreenStreamId)
                        state.pinnedStreamIds.isNotEmpty() -> featuredStreamItemsProvider.buildStreamItems(
                            streams = state.allStreams,
                            featuredStreamIds = state.pinnedStreamIds,
                            featuredStreamItemState = StreamItemState.Featured.Pinned
                        )
                        else -> mosaicStreamItemsProvider.buildStreamItems(state.allStreams)
                    }
                    _streamItems.value = streamItems
                }
        }
    }

    override fun pinStream(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        _internalState.update { state ->
            val maxPinnedStreams = state.maxPinnedStreams
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





