@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.FullscreenStreamItemProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ManualLayoutImplTest {

    private val streamsFlow: MutableStateFlow<List<StreamUi>> = MutableStateFlow(listOf())

    private val maxPinnedStreamsFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var testScope: CoroutineScope

    private lateinit var manualLayout: ManualLayoutImpl

    private lateinit var mosaicStreamItemsProvider: MosaicStreamItemsProvider

    private lateinit var featuredStreamItemsProviderMock: FeaturedStreamItemsProvider

    private lateinit var fullscreenStreamItemProviderMock: FullscreenStreamItemProvider

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(UnconfinedTestDispatcher())
        mosaicStreamItemsProvider = object : MosaicStreamItemsProvider {
            override val maxStreams: StateFlow<Int> = MutableStateFlow(0)
            override fun buildStreamItems(
                streams: List<StreamUi>
            ): List<StreamItem> {
                return streams.map { stream ->
                    StreamItem.Stream(stream.id, stream)
                }
            }
        }
        featuredStreamItemsProviderMock = object : FeaturedStreamItemsProvider {
            override val maxThumbnailStreams: StateFlow<Int> = MutableStateFlow(0)
            override fun buildStreamItems(
                streams: List<StreamUi>,
                featuredStreamIds: List<String>,
                featuredStreamItemState: StreamItemState.Featured,
            ): List<StreamItem> {
                return featuredStreamIds.filter { id -> id in streams.map { it.id } }.map {
                    StreamItem.Stream(it, StreamUi(it, "stream$it"), state = StreamItemState.Featured.Pinned)
                }
            }
        }
        fullscreenStreamItemProviderMock = object : FullscreenStreamItemProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                fullscreenStreamId: String,
            ): List<StreamItem> {
                return streams.firstOrNull { it.id == fullscreenStreamId }?.let { stream ->
                    listOf(StreamItem.Stream(stream.id, stream, state = StreamItemState.Featured.Fullscreen))
                } ?: emptyList<StreamItem>()
            }
        }

        manualLayout = ManualLayoutImpl(
            streamsFlow,
            maxPinnedStreamsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProvider,
            featuredStreamItemsProvider = featuredStreamItemsProviderMock,
            fullscreenStreamItemProvider = fullscreenStreamItemProviderMock,
            coroutineScope = testScope
        )
    }

    @Test
    fun `streamItems is an empty list when streams is empty`() = runTest(testDispatcher) {
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `pinStream add the pinned stream as the last in the pinned streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 2

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2")
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream("2", StreamUi("2", "stream2"), state = StreamItemState.Featured.Pinned)
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream add the pinned stream as first in the pinned streams list when prepend is true`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 2

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2", prepend = true)
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream(id = "2", stream = StreamUi("2", "stream2"), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream(id = "1", stream = StreamUi("1", "stream1"), state = StreamItemState.Featured.Pinned),
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream when the maxPinnedStream limit is reached`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream(id = "1", stream = StreamUi("1", "stream1"), state = StreamItemState.Featured.Pinned)
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream add the pinned stream even if the pin limit is reached when force is true`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2", force = true)
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("2", StreamUi("2", "stream2"), StreamItemState.Featured.Pinned)), streamItems)
    }

    @Test
    fun `pinStream don't add the pinned stream when maxPinnedStreams is less than 0`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = -1

        manualLayout.pinStream("1")

        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream if the maxPinnedStream is zero`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 0

        val result = manualLayout.pinStream("1")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream if the stream is already pinned`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("1")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("1", StreamUi("1", "stream1"), StreamItemState.Featured.Pinned)), streamItems)
    }

    @Test
    fun `pinStream don't pin the stream when the id is not found in the streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"))
        maxPinnedStreamsFlow.value = 1

        val result = manualLayout.pinStream("2")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("1", StreamUi("1", "stream1"))), streamItems)
    }

    @Test
    fun `unpinStream removes the stream from the pinned streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        manualLayout.unpinStream("1")
        val streamItems2 = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("1", StreamUi("1", "stream1"))), streamItems2)
    }

    @Test
    fun `clearPinnedStreams removes all the pinned streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 2

        manualLayout.pinStream("1")
        manualLayout.pinStream("2")

        manualLayout.clearPinnedStreams()
        val streamItems2 = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems2
        )
    }

    @Test
    fun `setFullscreenStream set the stream items list to only the fullscreen one`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))

        manualLayout.setFullscreenStream("1")
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf<StreamItem>(StreamItem.Stream("1", StreamUi("1", "stream1"), StreamItemState.Featured.Fullscreen)), streamItems)
    }

    @Test
    fun `clearFullscreenStream remove the fullscreen stream item`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))

        manualLayout.setFullscreenStream("1")
        manualLayout.clearFullscreenStream()
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `remove pinned streams removed from the new stream list value`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))

        manualLayout.pinStream("1")
        streamsFlow.value = listOf(StreamUi("2", "stream2"))
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("2", StreamUi("2", "stream2"))), streamItems)
    }

    @Test
    fun `clean the fullscreen stream when removed from the stream list value`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))

        manualLayout.setFullscreenStream("1")
        streamsFlow.value = listOf(StreamUi("2", "stream2"))
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(listOf(StreamItem.Stream("2", StreamUi("2", "stream2"))), streamItems)
    }

    @Test
    fun `fullscreen stream has higher importance over featured streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        val streamItems1 = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured.Pinned)),
            streamItems1
        )

        manualLayout.setFullscreenStream("2")
        val streamItems2 = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(StreamItem.Stream("2", StreamUi("2", "stream2"), state = StreamItemState.Featured.Fullscreen)),
            streamItems2
        )
    }

    @Test
    fun `featured streams have higher importance over mosaic streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        maxPinnedStreamsFlow.value = 1

        manualLayout.pinStream("1")
        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured.Pinned)),
            streamItems
        )
    }

    @Test
    fun `mosaic streams are set by default`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))

        val streamItems = manualLayout.streamItems.value
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }
}
