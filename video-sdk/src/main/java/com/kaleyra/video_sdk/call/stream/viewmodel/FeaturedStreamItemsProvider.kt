package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal class FeaturedStreamItemsProvider(
    override val streams: List<StreamUi>,
    private val featuredStreamIds: List<String>,
    private val maxAllowedThumbnailStreams: Int,
    private val featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        if (featuredStreamIds.isEmpty()) return emptyList()

        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        return if (nonFeaturedStreams.size <= maxAllowedThumbnailStreams) {
            (sortedFeaturedStreams + nonFeaturedStreams).map { stream ->
                StreamItem.Stream(
                    id = stream.id,
                    stream = stream,
                    state = if (stream in featuredStreams) featuredStreamItemState else StreamItemState.Thumbnail
                )
            }
        } else {
            val (thumbnailStreams, remainingStreams) = nonFeaturedStreams.withIndex().partition { it.index < maxAllowedThumbnailStreams - 1 }

            val featuredStreamItems = sortedFeaturedStreams.map { StreamItem.Stream(it.id, it, state = featuredStreamItemState) }
            val thumbnailStreamItems = thumbnailStreams.map { indexedValue ->
                StreamItem.Stream(indexedValue.value.id, indexedValue.value, state = StreamItemState.Thumbnail)
            }
            val moreItem = StreamItem.More(
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            featuredStreamItems + thumbnailStreamItems + moreItem
        }
    }
}