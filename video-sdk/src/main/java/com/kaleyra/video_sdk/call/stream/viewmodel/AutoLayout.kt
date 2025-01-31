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

/**
 * An interface for layouts that automatically adjust based on the available streams and constraints.
 */
internal interface AutoLayout : StreamLayout {
    /**
     * A flow of the current layout settings.
     */
    val layoutSettings: Flow<StreamLayoutSettings>
}

/**
 * An implementation of [AutoLayout] that dynamically arranges stream items.
 *
 * This class handles the logic for automatically arranging streams based on
 * the provided streams, layout constraints, and layout settings. It uses
 * [MosaicStreamItemsProvider] and [FeaturedStreamItemsProvider] to generate
 * the appropriate [StreamItem] list.
 *
 * @property layoutStreams A flow of the current list of [StreamUi] objects.
 * @property layoutConstraints A flow of the current [StreamLayoutConstraints].
 * @property layoutSettings A flow of the current [StreamLayoutSettings].
 * @property mosaicStreamItemsProvider The provider for generating mosaic-style stream items.
 * @property featuredStreamItemsProvider The provider for generating featured-style stream items.
 * @property coroutineScope The coroutine scope in which to launch the internal flows.
 */
internal class AutoLayoutImpl(
    override val layoutStreams: Flow<List<StreamUi>>,
    override val layoutConstraints: Flow<StreamLayoutConstraints>,
    override val layoutSettings: Flow<StreamLayoutSettings>,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider = MosaicStreamItemsProviderImpl(),
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider = FeaturedStreamItemsProviderImpl(),
    coroutineScope: CoroutineScope,
) : AutoLayout {

    /**
     * Represents the internal state of the layout.
     *
     * @property allStreams The current list of all available streams.
     * @property layoutConstraints The current layout constraints.
     * @property layoutSettings The current layout settings.
     */
    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val layoutConstraints: StreamLayoutConstraints = StreamLayoutConstraints(),
        val layoutSettings: StreamLayoutSettings = StreamLayoutSettings()
    )

    /**
     * A mutable state flow holding the current internal layout state.
     */
    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    /**
     * A mutable state flow holding the current list of [StreamItem] objects.
     */
    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: Flow<List<StreamItem>> = _streamItems

    init {
        // Combine the input flows and update the internal state.
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

        // Update the stream items whenever the internal state changes.
        _internalState
            .onEach { internalState ->
                _streamItems.update { streamItems -> mapToStreamItems(internalState, streamItems) }
            }
            .launchIn(coroutineScope)
    }

    /**
     * Finds the currently featured stream item, if any.
     *
     * @param items The list of [StreamItem] objects to search.
     * @return The featured [StreamItem.Stream] if found, `null` otherwise.
     */
    private fun findCurrentFeaturedStreamItem(items: List<StreamItem>): StreamItem.Stream? {
        return items
            .filterIsInstance<StreamItem.Stream>()
            .firstOrNull { it.state == StreamItemState.Featured }
    }

    /**
     * Determines whether to arrange streams by priority based on the current state and stream items.
     *
     * @param state The current layout state.
     * @param currentStreamItems The current list of stream items.
     * @return `true` if streams should be arranged by priority, `false` otherwise.
     */
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

    /**
     * Sorts the streams based on a defined priority order.
     *
     * @param streams The list of [StreamUi] objects to sort.
     * @param featuredStreamId The ID of the currently featured stream, if any.
     * @param defaultCameraIsBack Indicates if the default camera is the back camera.
     * @return The sorted list of [StreamUi] objects.
     */
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

    /**
     * Maps the current layout state to a list of [StreamItem] objects.
     *
     * This function determines whether to use the featured stream layout or the mosaic layout
     * based on the current constraints and settings.
     *
     * @param state The current layout state.
     * @param currentStreamItems The current list of stream items.
     * @return The new list of [StreamItem] objects.
     */
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
                maxNonFeaturedStreams = state.layoutConstraints.thumbnailStreamThreshold
            )
        } else {
            mosaicStreamItemsProvider.buildStreamItems(state.allStreams, state.layoutConstraints.mosaicStreamThreshold)
        }
    }
}
