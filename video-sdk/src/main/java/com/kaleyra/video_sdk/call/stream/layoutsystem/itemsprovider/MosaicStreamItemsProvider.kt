package com.kaleyra.video_sdk.call.stream.layoutsystem.itemsprovider

import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

/**
 * `MosaicStreamItemsProvider` is an interface responsible for building a list of `StreamItem`s
 * for a mosaic layout. This layout typically displays multiple streams in a grid or similar arrangement.
 *
 * It extends the `StreamItemsProvider` interface, indicating that it provides stream items.
 */
internal interface MosaicStreamItemsProvider : StreamItemsProvider {
    /**
     * Builds a list of `StreamItem`s for a mosaic layout.
     *
     * @param streams The list of available `StreamUi` objects representing the streams.
     * @param maxStreams The maximum number of streams to include in the mosaic layout.
     * @return A list of `StreamItem`s to be displayed in the mosaic layout.
     */
    fun buildStreamItems(streams: List<StreamUi>, maxStreams: Int): List<StreamItem>
}

/**
 * `MosaicStreamItemsProviderImpl` is a concrete implementation of the `MosaicStreamItemsProvider` interface.
 * It provides the logic for building a list of `StreamItem`s for a mosaic layout.
 */
internal class MosaicStreamItemsProviderImpl : MosaicStreamItemsProvider {

    /**
     * Builds a list of `StreamItem`s for a mosaic layout.
     *
     * This implementation prioritizes other users' streams and places the local user's stream last.
     * If there are more streams than `maxStreams`, it includes a "MoreStreams" item to indicate the
     * presence of additional streams.
     *
     * @param streams The list of available `StreamUi` objects representing the streams.
     * @param maxStreams The maximum number of streams to include in the mosaic layout.
     * @return A list of `StreamItem`s to be displayed in the mosaic layout.
     */
    override fun buildStreamItems(streams: List<StreamUi>, maxStreams: Int): List<StreamItem> {
        // If there are no streams or the maximum number of streams is less than 1, return an empty list.
        if (maxStreams < 1 || streams.isEmpty()) return emptyList()

        // If the number of streams is less than or equal to the maximum, include all streams.
        return if (streams.size <= maxStreams) {
            // Sort the streams to put the local user's stream last, then map each stream to a StreamItem.Stream.
            streams.sortedBy { it.isMine }.map { stream -> StreamItem.Stream(stream.id, stream) }
        } else {
            // If there are more streams than the maximum, partition the streams into local and other streams.
            val (localStream, otherStreams) = streams.partition { it.isMine }
            // Calculate the maximum number of other streams to include based on the number of local streams.
            val maxOtherFeaturedStreams = maxStreams - localStream.size
            // Partition the other streams into featured streams (to be displayed) and remaining streams (to be included in the "MoreStreams" item).
            val (featuredStreams, remainingStreams) = otherStreams.withIndex()
                .partition { it.index < maxOtherFeaturedStreams - 1 }

            // Map the featured streams to StreamItem.Stream objects.
            val streamItems = featuredStreams.map { indexedValue ->
                StreamItem.Stream(
                    indexedValue.value.id,
                    indexedValue.value
                )
            } + localStream.map { StreamItem.Stream(it.id, it) } // Add the local stream to the list of stream items.
            // Create a "MoreStreams" item to represent the remaining streams.
            val moreStreamsItem = StreamItem.MoreStreams(
                users = remainingStreams.map {
                    MoreStreamsUserPreview(
                        it.value.id,
                        it.value.userInfo?.username ?: "",
                        it.value.userInfo?.image
                    )
                }
            )

            // Return the list of stream items plus the "MoreStreams" item.
            streamItems + moreStreamsItem
        }
    }
}