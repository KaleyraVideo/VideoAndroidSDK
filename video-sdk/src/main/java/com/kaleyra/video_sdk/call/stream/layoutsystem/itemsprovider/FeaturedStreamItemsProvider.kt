package com.kaleyra.video_sdk.call.stream.layoutsystem.itemsprovider

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

/**
 * Interface for providing a list of [StreamItem]s, with a focus on featured streams.
 *
 * This interface defines a contract for classes that can build a list of stream items,
 * prioritizing streams that are designated as "featured."
 */
internal interface FeaturedStreamItemsProvider : StreamItemsProvider {
    /**
     * Builds a list of [StreamItem]s based on the provided streams and featured stream IDs.
     *
     * @param streams The complete list of available streams.
     * @param featuredStreamIds The IDs of streams that should be considered "featured."
     * @param maxNonFeaturedStreams The maximum number of non-featured streams to include in the list.
     * @param featuredStreamItemState The state to assign to featured stream items. Defaults to [StreamItemState.Featured].
     * @return A list of [StreamItem]s, including featured streams and a limited number of non-featured streams.
     */
    fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        maxNonFeaturedStreams: Int,
        featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
    ): List<StreamItem>
}

/**
 * Implementation of [FeaturedStreamItemsProvider] that constructs a list of [StreamItem]s,
 * prioritizing featured streams and limiting the number of non-featured streams.
 */
internal class FeaturedStreamItemsProviderImpl : FeaturedStreamItemsProvider {
    /**
     * Builds a list of [StreamItem]s, prioritizing featured streams and limiting the number of non-featured streams.
     *
     * @param streams The complete list of available streams.
     * @param featuredStreamIds The IDs of streams that should be considered "featured."
     * @param maxNonFeaturedStreams The maximum number of non-featured streams to include in the list.
     * @param featuredStreamItemState The state to assign to featured stream items.
     * @return A list of [StreamItem]s, including featured streams, a limited number of non-featured streams,
     *         and optionally a "More Streams" item if there are remaining streams.
     */
    override fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        maxNonFeaturedStreams: Int,
        featuredStreamItemState: StreamItemState.Featured
    ): List<StreamItem> {
        // If there are no featured streams or the maximum number of non-featured streams is invalid, return an empty list.
        if (maxNonFeaturedStreams < 0 || featuredStreamIds.isEmpty()) return emptyList()

        // Separate the streams into featured and non-featured groups.
        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        // Create a map to quickly look up the index of each featured stream ID.
        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        // Sort the featured streams based on their order in the featuredStreamIds list.
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        // Determine which non-featured streams to show and which to consider "remaining."
        val (visibleNonFeaturedStreams, remainingStreams) = when {
            maxNonFeaturedStreams == 0 -> emptyList<StreamUi>() to emptyList() // If no non-featured streams are allowed, show none.
            nonFeaturedStreams.size <= maxNonFeaturedStreams -> nonFeaturedStreams to emptyList() // If there are fewer non-featured streams than the max, show them all.
            else -> with(nonFeaturedStreams) {
                take(maxNonFeaturedStreams - 1) to drop(maxNonFeaturedStreams - 1) // Otherwise, take the max number of non-featured streams and mark the rest as remaining.
            }
        }

        // Create StreamItems for the featured streams, setting their state.
        val featuredStreamItems = sortedFeaturedStreams.map {
            StreamItem.Stream(it.id, it, state = featuredStreamItemState)
        }
        // Create StreamItems for the visible non-featured streams.
        val visibleNonFeaturedStreamsItems = visibleNonFeaturedStreams.map {
            StreamItem.Stream(it.id, it)
        }
        // If there are remaining streams, create a "More Streams" item.
        val moreStreamsItem = remainingStreams.takeIf { it.isNotEmpty() }?.let { streams ->
            StreamItem.MoreStreams(users = streams.map { MoreStreamsUserPreview(it.id, it.username, it.avatar) })
        }

        // Combine the featured stream items, visible non-featured stream items, and the optional "More Streams" item.
        return featuredStreamItems + visibleNonFeaturedStreamsItems + listOfNotNull(moreStreamsItem)
    }
}