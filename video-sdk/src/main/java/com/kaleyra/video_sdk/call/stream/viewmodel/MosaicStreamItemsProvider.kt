package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal class MosaicStreamItemsProvider(
    override val streams: List<StreamUi>,
    private val maxFeaturedStreams: Int,
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        require(maxFeaturedStreams >= 0) {
            "maxFeaturedStreams must be greater or equal to 0"
        }
        if (streams.isEmpty()) return emptyList()

        return if (streams.size <= maxFeaturedStreams) {
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            val (localStream, otherStreams) = streams.partition { it.isMine }
            val maxOtherFeaturedStreams = maxFeaturedStreams - localStream.size
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