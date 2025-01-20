package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal class FeaturedStreamItemsProvider(
    override val streams: List<StreamUi>,
    private val featuredStreamIds: List<String>,
    private val maxThumbnailStreams: Int,
    private val featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        require(maxThumbnailStreams >= 0) {
            "maxAllowedThumbnailStreams must be greater or equal to 0"
        }
        if (featuredStreamIds.isEmpty()) return emptyList()

        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        val (thumbnailStreams, moreStreams) = if (nonFeaturedStreams.size <= maxThumbnailStreams) {
            nonFeaturedStreams to emptyList()
        } else {
            with (nonFeaturedStreams) { take(maxThumbnailStreams - 1) to drop(maxThumbnailStreams - 1) }
        }

        val featuredStreamItems = sortedFeaturedStreams.map {
            StreamItem.Stream(it.id, it, state = featuredStreamItemState)
        }
        val thumbnailStreamItems = thumbnailStreams.map {
            StreamItem.Stream(it.id, it, state = StreamItemState.Thumbnail)
        }
        val moreStreamItems = moreStreams.takeIf { it.isNotEmpty() }?.let { streams ->
            StreamItem.More(users = streams.map { UserPreview(it.username, it.avatar) })
        }

        return featuredStreamItems + thumbnailStreamItems + listOfNotNull(moreStreamItems)
    }
}