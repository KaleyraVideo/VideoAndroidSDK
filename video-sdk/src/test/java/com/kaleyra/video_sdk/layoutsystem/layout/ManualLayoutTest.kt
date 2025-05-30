@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.layoutsystem.layout

import android.net.Uri
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.layoutsystem.itemsprovider.FeaturedStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.itemsprovider.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ManualLayoutImplTest {

    private val streamsFlow: MutableStateFlow<List<StreamUi>> = MutableStateFlow(listOf())

    private val layoutConstraintsFlow = MutableStateFlow(StreamLayoutConstraints())

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var testScope: CoroutineScope

    private lateinit var manualLayout: ManualLayoutImpl

    private lateinit var mosaicStreamItemsProviderMock: MosaicStreamItemsProvider

    private lateinit var featuredStreamItemsProviderMock: FeaturedStreamItemsProvider
    
    private val uriMock = mockk<Uri>()

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(UnconfinedTestDispatcher())
        mosaicStreamItemsProviderMock = object : MosaicStreamItemsProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                maxStreams: Int
            ): List<StreamItem> {
                return streams.take(maxStreams).map { stream ->
                    StreamItem.Stream(stream.id, stream)
                }
            }
        }
        featuredStreamItemsProviderMock = object : FeaturedStreamItemsProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                featuredStreamIds: List<String>,
                maxNonFeaturedStreams: Int,
                featuredStreamItemState: StreamItemState.Featured,
            ): List<StreamItem> {
                return featuredStreamIds.filter { id -> id in streams.map { it.id } }.map {
                    StreamItem.Stream(it, StreamUi(it, UserInfo("userId$it", "stream$it", ImmutableUri(uriMock))), state = featuredStreamItemState)
                } + streams.filter { it.id !in featuredStreamIds }.take(maxNonFeaturedStreams).map {
                    StreamItem.Stream(it.id, StreamUi(it.id, UserInfo("userId${it.id}", "stream${it.id}", ImmutableUri(uriMock))))
                }
            }
        }

        manualLayout = ManualLayoutImpl(
            streamsFlow,
            layoutConstraints = layoutConstraintsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProviderMock,
            featuredStreamItemsProvider = featuredStreamItemsProviderMock,
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        layoutConstraintsFlow.value= StreamLayoutConstraints()
    }

    @Test
    fun `streamItems is an empty list when streams is empty`() = runTest(testDispatcher) {
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `pinStream add the pinned stream as the last in the pinned streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 2) }

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2")
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned)
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream add the pinned stream as first in the pinned streams list when prepend is true`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 2) }

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2", prepend = true)
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream(id = "2", stream = StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream(id = "1", stream = StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned),
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream when the maxPinnedStream limit is reached`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream(id = "1", stream = StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned)
            ),
            streamItems
        )
    }

    @Test
    fun `pinStream add the pinned stream even if the pin limit is reached when force is true`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("2", force = true)
        Assert.assertTrue(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(listOf(StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))), StreamItemState.Featured.Pinned)), streamItems)
    }

    @Test
    fun `pinStream don't add the pinned stream when maxPinnedStreams is less than 0`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = -1) }

        manualLayout.pinStream("1")

        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            emptyList<StreamItem>(),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream if the maxPinnedStream is zero`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 0) }

        val result = manualLayout.pinStream("1")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            emptyList<StreamItem>(),
            streamItems
        )
    }

    @Test
    fun `pinStream don't add the pinned stream if the stream is already pinned`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        val result = manualLayout.pinStream("1")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(listOf(StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamItemState.Featured.Pinned)), streamItems)
    }

    @Test
    fun `pinStream don't pin the stream when the id is not found in the streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        val result = manualLayout.pinStream("2")
        Assert.assertFalse(result)
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `unpinStream removes the stream from the pinned streams list`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        manualLayout.unpinStream("1")
        val streamItems2 = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems2)
    }

    @Test
    fun `clearPinnedStreams removes all the pinned streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 2) }

        manualLayout.pinStream("1")
        manualLayout.pinStream("2")

        manualLayout.clearPinnedStreams()
        val streamItems2 = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems2)
    }

    @Test
    fun `setFullscreenStream set the stream items list to only the fullscreen one`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))

        manualLayout.setFullscreenStream("1")
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(listOf<StreamItem>(StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamItemState.Featured.Fullscreen)), streamItems)
    }

    @Test
    fun `clearFullscreenStream remove the fullscreen stream item`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))

        manualLayout.setFullscreenStream("1")
        manualLayout.clearFullscreenStream()
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `remove pinned streams removed from the new stream list value`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))

        manualLayout.pinStream("1")
        streamsFlow.value = listOf(StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `clean the fullscreen stream when removed from the stream list value`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))

        manualLayout.setFullscreenStream("1")
        streamsFlow.value = listOf(StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `fullscreen stream has higher importance over pinned streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        val streamItems1 = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned)),
            streamItems1
        )

        manualLayout.setFullscreenStream("2")
        val streamItems2 = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))), state = StreamItemState.Featured.Fullscreen)),
            streamItems2
        )
    }

    @Test
    fun `featured streams have higher importance over mosaic streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1) }

        manualLayout.pinStream("1")
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned)),
            streamItems
        )
    }

    @Test
    fun `mosaic streams are set by default`() = runTest(testDispatcher) {
        layoutConstraintsFlow.update { it.copy(mosaicStreamThreshold = 2) }
        streamsFlow.value = listOf(StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))

        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock)))),
                StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
            ),
            streamItems
        )
    }

    @Test
    fun `mosaic stream sizes are constrained by the maximum number of mosaic streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "stream3", ImmutableUri(uriMock))),
            StreamUi("4", UserInfo("userId4", "stream4", ImmutableUri(uriMock)))
        )
        layoutConstraintsFlow.update { it.copy(mosaicStreamThreshold = 2) }

        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock)))),
                StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))))
            ),
            streamItems
        )
    }

    @Test
    fun `thumbnail stream sizes are constrained by the maximum number of thumbnail streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))),
            StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock))),
            StreamUi("3", UserInfo("userId3", "stream3", ImmutableUri(uriMock))),
            StreamUi("4", UserInfo("userId4", "stream4", ImmutableUri(uriMock)))
        )
        layoutConstraintsFlow.update { it.copy(featuredStreamThreshold = 1, thumbnailStreamThreshold = 2) }

        manualLayout.pinStream("1")
        val streamItems = manualLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", UserInfo("userId1", "stream1", ImmutableUri(uriMock))), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream("2", StreamUi("2", UserInfo("userId2", "stream2", ImmutableUri(uriMock)))),
                StreamItem.Stream("3", StreamUi("3", UserInfo("userId3", "stream3", ImmutableUri(uriMock))))
            ),
            streamItems
        )
    }
}
