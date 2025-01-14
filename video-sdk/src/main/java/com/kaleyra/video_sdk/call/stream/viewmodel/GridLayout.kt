package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface GridLayout: StreamLayout {

    val maxStreams: StateFlow<Int>
}

internal class GridLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxStreams: StateFlow<Int>,
    coroutineScope: CoroutineScope
) : GridLayout {

    private data class State(
        val allStreams: List<StreamUi> = emptyList(),
        val streamItems: List<StreamItem> = emptyList(),
        val maxStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<State> = MutableStateFlow(State())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            maxStreams
        ) { streams, maxStreams ->
            _internalState.update { state ->
                if (maxStreams < 1) {
                    state.copy(allStreams = streams, streamItems = emptyList(), maxStreams = 0)
                } else {
                    state.copy(
                        allStreams = streams,
                        streamItems = buildStreamItems(streams, maxStreams),
                        maxStreams = maxStreams
                    )
                }
            }
        }.launchIn(coroutineScope)

        _internalState
            .onEach { state -> _streamItems.value = state.streamItems }
            .launchIn(coroutineScope)
    }

    private fun buildStreamItems(streams: List<StreamUi>, maxFeaturedStreams: Int): List<StreamItem> {
        if (streams.isEmpty()) return emptyList()

        return if (streams.size <= maxFeaturedStreams) {
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            val (localCameraStream, otherStreams) = streams.partition { it.isMine }
            val maxOtherFeaturedStreams = maxFeaturedStreams - localCameraStream.size
            val (featuredStreams, remainingStreams) = otherStreams.withIndex().partition { it.index < maxOtherFeaturedStreams - 1 }

            val streamItems = featuredStreams.map { indexedValue -> StreamItem.Stream(indexedValue.value.id, indexedValue.value) } + localCameraStream.map { StreamItem.Stream(it.id, it) }
            val moreItem = StreamItem.More(
                id = remainingStreams.first().value.id,
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            streamItems + moreItem
        }
    }
}