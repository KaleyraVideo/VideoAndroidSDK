package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal interface MosaicStreamItemsProvider: StreamItemsProvider {
    fun buildStreamItems(streams: List<StreamUi>, maxStreams: Int): List<StreamItem>
}

internal class MosaicStreamItemsProviderImpl: MosaicStreamItemsProvider {

    override fun buildStreamItems(streams: List<StreamUi>, maxStreams: Int): List<StreamItem> {
        if (maxStreams < 1 || streams.isEmpty()) return emptyList()

        return if (streams.size <= maxStreams) {
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            val (localStream, otherStreams) = streams.partition { it.isMine }
            val maxOtherFeaturedStreams = maxStreams - localStream.size
            val (featuredStreams, remainingStreams) = otherStreams.withIndex()
                .partition { it.index < maxOtherFeaturedStreams - 1 }

            val streamItems = featuredStreams.map { indexedValue ->
                StreamItem.Stream(
                    indexedValue.value.id,
                    indexedValue.value
                )
            } + localStream.map { StreamItem.Stream(it.id, it) }
            val hiddenStreamsItem = StreamItem.HiddenStreams(
                users = remainingStreams.map {
                    HiddenStreamUserPreview(
                        it.value.id,
                        it.value.username,
                        it.value.avatar
                    )
                }
            )

            streamItems + hiddenStreamsItem
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