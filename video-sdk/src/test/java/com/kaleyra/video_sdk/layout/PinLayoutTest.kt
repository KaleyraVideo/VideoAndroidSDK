package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.PinLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import com.kaleyra.video_sdk.call.stream.viewmodel.UserPreview
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinLayoutImplTest {

    private lateinit var pinLayout: PinLayoutImpl
    private lateinit var streamsFlow: MutableStateFlow<List<StreamUi>>
    private lateinit var maxAllowedPinnedStreamsFlow: MutableStateFlow<Int>
    private lateinit var maxAllowedThumbnailStreamsFlow: MutableStateFlow<Int>
    private lateinit var callUserMessageProvider: CallUserMessagesProvider
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        streamsFlow = MutableStateFlow(emptyList())
        maxAllowedPinnedStreamsFlow = MutableStateFlow(0)
        maxAllowedThumbnailStreamsFlow = MutableStateFlow(0)
        callUserMessageProvider = mockk()
        pinLayout = PinLayoutImpl(
            streamsFlow,
            maxAllowedPinnedStreamsFlow,
            maxAllowedThumbnailStreamsFlow,
            callUserMessageProvider,
            testScope
        )
    }

    @Test
    fun `streamItems is empty initially`() = runTest {
        Assert.assertEquals(emptyList<StreamItem>(), pinLayout.streamItems.first())
    }

    @Test
    fun `streamItems is empty when maxAllowedPinnedStreams is less than 1`() = runTest {
        streamsFlow.value = listOf(StreamUi("1", "user1"))
        maxAllowedPinnedStreamsFlow.value = 0
        Assert.assertEquals(emptyList<StreamItem>(), pinLayout.streamItems.first())
    }

    @Test
    fun `streamItems is empty when no streams are pinned`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 2

        Assert.assertEquals(emptyList<StreamItem>(), pinLayout.streamItems.first())
    }

    @Test
    fun `pinning a stream appends it to pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        streamsFlow.value = listOf(stream1)
        maxAllowedPinnedStreamsFlow.value = 1
        Assert.assertTrue(pinLayout.pin("1"))
        val expected = listOf(StreamItem.Stream("1", stream1, StreamItemState.PINNED))
        Assert.assertEquals(expected, pinLayout.streamItems.first())
    }

    @Test
    fun `pinning a stream when maxAllowedPinnedStreams is reached returns false`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 1
        pinLayout.pin("1")
        Assert.assertFalse(pinLayout.pin("2"))
    }

    @Test
    fun `pinning an already pinned stream returns false`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 2
        pinLayout.pin("1")
        Assert.assertFalse(pinLayout.pin("1"))
    }

    @Test
    fun `pinning a stream when maxAllowedPinnedStreams is reached and force is true returns true`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 1
        pinLayout.pin("1")
        Assert.assertTrue(pinLayout.pin("2", force = true))
    }

    @Test
    fun `pinning a stream when maxAllowedPinnedStreams is reached and force true, replaced the first pinned stream`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        val stream3 = StreamUi("3", "user3")
        streamsFlow.value = listOf(stream1, stream2, stream3)
        maxAllowedPinnedStreamsFlow.value = 2
        maxAllowedThumbnailStreamsFlow.value = 2

        pinLayout.pin("1")
        pinLayout.pin("2")
        val expected1 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.PINNED),
            StreamItem.Stream("3", stream3, StreamItemState.THUMBNAIL)
        )
        assertEquals(expected1, pinLayout.streamItems.first())

        pinLayout.pin("3", force = true)
        val expected2 = listOf(
            StreamItem.Stream("2", stream2, StreamItemState.PINNED),
            StreamItem.Stream("3", stream3, StreamItemState.PINNED),
            StreamItem.Stream("1", stream1, StreamItemState.THUMBNAIL)
        )
        assertEquals(expected2, pinLayout.streamItems.first())
    }

    @Test
    fun `pinning a stream when maxAllowedPinnedStreams is reached and prepend true and force true, replaced the last pinned stream`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        val stream3 = StreamUi("3", "user3")
        streamsFlow.value = listOf(stream1, stream2, stream3)
        maxAllowedPinnedStreamsFlow.value = 2
        maxAllowedThumbnailStreamsFlow.value = 2

        pinLayout.pin("1")
        pinLayout.pin("2")
        val expected1 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.PINNED),
            StreamItem.Stream("3", stream3, StreamItemState.THUMBNAIL)
        )
        assertEquals(expected1, pinLayout.streamItems.first())

        pinLayout.pin("3", prepend = true, force = true)
        val expected2 = listOf(
            StreamItem.Stream("3", stream3, StreamItemState.PINNED),
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.THUMBNAIL)
        )
        assertEquals(expected2, pinLayout.streamItems.first())
    }

    @Test
    fun `unpinning a stream removes it from pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        streamsFlow.value = listOf(stream1)
        maxAllowedPinnedStreamsFlow.value = 1
        pinLayout.pin("1")
        pinLayout.unpin("1")
        Assert.assertEquals(emptyList<StreamItem>(), pinLayout.streamItems.first())
    }

    @Test
    fun `clear removes all pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 2
        pinLayout.pin("1")
        pinLayout.pin("2")
        pinLayout.clear()
        Assert.assertEquals(emptyList<StreamItem>(), pinLayout.streamItems.first())
    }

    @Test
    fun `pinning a stream with prepend true, prepends it to pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 2
        pinLayout.pin("1")
        pinLayout.pin("2", prepend = true)
        val expected = listOf(
            StreamItem.Stream("2", stream2, StreamItemState.PINNED),
            StreamItem.Stream("1", stream1, StreamItemState.PINNED)
        )
        Assert.assertEquals(expected, pinLayout.streamItems.first())
    }

    @Test
    fun `thumbnail overflow if thumnails exceed maxAllowedThumbnailStreams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        val stream3 = StreamUi("3", "user3", avatar = ImmutableUri(mockk()))
        val stream4 = StreamUi("4", "user4", avatar = ImmutableUri(mockk()))
        streamsFlow.value = listOf(stream1, stream2, stream3, stream4)
        maxAllowedPinnedStreamsFlow.value = 2
        maxAllowedThumbnailStreamsFlow.value = 2

        pinLayout.pin("1")
        val expected1 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.THUMBNAIL),
            StreamItem.More(
                id = "3",
                users = listOf(
                    UserPreview("user3", avatar = stream3.avatar),
                    UserPreview("user4", avatar = stream4.avatar),
                )
            )
        )
        assertEquals(expected1, pinLayout.streamItems.first())
    }

    @Test
    fun `pinning a new remote screen share sends a user message`() = runTest {
        val stream1 = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = true))
        streamsFlow.value = listOf(stream1)
        verify {
            callUserMessageProvider.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }

    @Test
    fun `hasPinnedStreams returns true when there are pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        streamsFlow.value = listOf(stream1)
        maxAllowedPinnedStreamsFlow.value = 1
        pinLayout.pin("1")
        Assert.assertTrue(pinLayout.hasPinnedStreams)
    }

    @Test
    fun `hasPinnedStreams returns false when there are no pinned streams`() = runTest {
        val stream1 = StreamUi("1", "user1")
        streamsFlow.value = listOf(stream1)
        maxAllowedPinnedStreamsFlow.value = 1
        Assert.assertFalse(pinLayout.hasPinnedStreams)
    }

    @Test
    fun `streamItems updates when streams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        val stream3 = StreamUi("3", "user3")
        maxAllowedPinnedStreamsFlow.value = 2
        maxAllowedThumbnailStreamsFlow.value = 2
        streamsFlow.value = listOf(stream1, stream2)

        pinLayout.pin("1")
        pinLayout.pin("2")
        val expected1 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.PINNED)
        )
        Assert.assertEquals(expected1, pinLayout.streamItems.first())

        streamsFlow.value = listOf(stream1, stream3)
        val expected2 = listOf(StreamItem.Stream("1", stream1, StreamItemState.PINNED), StreamItem.Stream("3", stream3, StreamItemState.THUMBNAIL))
        Assert.assertEquals(expected2, pinLayout.streamItems.first())
    }

    @Test
    fun `streamItems updates when maxAllowedPinnedStreams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        streamsFlow.value = listOf(stream1, stream2)
        maxAllowedPinnedStreamsFlow.value = 2
        maxAllowedThumbnailStreamsFlow.value = 2

        pinLayout.pin("1")
        pinLayout.pin("2")
        val expected1 = listOf(StreamItem.Stream("1", stream1, StreamItemState.PINNED), StreamItem.Stream("2", stream2, StreamItemState.PINNED))
        Assert.assertEquals(expected1, pinLayout.streamItems.first())

        maxAllowedPinnedStreamsFlow.value = 1
        val expected2 = listOf(StreamItem.Stream("1", stream1, StreamItemState.PINNED), StreamItem.Stream("2", stream2, StreamItemState.THUMBNAIL))
        Assert.assertEquals(expected2, pinLayout.streamItems.first())
    }

    @Test
    fun `streamItems updates when maxAllowedThumbnailStreams flow changes`() = runTest {
        val stream1 = StreamUi("1", "user1")
        val stream2 = StreamUi("2", "user2")
        val stream3 = StreamUi("3", "user3", avatar = ImmutableUri(mockk()))
        val stream4 = StreamUi("4", "user4", avatar = ImmutableUri(mockk()))
        streamsFlow.value = listOf(stream1, stream2, stream3, stream4)
        maxAllowedPinnedStreamsFlow.value = 1
        maxAllowedThumbnailStreamsFlow.value = 2
        pinLayout.pin("1")
        val expected1 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.THUMBNAIL),
            StreamItem.More(
                id = "3",
                users = listOf(
                    UserPreview("user3", avatar = stream3.avatar),
                    UserPreview("user4", avatar = stream4.avatar),
                )
            )
        )
        Assert.assertEquals(expected1, pinLayout.streamItems.first())

        maxAllowedThumbnailStreamsFlow.value = 3
        val expected2 = listOf(
            StreamItem.Stream("1", stream1, StreamItemState.PINNED),
            StreamItem.Stream("2", stream2, StreamItemState.THUMBNAIL),
            StreamItem.Stream("3", stream3, StreamItemState.THUMBNAIL),
            StreamItem.Stream("4", stream4, StreamItemState.THUMBNAIL)
        )
        Assert.assertEquals(expected2, pinLayout.streamItems.first())
    }

    @Test
    fun `streamItems only emits when there is a change`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "user1")
        streamsFlow.value = listOf(stream1)
        maxAllowedPinnedStreamsFlow.value = 1

        val collectedItems = mutableListOf<List<StreamItem>>()
        val job = launch {
            pinLayout.streamItems.toList(collectedItems)
        }

        pinLayout.pin("1")

        pinLayout.pin("1")

        pinLayout.unpin("1")

        pinLayout.unpin("1")

        Assert.assertEquals(3, collectedItems.size)
        assertTrue(collectedItems[0].isEmpty())
        Assert.assertEquals(1, collectedItems[1].size)
        assertTrue(collectedItems[2].isEmpty())
        job.cancel()
    }
}