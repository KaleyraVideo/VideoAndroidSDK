package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.AutoLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.FeaturedStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.viewmodel.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.MosaicStreamItemsProvider
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutControllerImpl
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamLayoutSettings
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.EqMatcher
import io.mockk.OfTypeMatcher
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

    private val layoutConstraintsFlow = MutableStateFlow(StreamLayoutConstraints())

    private val layoutSettingsFlow = MutableStateFlow(StreamLayoutSettings())

    private val autoLayoutStreamItems = listOf(
        StreamItem.Stream("1", StreamUi("1", "stream1")),
        StreamItem.Stream("2", StreamUi("2", "stream2")),
    )

    private val manualLayoutStreamItems = listOf(
        StreamItem.Stream("1", StreamUi("1", "stream1"), state = StreamItemState.Featured),
        StreamItem.Stream("2", StreamUi("2", "stream2")),
    )

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var testScope: CoroutineScope

    private lateinit var layoutController: StreamLayoutController

    private lateinit var callUserMessageProviderMock: CallUserMessagesProvider

    @Before
    fun setUp() {
        mockkConstructor(AutoLayoutImpl::class)
        mockkConstructor(ManualLayoutImpl::class)
        testDispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(UnconfinedTestDispatcher())
        callUserMessageProviderMock = mockk(relaxed = true)
        every {
            constructedWith<AutoLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(layoutConstraintsFlow),
                EqMatcher(layoutSettingsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
                EqMatcher(testScope),
            ).streamItems
        } returns MutableStateFlow(autoLayoutStreamItems)
        every {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
                EqMatcher(testScope),
            ).streamItems
        } returns MutableStateFlow(manualLayoutStreamItems)
        layoutController = spyk(
            StreamLayoutControllerImpl(
                layoutStreams = streamsFlow,
                layoutConstraints = layoutConstraintsFlow,
                layoutSettings = layoutSettingsFlow,
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
    fun `isInAutoMode is true by default`() = runTest(testDispatcher) {
        assertEquals(true, layoutController.isInAutoMode.first())
    }

    @Test
    fun `isInAutoMode is false after switching to manual layout`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()
        assertEquals(false, layoutController.isInAutoMode.first())
    }

    @Test
    fun `isInAutoMode is true after switching to auto layout`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()
        layoutController.switchToAutoMode()
        assertEquals(true, layoutController.isInAutoMode.first())
    }

    @Test
    fun `switchToAutoMode updates streamItems to the auto layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }
        layoutController.switchToAutoMode()
        assertEquals(autoLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `switchToManualMode updates streamItems to the manual layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.switchToManualMode()
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
        verify(exactly = 1) {
            constructedWith<ManualLayoutImpl>(
                EqMatcher(streamsFlow),
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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
                EqMatcher(layoutConstraintsFlow),
                OfTypeMatcher<MosaicStreamItemsProvider>(MosaicStreamItemsProvider::class),
                OfTypeMatcher<FeaturedStreamItemsProvider>(FeaturedStreamItemsProvider::class),
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

        layoutController.switchToManualMode()
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

        layoutController.switchToAutoMode()
        layoutController.setFullscreenStream("stream1")
        layoutController.clearFullscreenStream()
        assertEquals(autoLayoutStreamItems, streamItemsValues.last())
    }

    @Test
    fun `pin screen share user message is triggered when there is a screen share stream and the manual layout is active`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is a camera stream and the manual layout is active`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1")))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is only one screen share stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoMode()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is a camera stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoMode()

        streamsFlow.value = listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1")))
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is triggered when there is a new screen share stream and the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoMode()

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
        layoutController.switchToManualMode()

        streamsFlow.value = listOf(StreamUi("1", username = "user1"))
        streamsFlow.value = listOf(
            StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)),
        )
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }
}