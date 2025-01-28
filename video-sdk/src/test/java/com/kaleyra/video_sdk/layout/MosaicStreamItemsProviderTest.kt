package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProviderImpl
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Test

class MosaicStreamItemsProviderTest {

    @Test
    fun `buildStreamItems returns empty list when streams is empty`() {
        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(3))
        val result = provider.buildStreamItems(emptyList())
        Assert.assertEquals(emptyList<StreamItem>(), result)
    }

    @Test
    fun `buildStreamItems returns empty list if maxStreams is less than zero`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val maxStreams = -4
        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(maxStreams))
        provider.buildStreamItems(streams)
    }

    @Test
    fun `buildStreamItems returns all streams if streams are less or equal than maxStreams`() {
        val stream1 = StreamUi("1", "stream1")
        val stream2 = StreamUi("2", "stream2")
        val streams = listOf(stream1, stream2)

        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(2))
        val result = provider.buildStreamItems(streams)
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[0])
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[1])
    }

    @Test
    fun `buildStreamItems returns featured and more streams if streams are more than maxStreams`() {
        val avatar1 = ImmutableUri(mockk())
        val avatar2 = ImmutableUri(mockk())
        val avatar3 = ImmutableUri(mockk())
        val stream1 = StreamUi("1", "stream1")
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3", avatar = avatar1)
        val stream4 = StreamUi("4", "stream4", avatar = avatar2)
        val stream5 = StreamUi("5", "stream5", avatar = avatar3)
        val streams = listOf(stream1, stream2, stream3, stream4, stream5)

        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(3))
        val result = provider.buildStreamItems(streams)
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[0])
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[1])
        Assert.assertEquals(
            StreamItem.More(
                users = listOf(
                    HiddenStreamUserPreview("3", "stream3", avatar1),
                    HiddenStreamUserPreview("4", "stream4", avatar2),
                    HiddenStreamUserPreview("5", "stream5", avatar3)
                )
            ),
            result[2]
        )
    }

    @Test
    fun `buildStreamItems returns local stream as the last featured stream if streams less or equal to maxStreams`() {
        val stream1 = StreamUi("1", "stream1", isMine = true)
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3")
        val streams = listOf(stream1, stream2, stream3)

        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(3))
        val result = provider.buildStreamItems(streams)
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[0])
        Assert.assertEquals(StreamItem.Stream("3", stream3), result[1])
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[2])
    }

    @Test
    fun `buildStreamItems returns local stream as the last visible featured stream if streams are more than maxStreams`() {
        val avatar1 = ImmutableUri(mockk())
        val avatar2 = ImmutableUri(mockk())
        val stream1 = StreamUi("1", "stream1", isMine = true)
        val stream2 = StreamUi("2", "stream2")
        val stream3 = StreamUi("3", "stream3", avatar = avatar1)
        val stream4 = StreamUi("4", "stream4", avatar = avatar2)
        val streams = listOf(stream1, stream2, stream3, stream4)

        val provider = MosaicStreamItemsProviderImpl(MutableStateFlow(3))
        val result = provider.buildStreamItems(streams)
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("2", stream2), result[0])
        Assert.assertEquals(StreamItem.Stream("1", stream1), result[1])
        Assert.assertEquals(
            StreamItem.More(
                users = listOf(
                    HiddenStreamUserPreview("3", "stream3", avatar1),
                    HiddenStreamUserPreview("4", "stream4", avatar2),
                )
            ),
            result[2]
        )
    }
}