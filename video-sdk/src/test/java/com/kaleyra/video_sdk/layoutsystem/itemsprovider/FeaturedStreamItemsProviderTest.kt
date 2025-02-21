package com.kaleyra.video_sdk.layoutsystem.itemsprovider

import android.net.Uri
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.layoutsystem.itemsprovider.FeaturedStreamItemsProviderImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItemState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Assert
import org.junit.Test

class FeaturedStreamItemsProviderTest {

    private val uriMock = mockk<Uri>()
    
    @Test
    fun `buildStreamItems returns empty list when featuredStreamIds is empty`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = emptyList<String>()
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(emptyList<StreamItem>(), result)
    }

    @Test
    fun `buildStreamItems returns all streams as featured when all are in featuredStreamIds`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = listOf("1", "2", "3")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 1)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(
            StreamItem.Stream(
                "2",
                StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
                StreamItemState.Featured
            ), result[1]
        )
        Assert.assertEquals(
            StreamItem.Stream(
                "3",
                StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
                StreamItemState.Featured
            ), result[2]
        )
    }

    @Test
    fun `buildStreamItems returns empty list if maxNonFeaturedStreams is less than zero`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = listOf("1")
        val provider = FeaturedStreamItemsProviderImpl()
        val result = provider.buildStreamItems(streams, featuredStreamIds, -4)
        assertEquals(emptyList<StreamItem>(), result)
    }

    @Test
    fun `buildStreamItems returns no thumbnails if maxNonFeaturedStreams is zero`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = listOf("1", "2")
        val provider = FeaturedStreamItemsProviderImpl()
        val result = provider.buildStreamItems(streams, featuredStreamIds, 0)
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(
            StreamItem.Stream(
                "2",
                StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
                StreamItemState.Featured
            ), result[1]
        )
    }

    @Test
    fun `buildStreamItems returns featured and thumbnail streams when non featured streams are less or equal than maxNonFeaturedStreams`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = listOf("1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[1])
        Assert.assertEquals(
            StreamItem.Stream(
                "3",
                StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
            ), result[2]
        )
    }

    @Test
    fun `buildStreamItems returns featured, thumbnail, and more when non featured streams are greater than maxNonFeaturedStreams`() {
        val avatar1 = ImmutableUri(uriMock)
        val avatar2 = ImmutableUri(uriMock)
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", avatar1)),
            StreamUi("4", UserInfo("userId4", "Stream 4", avatar2)),
        )
        val featuredStreamIds = listOf("1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[1])
        Assert.assertEquals(
            StreamItem.MoreStreams(
                users = listOf(
                    MoreStreamsUserPreview(
                        "3",
                        "Stream 3",
                        avatar1
                    ), MoreStreamsUserPreview("4", "Stream 4", avatar2)
                )
            ), result[2]
        )
    }

    @Test
    fun `buildStreamItems sorts featured streams according to featuredStreamIds order`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
        )
        val featuredStreamIds = listOf("3", "1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(3, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "3",
                StreamUi("3", UserInfo("userId3", "Stream 3", ImmutableUri(uriMock))),
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[1]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[2])
    }

    @Test
    fun `buildStreamItems handles featuredStreamIds with missing stream ids`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
                 StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      
        )
        val featuredStreamIds = listOf("3", "1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[1])
    }

    @Test
    fun `buildStreamItems handles featuredStreamIds with duplicated stream ids`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
                 StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      
        )
        val featuredStreamIds = listOf("1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(streams, featuredStreamIds, 2)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured
            ), result[0]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[1])
    }

    @Test
    fun `featured streams' state is the provided featuredStreamItemState`() {
        val streams = listOf(
            StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),
                 StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      
        )
        val featuredStreamIds = listOf("1")
        val provider = FeaturedStreamItemsProviderImpl()

        val result = provider.buildStreamItems(
            streams,
            featuredStreamIds,
            2,
            featuredStreamItemState = StreamItemState.Featured.Pinned
        )

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(
            StreamItem.Stream(
                "1",
                     StreamUi("1", UserInfo("userId1", "Stream 1", ImmutableUri(uriMock))),      
                StreamItemState.Featured.Pinned
            ), result[0]
        )
        Assert.assertEquals(StreamItem.Stream("2",      StreamUi("2", UserInfo("userId2", "Stream 2", ImmutableUri(uriMock))),      ), result[1])
    }
}
