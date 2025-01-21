package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.flow.StateFlow

internal interface MosaicStreamItemsProvider: StreamItemsProvider {

    val maxStreams: StateFlow<Int>

    fun buildStreamItems(streams: List<StreamUi>): List<StreamItem>
}

internal class MosaicStreamItemsProviderImpl(override val maxStreams: StateFlow<Int>): MosaicStreamItemsProvider {

    override fun buildStreamItems(streams: List<StreamUi>): List<StreamItem> {
        val maxStreamsValue = maxStreams.value
        if (maxStreamsValue < 1 || streams.isEmpty()) return emptyList()

        return if (streams.size <= maxStreamsValue) {
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            val (localStream, otherStreams) = streams.partition { it.isMine }
            val maxOtherFeaturedStreams = maxStreamsValue - localStream.size
            val (featuredStreams, remainingStreams) = otherStreams.withIndex().partition { it.index < maxOtherFeaturedStreams - 1 }

            val streamItems = featuredStreams.map { indexedValue -> StreamItem.Stream(indexedValue.value.id, indexedValue.value) } + localStream.map { StreamItem.Stream(it.id, it) }
            val moreStreamItem = StreamItem.More(
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            streamItems + moreStreamItem
        }
    }
}

//    private val sortStreamsByPriority: (List<StreamUi>) -> List<StreamUi> = { value ->
//        value
//            .sortedWith(
//                compareByDescending<StreamUi> { it.video != null && it.video.isScreenShare }
//                    .thenByDescending { it.video != null && it.video.isEnabled }
//            )
//    }
//            val sortedStreams = sortStreamsByPriority(streams)