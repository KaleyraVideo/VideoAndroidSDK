package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface StreamLayout {

    val streams: Flow<List<StreamUi>>

    val streamItems: Flow<List<StreamItem>>
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

    val isOneToOneCall: Flow<Boolean>

    val isDefaultBackCamera: Boolean

    val maxMosaicStreams: Flow<Int>

    val maxThumbnailStreams: Flow<Int>

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider
}

internal class AutoLayoutImpl(
    override val streams: Flow<List<StreamUi>>,
    override val isOneToOneCall: Flow<Boolean>,
    override val isDefaultBackCamera: Boolean,
    override val maxMosaicStreams: Flow<Int>,
    override val maxThumbnailStreams: Flow<Int>,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider,
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider,
    coroutineScope: CoroutineScope,
) : AutoLayout {

    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val isOneToOneCall: Boolean = false,
        val maxMosaicStreams: Int = 0,
        val maxThumbnailStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: Flow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            isOneToOneCall,
            maxMosaicStreams,
            maxThumbnailStreams
        ) { streams, isOneToOneCall, maxMosaicStreams, maxThumbnailStreams ->
            _internalState.update { state ->
                state.copy(
                    allStreams = streams,
                    isOneToOneCall = isOneToOneCall,
                    maxMosaicStreams = maxMosaicStreams,
                    maxThumbnailStreams = maxThumbnailStreams
                )
            }
        }.launchIn(coroutineScope)

        _internalState
            .onEach { internalState ->
                _streamItems.update { streamItems -> mapToStreamItems(internalState, streamItems) }
            }
            .launchIn(coroutineScope)
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

    private fun mapToStreamItems(state: LayoutState, currentStreamItems: List<StreamItem>): List<StreamItem> {
        return if (shouldArrangeByPriority(state, currentStreamItems)) {
            val featuredStreamId = findCurrentFeaturedStreamItem(currentStreamItems)?.takeIf { it.stream.video?.isScreenShare == true }?.id
            val sortedStreams = sortStreamsByPriority(
                streams = state.allStreams,
                featuredStreamId = featuredStreamId
            )

            featuredStreamItemsProvider.buildStreamItems(
                streams = sortedStreams,
                featuredStreamIds = listOfNotNull(sortedStreams.firstOrNull()?.id),
                maxThumbnailStreams = state.maxThumbnailStreams
            )
        } else {
            mosaicStreamItemsProvider.buildStreamItems(state.allStreams, state.maxMosaicStreams)
        }
    }
}
