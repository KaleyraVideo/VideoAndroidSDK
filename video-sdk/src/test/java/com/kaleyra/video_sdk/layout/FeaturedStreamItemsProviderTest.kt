package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProviderImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import com.kaleyra.video_sdk.call.stream.viewmodel.UserPreview
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Test

class FeaturedStreamItemsProviderTest {

    @Test
    fun `buildStreamItems returns empty list when featuredStreamIds is empty`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2")
        )
        val featuredStreamIds = emptyList<String>()
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(emptyList<StreamItem>(), result)
    }

    @Test
    fun `buildStreamItems returns all streams as featured when all are in featuredStreamIds`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val featuredStreamIds = listOf("1", "2", "3")
        val maxThumbnailStreams = MutableStateFlow(1)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Featured), result[1])
        Assert.assertEquals(StreamItem.Stream("3", StreamUi("3", "Stream 3"), StreamItemState.Featured), result[2])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `buildStreamItems throws an IllegalArgumentException if maxThumbnailStreams is less than zero`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val featuredStreamIds = listOf("1")
        val maxThumbnailStreams = MutableStateFlow(-4)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)
        provider.buildStreamItems(streams, featuredStreamIds)
    }

    @Test
    fun `buildStreamItems returns featured and thumbnail streams when non featured streams are less or equal than maxThumbnailStreams`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val featuredStreamIds = listOf("1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[1])
        Assert.assertEquals(StreamItem.Stream("3", StreamUi("3", "Stream 3"), StreamItemState.Thumbnail), result[2])
    }

    @Test
    fun `buildStreamItems returns featured, thumbnail, and more when non featured streams are greater than maxThumbnailStreams`() {
        val avatar1 = ImmutableUri(mockk())
        val avatar2 = ImmutableUri(mockk())
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3", avatar = avatar1),
            StreamUi("4", "Stream 4", avatar = avatar2)
        )
        val featuredStreamIds = listOf("1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[1])
        Assert.assertEquals(StreamItem.More(users = listOf(UserPreview("Stream 3", avatar1), UserPreview("Stream 4", avatar2))), result[2])
    }

    @Test
    fun `buildStreamItems sorts featured streams according to featuredStreamIds order`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2"),
            StreamUi("3", "Stream 3")
        )
        val featuredStreamIds = listOf("3", "1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(StreamItem.Stream("3", StreamUi("3", "Stream 3"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[1])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[2])
    }

    @Test
    fun `buildStreamItems handles featuredStreamIds with missing stream ids`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2")
        )
        val featuredStreamIds = listOf("3", "1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[1])
    }

    @Test
    fun `buildStreamItems handles featuredStreamIds with duplicated stream ids`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2")
        )
        val featuredStreamIds = listOf("1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[1])
    }

    @Test
    fun `featured streams' state is the provided featuredStreamItemState`() {
        val streams = listOf(
            StreamUi("1", "Stream 1"),
            StreamUi("2", "Stream 2")
        )
        val featuredStreamIds = listOf("1")
        val maxThumbnailStreams = MutableStateFlow(2)
        val provider = FeaturedStreamItemsProviderImpl(maxThumbnailStreams)

        val result = provider.buildStreamItems(streams, featuredStreamIds, featuredStreamItemState = StreamItemState.Featured.Pinned)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(StreamItem.Stream("1", StreamUi("1", "Stream 1"), StreamItemState.Featured.Pinned), result[0])
        Assert.assertEquals(StreamItem.Stream("2", StreamUi("2", "Stream 2"), StreamItemState.Thumbnail), result[1])
    }
}
