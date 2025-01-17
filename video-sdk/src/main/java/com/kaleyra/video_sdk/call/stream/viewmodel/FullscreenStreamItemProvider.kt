package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi

internal class FullscreenStreamItemProvider(
    override val streams: List<StreamUi>,
    private val fullscreenStreamId: String
): StreamItemsProvider {

    override fun buildStreamItems(): List<StreamItem> {
        val fullscreenStream = streams.firstOrNull { it.id == fullscreenStreamId } ?: return emptyList()
        return listOf(StreamItem.Stream(fullscreenStream.id, fullscreenStream, state = StreamItemState.Featured.Fullscreen))
    }
}