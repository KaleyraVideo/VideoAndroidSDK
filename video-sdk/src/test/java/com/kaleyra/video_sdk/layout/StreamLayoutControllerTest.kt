package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.AutoLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.FullscreenStreamItemProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutControllerImpl
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.EqMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamLayoutControllerTest {

    private val streamsFlow: MutableStateFlow<List<StreamUi>> = MutableStateFlow(emptyList())

    private val maxMosaicStreamsFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    private val maxPinnedStreamsFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    private val maxThumbnailStreamsFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    private val isOneToOneCall: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val autoLayoutStreamItems = listOf(
        StreamItem.Stream("1", StreamUi("1", "stream1")),
        StreamItem.Stream("2", StreamUi("2", "stream2")),
    )

    private val manualLayoutStreamItems = listOf(
        StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured),
        StreamItem.Stream("2", StreamUi("2", "stream2"), state = StreamItemState.Thumbnail),
    )

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var testScope: CoroutineScope

    private lateinit var layoutController: StreamLayoutController

    private lateinit var mosaicStreamItemsProviderMock: MosaicStreamItemsProvider

    private lateinit var featuredStreamItemsProviderMock: FeaturedStreamItemsProvider

    private lateinit var fullscreenStreamItemProviderMock: FullscreenStreamItemProvider

    private lateinit var callUserMessageProviderMock: CallUserMessagesProvider

    @Before
    fun setUp() {
        mockkConstructor(AutoLayoutImpl::class)
        mockkConstructor(ManualLayoutImpl::class)
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
                maxThumbnailStreams: Int,
                featuredStreamItemState: StreamItemState.Featured,
            ): List<StreamItem> {
                return featuredStreamIds.filter { id -> id in streams.map { it.id } }.map {
                    StreamItem.Stream(it, StreamUi(it, "stream$it"), state = featuredStreamItemState)
                } + streams.filter { it.id !in featuredStreamIds }.take(maxThumbnailStreams).map {
                    StreamItem.Stream(it.id, StreamUi(it.id, "stream${it.id}"), state = StreamItemState.Thumbnail)
                }
            }
        }
        fullscreenStreamItemProviderMock = object : FullscreenStreamItemProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                fullscreenStreamId: String,
            ): List<StreamItem> {
                return streams.firstOrNull { it.id == fullscreenStreamId }?.let { stream ->
                    listOf(
                        StreamItem.Stream(
                            stream.id,
                            stream,
                            state = StreamItemState.Featured.Fullscreen
                        )
                    )
                } ?: emptyList()
            }
        }
        callUserMessageProviderMock = mockk(relaxed = true)
        every {
            constructedWith<AutoLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(isOneToOneCall),
                EqMatcher(false),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(testScope),
            ).streamItems
        } returns MutableStateFlow(autoLayoutStreamItems)
        every {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).streamItems
        } returns MutableStateFlow(manualLayoutStreamItems)
        layoutController = spyk(
            StreamLayoutControllerImpl(
                streams = streamsFlow,
                maxPinnedStreams = maxPinnedStreamsFlow,
                maxMosaicStreams = maxMosaicStreamsFlow,
                maxThumbnailStreams = maxThumbnailStreamsFlow,
                isOneToOneCall = isOneToOneCall,
                isDefaultBackCamera = false,
                mosaicStreamItemsProvider = mosaicStreamItemsProviderMock,
                featuredStreamItemsProvider = featuredStreamItemsProviderMock,
                fullscreenStreamItemProvider = fullscreenStreamItemProviderMock,
                callUserMessageProvider = callUserMessageProviderMock,
                coroutineScope = testScope
            ),
            recordPrivateCalls = true
        )
    }

    @Test
    fun `streamItems are autoLayout streamItems by default`() = runTest(testDispatcher) {
        assertEquals(autoLayoutStreamItems, layoutController.streamItems.first())
    }

    @Test
    fun `switchToAutoLayout updates streamItems to the auto layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }
        layoutController.switchToAutoLayout()
        assertEquals(autoLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `switchToManualLayout updates streamItems to the manual layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.switchToManualLayout()
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).clearPinnedStreams()
        }
    }

    @Test
    fun `pinStream updates streamItems to the manual layout ones`() = runTest(testDispatcher) {
     val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.pinStream("streamId")
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `setFullscreenStream updates streamItems to the manual layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.setFullscreenStream("streamId")
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `pinStream with prepend true`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", prepend = true)
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).pinStream("streamId", prepend = true)
        }
    }

    @Test
    fun `pinStream with prepend false`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", prepend = false)
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).pinStream("streamId", prepend = false)
        }
    }

    @Test
    fun `pinStream with force true`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", force = true)
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).pinStream("streamId", force = true)
        }
    }

    @Test
    fun `pinStream with force false`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", force = false)
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).pinStream("streamId", force = false)
        }
    }

    @Test
    fun `unpinStream invokes manual layout unpinStream method`() = runTest(testDispatcher) {
        layoutController.unpinStream("streamId")
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).unpinStream("streamId")
        }
    }

    @Test
    fun `clearPinnedStreams invokes manual layout clearPinnedStreams method`() = runTest(testDispatcher) {
        layoutController.clearPinnedStreams()
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).clearPinnedStreams()
        }
    }

    @Test
    fun `setFullscreenStream invokes manual layout setFullscreenStream method`() = runTest(testDispatcher) {
        layoutController.setFullscreenStream("streamId")
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).setFullscreenStream("streamId")
        }
    }

    @Test
    fun `clearFullscreenStream invokes manual layout clearFullscreenStream method`() = runTest(testDispatcher) {
        layoutController.clearFullscreenStream()
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(maxPinnedStreamsFlow),
                EqMatcher(maxMosaicStreamsFlow),
                EqMatcher(maxThumbnailStreamsFlow),
                EqMatcher(mosaicStreamItemsProviderMock),
                EqMatcher(featuredStreamItemsProviderMock),
                EqMatcher(fullscreenStreamItemProviderMock),
                EqMatcher(testScope),
            ).clearFullscreenStream()
        }
    }

    @Test
    fun `when clearFullscreenStream is called, the layout remains the manual layout if it was active before entering fullscreen mode`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.switchToManualLayout()
        layoutController.setFullscreenStream("stream1")
        layoutController.clearFullscreenStream()
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `when clearFullscreenStream is called, the layout reverts to auto layout if it was active before entering fullscreen mode`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.switchToAutoLayout()
        layoutController.setFullscreenStream("stream1")
        layoutController.clearFullscreenStream()
        assertEquals(autoLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `pin screen share user message is triggered when there is a screen share stream and the manual layout is active`() = runTest(testDispatcher) {
        layoutController.switchToManualLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is a camera stream and the manual layout is active`() = runTest(testDispatcher) {
        layoutController.switchToManualLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1")))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is only one screen share stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is a camera stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1")))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is triggered when there is a new screen share stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        streamsFlow.value = listOf(
            StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)),
            StreamUi("2", username = "user2", video = VideoUi(id = "2", isScreenShare = true)),
        )
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("2", "user2"))
        }
    }

    @Test
    fun `pin screen share user message is triggered when a video-less stream transitions to a screen share video`() = runTest(testDispatcher) {
        layoutController.switchToManualLayout()

        streamsFlow.value = listOf(StreamUi("1", username = "user1"))
        streamsFlow.value = listOf(
            StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)),
        )
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }
}