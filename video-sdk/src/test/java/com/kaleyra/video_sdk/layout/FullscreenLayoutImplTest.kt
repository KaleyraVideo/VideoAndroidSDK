package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.FullscreenLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItem
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FullscreenLayoutImplTest {

    @Test
    fun `initial streamItems is empty`() = runTest {
        val streams = MutableStateFlow<List<StreamUi>>(emptyList())
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        val initialStreamItems = fullscreenLayout.streamItems.first()
        assertTrue(initialStreamItems.isEmpty())
    }

    @Test
    fun `setFullscreenStream updates streamItems`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val stream2 = StreamUi("2", "Stream 2")
        val streams = MutableStateFlow(listOf(stream1, stream2))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        fullscreenLayout.setFullscreenStream("1")

        val streamItems = fullscreenLayout.streamItems.first()
        assertEquals(1, streamItems.size)
        val streamItem = streamItems.first() as StreamItem.Stream
        assertEquals("1", streamItem.id)
        assertEquals(stream1, streamItem.stream)
        assertEquals(StreamItemState.FULLSCREEN, streamItem.state)
    }

    @Test
    fun `clearFullscreenStream updates streamItems to empty`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val streams = MutableStateFlow(listOf(stream1))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        fullscreenLayout.setFullscreenStream("1")
        assertEquals(1, fullscreenLayout.streamItems.first().size)

        fullscreenLayout.clearFullscreenStream()

        val streamItems = fullscreenLayout.streamItems.first()
        assertTrue(streamItems.isEmpty())
    }

    @Test
    fun `streamItems only emits when there is a change`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val streams = MutableStateFlow(listOf(stream1))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        val collectedItems = mutableListOf<List<StreamItem>>()
        val job = launch {
            fullscreenLayout.streamItems.toList(collectedItems)
        }

        fullscreenLayout.setFullscreenStream("1")

        fullscreenLayout.setFullscreenStream("1")

        fullscreenLayout.clearFullscreenStream()

        fullscreenLayout.clearFullscreenStream()

        assertEquals(3, collectedItems.size)
        assertTrue(collectedItems[0].isEmpty())
        assertEquals(1, collectedItems[1].size)
        assertTrue(collectedItems[2].isEmpty())
        job.cancel()
    }

    @Test
    fun `streamItems updates on fullscreen stream removed`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val stream2 = StreamUi("2", "Stream 2")
        val streams = MutableStateFlow(listOf(stream1))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        fullscreenLayout.setFullscreenStream("1")

        streams.value = listOf(stream2)

        val streamItems = fullscreenLayout.streamItems.first()
        assertEquals(0, streamItems.size)
    }

    @Test
    fun `fullscreen stream still set after streams update`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val stream2 = StreamUi("2", "Stream 2")
        val streams = MutableStateFlow(listOf(stream1))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        fullscreenLayout.setFullscreenStream("1")

        streams.value = listOf(stream1, stream2)

        val streamItems = fullscreenLayout.streamItems.first()
        assertEquals(1, streamItems.size)
        val streamItem = streamItems.first() as StreamItem.Stream
        assertEquals("1", streamItem.id)
        assertEquals(stream1, streamItem.stream)
        assertEquals(StreamItemState.FULLSCREEN, streamItem.state)
    }

    @Test
    fun `setFullscreenStream with non existing streamId`() = runTest(UnconfinedTestDispatcher()) {
        val stream1 = StreamUi("1", "Stream 1")
        val streams = MutableStateFlow(listOf(stream1))
        val fullscreenLayout = FullscreenLayoutImpl(streams, backgroundScope)

        fullscreenLayout.setFullscreenStream("2")

        val streamItems = fullscreenLayout.streamItems.first()
        assertTrue(streamItems.isEmpty())
    }
}