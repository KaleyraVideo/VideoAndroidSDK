package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.UserPreview
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class MosaicStreamItemsProviderTest {

    @Test
    fun `buildStreamItems returns empty list when streams is empty`() {
        val provider = MosaicStreamItemsProvider(emptyList(), 3)
        val result = provider.buildStreamItems()
        Assert.assertEquals(emptyList<StreamItem>(), result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `buildStreamItems throws an IllegalArgumentException if maxFeaturedStreams is less than zero`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val maxFeaturedStreams = -4
        val provider = MosaicStreamItemsProvider(streams, maxFeaturedStreams)
        provider.buildStreamItems()
    }

    @Test
    fun `buildStreamItems returns all streams if streams are less or equal than maxFeaturedStreams`() {
        val stream1 = StreamUi("1", "stream1")
        val stream2 = StreamUi("2", "stream2")
        val streams = listOf(stream1, stream2)

        val provider = MosaicStreamItemsProvider(streams, 2)
        val result = provider.buildStreamItems()
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[0])
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[1])
    }

    @Test
    fun `buildStreamItems returns featured and more streams if streams are more than maxFeaturedStreams`() {
        val avatar1 = ImmutableUri(mockk())
        val avatar2 = ImmutableUri(mockk())
        val avatar3 = ImmutableUri(mockk())
        val stream1 = StreamUi("1", "stream1")
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3", avatar = avatar1)
        val stream4 = StreamUi("4", "stream4", avatar = avatar2)
        val stream5 = StreamUi("5", "stream5", avatar = avatar3)
        val streams = listOf(stream1, stream2, stream3, stream4, stream5)

        val provider = MosaicStreamItemsProvider(streams, 3)
        val result = provider.buildStreamItems()
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[0])
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[1])
        Assert.assertEquals(
            StreamItem.More(
                users = listOf(
                    UserPreview("stream3", avatar1),
                    UserPreview("stream4", avatar2),
                    UserPreview("stream5", avatar3)
                )
            ),
            result[2]
        )
    }

    @Test
    fun `buildStreamItems returns local stream as the last featured stream if streams less or equal to maxFeaturedStreams`() {
        val stream1 = StreamUi("1", "stream1", isMine = true)
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3")
        val streams = listOf(stream1, stream2, stream3)

        val provider = MosaicStreamItemsProvider(streams, 3)
        val result = provider.buildStreamItems()
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[0])
        Assert.assertEquals(StreamItem.Stream("3", stream3), result[1])
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[2])
    }

    @Test
    fun `buildStreamItems returns local stream as the last visible featured stream if streams are more than maxFeaturedStreams`() {
        val avatar1 = ImmutableUri(mockk())
        val avatar2 = ImmutableUri(mockk())
        val stream1 = StreamUi("1", "stream1", isMine = true)
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3", avatar = avatar1)
        val stream4 = StreamUi("4", "stream4", avatar = avatar2)
        val streams = listOf(stream1, stream2, stream3, stream4)

        val provider = MosaicStreamItemsProvider(streams, 3)
        val result = provider.buildStreamItems()
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[0])
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[1])
        Assert.assertEquals(
            StreamItem.More(
                users = listOf(
                    UserPreview("stream3", avatar1),
                    UserPreview("stream4", avatar2),
                )
            ),
            result[2]
        )
    }
}