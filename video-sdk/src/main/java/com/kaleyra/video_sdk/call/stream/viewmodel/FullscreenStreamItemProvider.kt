package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal interface FullscreenStreamItemProvider: StreamItemsProvider {

    fun buildStreamItems(streams: List<StreamUi>, fullscreenStreamId: String): List<StreamItem>
}

internal class FullscreenStreamItemProviderImpl: FullscreenStreamItemProvider {

    override fun buildStreamItems(streams: List<StreamUi>, fullscreenStreamId: String): List<StreamItem> {
        val fullscreenStream = streams.firstOrNull { it.id == fullscreenStreamId } ?: return emptyList()
        return listOf(StreamItem.Stream(fullscreenStream.id, fullscreenStream, state = StreamItemState.Featured.Fullscreen))
    }
}