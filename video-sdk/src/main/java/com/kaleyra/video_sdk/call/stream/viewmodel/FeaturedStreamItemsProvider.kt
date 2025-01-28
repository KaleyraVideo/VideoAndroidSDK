package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.flow.StateFlow

internal interface FeaturedStreamItemsProvider: StreamItemsProvider {

    val maxThumbnailStreams: StateFlow<Int>

    fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        featuredStreamItemState: StreamItemState.Featured = StreamItemState.Featured
    ): List<StreamItem>
}

internal class FeaturedStreamItemsProviderImpl(override val maxThumbnailStreams: StateFlow<Int>): FeaturedStreamItemsProvider {

    override fun buildStreamItems(
        streams: List<StreamUi>,
        featuredStreamIds: List<String>,
        featuredStreamItemState: StreamItemState.Featured
    ): List<StreamItem> {
        val maxThumbnailStreamsValue = maxThumbnailStreams.value
        if (maxThumbnailStreamsValue < 1 || featuredStreamIds.isEmpty()) return emptyList()

        val (featuredStreams, nonFeaturedStreams) = streams.partition { it.id in featuredStreamIds }

        val featuredStreamIdIndices = featuredStreamIds.withIndex().associate { indexedValue -> indexedValue.value to indexedValue.index }
        val sortedFeaturedStreams = featuredStreams.sortedBy { featuredStreamIdIndices[it.id] ?: Int.MAX_VALUE }
        val (thumbnailStreams, moreStreams) = if (nonFeaturedStreams.size <= maxThumbnailStreamsValue) {
            nonFeaturedStreams to emptyList()
        } else {
            with (nonFeaturedStreams) { take(maxThumbnailStreamsValue - 1) to drop(maxThumbnailStreamsValue - 1) }
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