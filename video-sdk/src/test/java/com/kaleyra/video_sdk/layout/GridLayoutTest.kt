package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.GridLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.UserPreview
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GridLayoutTest {

    private lateinit var gridLayout: GridLayoutImpl
    private lateinit var streamsFlow: MutableStateFlow<List<StreamUi>>
    private lateinit var maxFeaturedStreamsFlow: MutableStateFlow<Int>
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        streamsFlow = MutableStateFlow(emptyList())
        maxFeaturedStreamsFlow = MutableStateFlow(0)
        gridLayout = GridLayoutImpl(streamsFlow, maxFeaturedStreamsFlow, testScope)
    }

    @Test
    fun `streamItems is empty when streams are empty`() = runTest {
        maxFeaturedStreamsFlow = MutableStateFlow(2)
        Assert.assertEquals(emptyList<StreamItem>(), gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems is empty when maxFeaturedStreams is less than 1`() = runTest {
        streamsFlow.value = listOf(
            StreamUi("1", "user1"),
            StreamUi("2", "user2")
        )
        maxFeaturedStreamsFlow.value = 0
        Assert.assertEquals(emptyList<StreamItem>(), gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems contains all streams when streams size is less than to maxFeaturedStreams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxFeaturedStreamsFlow.value = 4

        val expected = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.Stream("2", stream2)
        )
        Assert.assertEquals(expected, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems contains all streams when streams size is equals to maxFeaturedStreams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxFeaturedStreamsFlow.value = 2

        val expected = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.Stream("2", stream2)
        )
        Assert.assertEquals(expected, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems contains featured streams and more item when streams size is greater than maxFeaturedStreams`() = runTest {
        val stream1 = StreamUi("1", "user1", avatar = ImmutableUri(mockk()))
        val stream2 = StreamUi("2", "user2", avatar = ImmutableUri(mockk()))
        val stream3 = StreamUi("3", "user3", avatar = ImmutableUri(mockk()))
        val stream4 = StreamUi("4", "user4", avatar = ImmutableUri(mockk()))
        streamsFlow.value = listOf(stream1, stream2, stream3, stream4)
        maxFeaturedStreamsFlow.value = 3

        val expected = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.Stream("2", stream2),
            StreamItem.More(
                id = "3",
                users = listOf(
                    UserPreview("user3", stream3.avatar),
                    UserPreview("user4", stream4.avatar)
                )
            )
        )
        Assert.assertEquals(expected, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems updates when streams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        maxFeaturedStreamsFlow.value = 2
        streamsFlow.value = listOf(stream1)
        val expected1 = listOf(StreamItem.Stream("1", stream1))
        Assert.assertEquals(expected1, gridLayout.streamItems.first())

        streamsFlow.value = listOf(stream1, stream2)
        val expected2 = listOf(StreamItem.Stream("1", stream1), StreamItem.Stream("2", stream2))
        Assert.assertEquals(expected2, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems updates when maxFeaturedStreams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2", avatar = ImmutableUri(mockk()))
        val stream3 = StreamUi("3", "user3", avatar = ImmutableUri(mockk()))
        streamsFlow.value = listOf(stream1, stream2, stream3)
        maxFeaturedStreamsFlow.value = 2
        val expected1 = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.More(
                id = "2",
                users = listOf(
                    UserPreview("user2", stream2.avatar),
                    UserPreview("user3", stream3.avatar)
                )
            )
        )
        Assert.assertEquals(expected1, gridLayout.streamItems.first())

        maxFeaturedStreamsFlow.value = 3
        val expected2 = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.Stream("2", stream2),
            StreamItem.Stream("3", stream3)
        )
        Assert.assertEquals(expected2, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems updates when both streams and maxFeaturedStreams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2", avatar = ImmutableUri(mockk()))
        val stream3 = StreamUi("3", "user3", avatar = ImmutableUri(mockk()))
        streamsFlow.value = listOf(stream1, stream2)
        maxFeaturedStreamsFlow.value = 1
        val expected1 = listOf(
            StreamItem.More(
                id = "1",
                users = listOf(
                    UserPreview("user1", stream1.avatar),
                    UserPreview("user2", stream2.avatar)
                )
            )
        )
        Assert.assertEquals(expected1, gridLayout.streamItems.first())

        streamsFlow.value = listOf(stream1, stream2, stream3)
        maxFeaturedStreamsFlow.value = 2
        val expected2 = listOf(
            StreamItem.Stream("1", stream1),
            StreamItem.More(
                id = "2",
                users = listOf(
                    UserPreview("user2", stream2.avatar),
                    UserPreview("user3", stream3.avatar)
                )
            )
        )
        Assert.assertEquals(expected2, gridLayout.streamItems.first())
    }

    @Test
    fun `streamItems only emits when there is a change`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "user1")

        val collectedItems = mutableListOf<List<StreamItem>>()
        val job = launch {
            gridLayout.streamItems.toList(collectedItems)
        }

        streamsFlow.value = listOf(stream1)
        maxFeaturedStreamsFlow.value = 2

        streamsFlow.value = listOf(stream1)
        maxFeaturedStreamsFlow.value = 2

        streamsFlow.value = listOf()

        assertEquals(3, collectedItems.size)
        assertTrue(collectedItems[0].isEmpty())
        assertEquals(1, collectedItems[1].size)
        assertTrue(collectedItems[2].isEmpty())
        job.cancel()
    }
}