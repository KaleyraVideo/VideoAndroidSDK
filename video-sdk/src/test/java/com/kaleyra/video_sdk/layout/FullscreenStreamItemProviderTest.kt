package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.FullscreenStreamItemProviderImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import org.junit.Assert
import org.junit.Test

class FullscreenStreamItemProviderTest {

    @Test
    fun `buildStreamItems returns empty list when fullscreen stream not found`() {
        val streams = listOf(
            StreamUi("stream1", "Stream 1"),
            StreamUi("stream2", "Stream 2")
        )
        val fullscreenStreamId = "stream3"
        val provider = FullscreenStreamItemProviderImpl()

        val result = provider.buildStreamItems(streams, fullscreenStreamId)

        Assert.assertEquals(emptyList<StreamItem>(), result)
    }

    @Test
    fun `buildStreamItems returns fullscreen stream item when fullscreen stream found`() {
        val streams = listOf(
            StreamUi("stream1", "Stream 1"),
            StreamUi("stream2", "Stream 2")
        )
        val fullscreenStreamId = "stream2"
        val provider = FullscreenStreamItemProviderImpl()

        val result = provider.buildStreamItems(streams, fullscreenStreamId)

        Assert.assertEquals(1, result.size)
        val streamItem = result.first()
        assert(streamItem is StreamItem.Stream)
        streamItem as StreamItem.Stream
        Assert.assertEquals("stream2", streamItem.id)
        Assert.assertEquals(StreamUi("stream2", "Stream 2"), streamItem.stream)
        Assert.assertEquals(StreamItemState.Featured.Fullscreen, streamItem.state)
    }
}