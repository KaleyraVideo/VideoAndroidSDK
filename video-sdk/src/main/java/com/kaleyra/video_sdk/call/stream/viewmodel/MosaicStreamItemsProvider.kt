package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

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

//    private val sortStreamsByPriority: (List<StreamUi>) -> List<StreamUi> = { value ->
//        value
//            .sortedWith(
//                compareByDescending<StreamUi> { it.video != null && it.video.isScreenShare }
//                    .thenByDescending { it.video != null && it.video.isEnabled }
//            )
//    }
//            val sortedStreams = sortStreamsByPriority(streams)