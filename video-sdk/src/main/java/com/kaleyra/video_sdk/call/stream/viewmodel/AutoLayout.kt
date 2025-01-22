package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface StreamLayout {

    val streams: StateFlow<List<StreamUi>>

    val streamItems: StateFlow<List<StreamItem>>
}

sealed class StreamItemState {
    data object Standard : StreamItemState()

    sealed class Featured : StreamItemState() {

        companion object Companion : Featured()

        data object Pinned : Featured()

        data object Fullscreen : Featured()
    }

    data object Thumbnail : StreamItemState()
}

internal interface AutoLayout : StreamLayout {

    val isOneToOneCall: StateFlow<Boolean>

    val isDefaultBackCamera: Boolean

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider
}

internal class AutoLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val isOneToOneCall: StateFlow<Boolean>,
    override val isDefaultBackCamera: Boolean,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider,
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider,
    coroutineScope: CoroutineScope,
) : AutoLayout {

    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val isOneToOneCall: Boolean = false
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            isOneToOneCall
        ) { streams, isOneToOneCall ->
            _internalState.update { state ->
                state.copy(allStreams = streams, isOneToOneCall = isOneToOneCall)
            }
        }.launchIn(coroutineScope)

        coroutineScope.launch {
            _internalState.collectLatest { state ->
                val streamItemsProvider = if (shouldArrangeByPriority(state, _streamItems.value)) {
                    val featuredStreamId = findCurrentFeaturedStreamItem(_streamItems.value)?.takeIf { it.stream.video?.isScreenShare == true }?.id
                    val sortedStreams = sortStreamsByPriority(
                        streams = state.allStreams,
                        featuredStreamId = featuredStreamId
                    )

                    featuredStreamItemsProvider.buildStreamItems(
                        streams = sortedStreams,
                        featuredStreamIds = listOfNotNull(sortedStreams.firstOrNull()?.id),
                    )
                } else {
                    mosaicStreamItemsProvider.buildStreamItems(state.allStreams)
                }
                _streamItems.value = streamItemsProvider
            }
        }
    }

    private fun findCurrentFeaturedStreamItem(items: List<StreamItem>): StreamItem.Stream? {
        return items
            .filterIsInstance<StreamItem.Stream>()
            .firstOrNull { it.state == StreamItemState.Featured }
    }

    private fun shouldArrangeByPriority(
        state: LayoutState,
        currentStreamItems: List<StreamItem>,
    ): Boolean {
        val newRemoteScreenShareCount = state.allStreams.count { it.isRemoteScreenShare() }
        val previousRemoteScreenShareCount = currentStreamItems
            .filterIsInstance<StreamItem.Stream>()
            .count { it.stream.isRemoteScreenShare() }

        val hasOneRemoteScreenShare = newRemoteScreenShareCount == 1
        val hasTwoRemoteScreensWithStreamItems = newRemoteScreenShareCount > 1 && previousRemoteScreenShareCount != 0
        return state.isOneToOneCall || hasOneRemoteScreenShare || hasTwoRemoteScreensWithStreamItems
    }

    private fun sortStreamsByPriority(
        streams: List<StreamUi>,
        featuredStreamId: String? = null,
    ): List<StreamUi> {
        return streams
            .sortedWith(
                compareByDescending<StreamUi> { it.id == featuredStreamId }
                    .thenByDescending { it.isRemoteScreenShare() }
                    .thenByDescending { it.isMyCameraStream() && isDefaultBackCamera }
                    .thenByDescending { it.isRemoteCameraStream() }
            )
    }
}
