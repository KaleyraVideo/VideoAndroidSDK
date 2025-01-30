package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface AutoLayout : StreamLayout {

    val layoutSettings: Flow<StreamLayoutSettings>

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider
}

internal class AutoLayoutImpl(
    override val layoutStreams: Flow<List<StreamUi>>,
    override val layoutConstraints: Flow<StreamLayoutConstraints>,
    override val layoutSettings: Flow<StreamLayoutSettings>,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider = MosaicStreamItemsProviderImpl(),
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider = FeaturedStreamItemsProviderImpl(),
    coroutineScope: CoroutineScope,
) : AutoLayout {

    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val layoutConstraints: StreamLayoutConstraints = StreamLayoutConstraints(),
        val layoutSettings: StreamLayoutSettings = StreamLayoutSettings()
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: Flow<List<StreamItem>> = _streamItems

    init {
        combine(
            layoutStreams,
            layoutConstraints,
            layoutSettings
        ) { streams, layoutConstraints, layoutSettings ->
            _internalState.update { state ->
                state.copy(
                    allStreams = streams,
                    layoutConstraints = layoutConstraints,
                    layoutSettings = layoutSettings
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
        return !state.layoutSettings.isGroupCall || hasOneRemoteScreenShare || hasTwoRemoteScreensWithStreamItems
    }

    private fun sortStreamsByPriority(
        streams: List<StreamUi>,
        featuredStreamId: String? = null,
        defaultCameraIsBack: Boolean
    ): List<StreamUi> {
        return streams
            .sortedWith(
                compareByDescending<StreamUi> { it.id == featuredStreamId }
                    .thenByDescending { it.isRemoteScreenShare() }
                    .thenByDescending { it.isMyCameraStream() && defaultCameraIsBack }
                    .thenByDescending { it.isRemoteCameraStream() }
            )
    }

    private fun mapToStreamItems(state: LayoutState, currentStreamItems: List<StreamItem>): List<StreamItem> {
        return if (state.layoutConstraints.featuredStreamThreshold > 0 && shouldArrangeByPriority(state, currentStreamItems)) {
            val featuredStreamId = findCurrentFeaturedStreamItem(currentStreamItems)?.takeIf { it.stream.video?.isScreenShare == true }?.id
            val sortedStreams = sortStreamsByPriority(
                streams = state.allStreams,
                featuredStreamId = featuredStreamId,
                defaultCameraIsBack = state.layoutSettings.defaultCameraIsBack
            )

            featuredStreamItemsProvider.buildStreamItems(
                streams = sortedStreams,
                featuredStreamIds = listOfNotNull(sortedStreams.firstOrNull()?.id),
                maxThumbnailStreams = state.layoutConstraints.thumbnailStreamThreshold
            )
        } else {
            mosaicStreamItemsProvider.buildStreamItems(state.allStreams, state.layoutConstraints.mosaicStreamThreshold)
        }
    }
}
