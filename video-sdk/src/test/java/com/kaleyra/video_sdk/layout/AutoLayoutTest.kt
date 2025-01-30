package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.AutoLayout
import com.kaleyra.video_sdk.call.stream.viewmodel.AutoLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutoLayoutImplTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    private lateinit var mosaicStreamItemsProvider: MosaicStreamItemsProvider
    private lateinit var featuredStreamItemsProvider: FeaturedStreamItemsProvider

    private lateinit var autoLayout: AutoLayout

    private val streamsFlow = MutableStateFlow<List<StreamUi>>(emptyList())
    private val layoutSettingsFlow = MutableStateFlow(StreamLayoutSettings())
    private val layoutConstraintsFlow = MutableStateFlow(StreamLayoutConstraints(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE))

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(testDispatcher)
        mosaicStreamItemsProvider = object : MosaicStreamItemsProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                maxStreams: Int
            ): List<StreamItem> {
                return streams.take(maxStreams).map { stream ->
                    StreamItem.Stream(stream.id, stream)
                }
            }
        }
        featuredStreamItemsProvider = object : FeaturedStreamItemsProvider {
            override fun buildStreamItems(
                streams: List<StreamUi>,
                featuredStreamIds: List<String>,
                maxThumbnailStreams: Int,
                featuredStreamItemState: StreamItemState.Featured,
            ): List<StreamItem> {
                return featuredStreamIds.filter { id -> id in streams.map { it.id } }.map { id ->
                    val stream = streams.firstOrNull { it.id == id } ?: StreamUi("id", "stream")
                    StreamItem.Stream(id, stream, state = StreamItemState.Featured)
                } + streams.filter { stream -> stream.id !in featuredStreamIds }.take(maxThumbnailStreams).map { stream ->
                    StreamItem.Stream(stream.id, stream)
                }
            }
        }
        autoLayout = AutoLayoutImpl(
            streamsFlow,
            layoutConstraints = layoutConstraintsFlow,
            layoutSettings = layoutSettingsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProvider,
            featuredStreamItemsProvider = featuredStreamItemsProvider,
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        layoutConstraintsFlow.value = StreamLayoutConstraints(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        layoutSettingsFlow.value = StreamLayoutSettings()
    }

    @Test
    fun `streamItems is an empty list when streams is empty`() = runTest(testDispatcher) {
        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }

    @Test
    fun `streamItems are in mosaic mode when not in one to one call and no screen share`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        layoutSettingsFlow.value = StreamLayoutSettings(isGroupCall = true)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `streamItems are in featured mode when in one to one call`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(StreamUi("1", "stream1"), StreamUi("2", "stream2"))
        layoutSettingsFlow.value = StreamLayoutSettings(isGroupCall = false)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `streamItems are in featured mode when has one remote screen share`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", "stream1"),
            StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true))
        )

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("2", StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true)), state = StreamItemState.Featured),
                StreamItem.Stream("1", StreamUi("1", "stream1")),
            ),
            streamItems
        )
    }

    @Test
    fun `streamItems are in mosaic mode when two remote screen share join together`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", "stream1"),
            StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true)),
            StreamUi("3", "stream3", video = VideoUi("3", isScreenShare = true))
        )
        layoutConstraintsFlow.value = StreamLayoutConstraints(mosaicStreamThreshold = 4)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true))),
                StreamItem.Stream("3", StreamUi("3", "stream3", video = VideoUi("3", isScreenShare = true))),
            ),
            streamItems
        )
    }

    @Test
    fun `existing featured screen share item remaining featured when another screen share join the call`() = runTest(testDispatcher) {
        val stream1 = StreamUi("1", "stream1")
        val stream2 = StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true))
        val stream3 = StreamUi("3", "stream3", video = VideoUi("3", isScreenShare = true))
        streamsFlow.value = listOf(stream1, stream2)

        streamsFlow.value = listOf(stream1, stream2, stream3)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("2", stream2, state = StreamItemState.Featured),
                StreamItem.Stream("3", stream3),
                StreamItem.Stream("1", stream1),
            ),
            streamItems
        )
    }

    @Test
    fun `remote screen share is featured over local camera stream when isDefaultBackCamera is true`() = runTest(testDispatcher) {
        val screenShareStream = StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = true))
        val cameraStream = StreamUi("2", "stream2", isMine = true, video = VideoUi("2", isScreenShare = false))
        streamsFlow.value = listOf(cameraStream, screenShareStream)
        layoutSettingsFlow.value = StreamLayoutSettings(defaultCameraIsBack = true)

        autoLayout = AutoLayoutImpl(
            streamsFlow,
            layoutConstraints = layoutConstraintsFlow,
            layoutSettings = layoutSettingsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProvider,
            featuredStreamItemsProvider = featuredStreamItemsProvider,
            coroutineScope = testScope
        )

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", screenShareStream, state = StreamItemState.Featured),
                StreamItem.Stream("2", cameraStream),
            ),
            streamItems
        )
    }

    @Test
    fun `local camera stream is featured over remote camera stream when isDefaultBackCamera is true`() = runTest(testDispatcher) {
        val remoteCameraStream = StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = false))
        val cameraStream = StreamUi("2", "stream2", isMine = true, video = VideoUi("2", isScreenShare = false))
        streamsFlow.value = listOf(remoteCameraStream, cameraStream)
        layoutSettingsFlow.value = StreamLayoutSettings(isGroupCall = false, defaultCameraIsBack = true)

        autoLayout = AutoLayoutImpl(
            streamsFlow,
            layoutConstraints = layoutConstraintsFlow,
            layoutSettings = layoutSettingsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProvider,
            featuredStreamItemsProvider = featuredStreamItemsProvider,
            coroutineScope = testScope
        )

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("2", cameraStream, state = StreamItemState.Featured),
                StreamItem.Stream("1", remoteCameraStream),
            ),
            streamItems
        )
    }

    @Test
    fun `remote camera stream is featured over local camera stream when isDefaultBackCamera is false`() = runTest {
        val remoteCameraStream = StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = false))
        val cameraStream = StreamUi("2", "stream2", isMine = true, video = VideoUi("2", isScreenShare = false))
        streamsFlow.value = listOf(cameraStream, remoteCameraStream)
        layoutSettingsFlow.value = StreamLayoutSettings(isGroupCall = false)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", remoteCameraStream, state = StreamItemState.Featured),
                StreamItem.Stream("2", cameraStream),
            ),
            streamItems
        )
    }

    @Test
    fun `streamItems featured priority`() = runTest {
        val remoteScreenShare1 = StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = true))
        val remoteScreenShare2 = StreamUi("2", "stream2", video = VideoUi("2", isScreenShare = true))
        val backCamera = StreamUi("3", "stream3", isMine = true, video = VideoUi("3", isScreenShare = false))
        val remoteCamera = StreamUi("4", "stream4", video = VideoUi("4", isScreenShare = false))

        layoutSettingsFlow.value = StreamLayoutSettings(defaultCameraIsBack = true)

        autoLayout = AutoLayoutImpl(
            streamsFlow,
            layoutConstraints = layoutConstraintsFlow,
            layoutSettings = layoutSettingsFlow,
            mosaicStreamItemsProvider = mosaicStreamItemsProvider,
            featuredStreamItemsProvider = featuredStreamItemsProvider,
            coroutineScope = testScope
        )

        streamsFlow.value = listOf(remoteScreenShare1)

        val streamItems1 = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(StreamItem.Stream("1", remoteScreenShare1, state = StreamItemState.Featured)),
            streamItems1
        )

        streamsFlow.value = listOf(
            remoteCamera,
            backCamera,
            remoteScreenShare2,
            remoteScreenShare1
        )

        val streamItems2 = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", remoteScreenShare1, state = StreamItemState.Featured),
                StreamItem.Stream("2", remoteScreenShare2),
                StreamItem.Stream("3", backCamera),
                StreamItem.Stream("4", remoteCamera),
            ),
            streamItems2
        )
    }

    @Test
    fun `mosaic stream sizes are constrained by the maximum number of mosaic streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", "stream1"),
            StreamUi("2", "stream2"),
            StreamUi("3", "stream3"),
            StreamUi("4", "stream4")
        )
        layoutConstraintsFlow.value = StreamLayoutConstraints(mosaicStreamThreshold = 2)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1")),
                StreamItem.Stream("2", StreamUi("2", "stream2"))
            ),
            streamItems
        )
    }

    @Test
    fun `thumbnail stream sizes are constrained by the maximum number of thumbnail streams`() = runTest(testDispatcher) {
        streamsFlow.value = listOf(
            StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = true)),
            StreamUi("2", "stream2"),
            StreamUi("3", "stream3"),
            StreamUi("4", "stream4")
        )
        layoutConstraintsFlow.value = StreamLayoutConstraints(
            featuredStreamThreshold = 2,
            thumbnailStreamThreshold = 2
        )

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = true)), state = StreamItemState.Featured),
                StreamItem.Stream("2", StreamUi("2", "stream2")),
                StreamItem.Stream("3", StreamUi("3", "stream3"))
            ),
            streamItems
        )
    }

    @Test
    fun `featured stream is not set when maximum number of featured is zero`() = runTest(testDispatcher) {
        val remoteCameraStream = StreamUi("1", "stream1", video = VideoUi("1", isScreenShare = false))
        val cameraStream = StreamUi("2", "stream2", isMine = true, video = VideoUi("2", isScreenShare = false))
        streamsFlow.value = listOf(cameraStream, remoteCameraStream)
        layoutSettingsFlow.value = StreamLayoutSettings(isGroupCall = false)
        layoutConstraintsFlow.value = StreamLayoutConstraints(featuredStreamThreshold = 0)

        val streamItems = autoLayout.streamItems.first()
        Assert.assertEquals(emptyList<StreamItem>(), streamItems)
    }
}