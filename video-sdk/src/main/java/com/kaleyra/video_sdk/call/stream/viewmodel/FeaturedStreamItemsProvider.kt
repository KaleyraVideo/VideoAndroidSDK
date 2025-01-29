package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal interface FeaturedStreamItemsProvider: StreamItemsProvider {
    fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        maxThumbnailStreams: Int,
        featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
    ): List<StreamItem>
}

internal class FeaturedStreamItemsProviderImpl: FeaturedStreamItemsProvider {

    override fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        maxThumbnailStreams: Int,
        featuredStreamItemState: StreamItemState.Featured
    ): List<StreamItem> {
        if (maxThumbnailStreams < 1 || featuredStreamIds.isEmpty()) return emptyList()

        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        val (thumbnailStreams, moreStreams) = if (nonFeaturedStreams.size <= maxThumbnailStreams) {
            nonFeaturedStreams to emptyList()
        } else {
            with(nonFeaturedStreams) {
                take(maxThumbnailStreams - 1) to drop(
                    maxThumbnailStreams - 1
                )
            }
        }

        val featuredStreamItems = sortedFeaturedStreams.map {
            StreamItem.Stream(it.id, it, state = featuredStreamItemState)
        }
        val thumbnailStreamItems = thumbnailStreams.map {
            StreamItem.Stream(it.id, it, state = StreamItemState.Thumbnail)
        }
        val hiddenStreamsItems = moreStreams.takeIf { it.isNotEmpty() }?.let { streams ->
            StreamItem.HiddenStreams(users = streams.map { HiddenStreamUserPreview(it.id, it.username, it.avatar) })
        }

        return featuredStreamItems + thumbnailStreamItems + listOfNotNull(hiddenStreamsItems)
    }
}