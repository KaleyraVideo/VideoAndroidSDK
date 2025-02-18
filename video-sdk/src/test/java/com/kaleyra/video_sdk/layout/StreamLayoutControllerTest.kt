package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.AutoLayoutImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutControllerImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutSettings
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

@OptIn(ExperimentalCoroutinesApi::class)
class StreamLayoutControllerTest {

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
        every { anyConstructed<AutoLayoutImpl>().streamItems } returns MutableStateFlow(autoLayoutStreamItems)
        every { anyConstructed<ManualLayoutImpl>().streamItems } returns MutableStateFlow(manualLayoutStreamItems)
        layoutController = StreamLayoutControllerImpl.getInstance(
            callUserMessageProvider = callUserMessageProviderMock,
            coroutineScope = testScope
        )
    }

    @After
    fun tearDown() {
        // Reset singleton instance
        val instance: Field = StreamLayoutControllerImpl::class.java.getDeclaredField("instance")
        instance.isAccessible = true
        instance.set(null, null)
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
    fun `applyStreams updates layout controller's streams`() = runTest(testDispatcher) {
        val streams = listOf(
            StreamUi("1", "user1"),
            StreamUi("2", "user2"),
            StreamUi("3", "user3"),
        )
        layoutController.applyStreams(streams)
        assertEquals(streams, layoutController.layoutStreams.value)
    }

    @Test
    fun `applyConstraints updates layout controller's constraints`() = runTest(testDispatcher) {
        val constraints = StreamLayoutConstraints(4, 5, 6)
        layoutController.applyConstraints(constraints)
        assertEquals(constraints, layoutController.layoutConstraints.value)
    }

    @Test
    fun `applySettings updates layout controller's settings`() = runTest(testDispatcher) {
        val settings = StreamLayoutSettings(isGroupCall = true, defaultCameraIsBack = true)
        layoutController.applySettings(settings)
        assertEquals(settings, layoutController.layoutSettings.value)
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
    fun `isPinnedStreamLimitReached is false when pinned streams items are less than featuredStreamThreshold`() = runTest(testDispatcher) {
        every { anyConstructed<ManualLayoutImpl>().streamItems } returns MutableStateFlow(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "user1"), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream("2", StreamUi("2", "user2")),
            )
        )
        layoutController.switchToManualMode()
        layoutController.applyConstraints(StreamLayoutConstraints(featuredStreamThreshold = 2))
        assertEquals(false, layoutController.isPinnedStreamLimitReached.first())
    }

    @Test
    fun `isPinnedStreamLimitReached is true when pinned streams items are equals or more than featuredStreamThreshold`() = runTest(testDispatcher) {
        every { anyConstructed<ManualLayoutImpl>().streamItems } returns MutableStateFlow(
            listOf(
                StreamItem.Stream("1", StreamUi("1", "user1"), state = StreamItemState.Featured.Pinned),
                StreamItem.Stream("2", StreamUi("2", "user2"), state = StreamItemState.Featured.Pinned),
            )
        )
        layoutController.switchToManualMode()
        layoutController.applyConstraints(StreamLayoutConstraints(featuredStreamThreshold = 1))
        assertEquals(true, layoutController.isPinnedStreamLimitReached.first())
    }

    @Test
    fun `switchToAutoMode updates streamItems to the auto layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }
        layoutController.switchToAutoMode()
        assertEquals(autoLayoutStreamItems, streamItemsValues.last())
        verify(exactly = 1) { anyConstructed<ManualLayoutImpl>().clearPinnedStreams() }
    }

    @Test
    fun `switchToManualMode updates streamItems to the manual layout ones`() = runTest(testDispatcher) {
        val streamItemsValues = mutableListOf<List<StreamItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            layoutController.streamItems.toList(streamItemsValues)
        }

        layoutController.switchToManualMode()
        assertEquals(manualLayoutStreamItems, streamItemsValues.last())
        verify(exactly = 1) { anyConstructed<ManualLayoutImpl>().clearPinnedStreams() }
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
            anyConstructed<ManualLayoutImpl>().pinStream("streamId", prepend = true)
        }
    }

    @Test
    fun `pinStream with prepend false`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", prepend = false)
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().pinStream("streamId", prepend = false)
        }
    }

    @Test
    fun `pinStream with force true`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", force = true)
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().pinStream("streamId", force = true)
        }
    }

    @Test
    fun `pinStream with force false`() = runTest(testDispatcher) {
        layoutController.pinStream("streamId", force = false)
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().pinStream("streamId", force = false)
        }
    }

    @Test
    fun `unpinStream invokes manual layout unpinStream method`() = runTest(testDispatcher) {
        layoutController.unpinStream("streamId")
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().unpinStream("streamId")
        }
    }

    @Test
    fun `clearPinnedStreams invokes manual layout clearPinnedStreams method`() = runTest(testDispatcher) {
        layoutController.clearPinnedStreams()
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().clearPinnedStreams()
        }
    }

    @Test
    fun `setFullscreenStream invokes manual layout setFullscreenStream method`() = runTest(testDispatcher) {
        layoutController.setFullscreenStream("streamId")
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().setFullscreenStream("streamId")
        }
    }

    @Test
    fun `clearFullscreenStream invokes manual layout clearFullscreenStream method`() = runTest(testDispatcher) {
        layoutController.clearFullscreenStream()
        verify(exactly = 1) {
            anyConstructed<ManualLayoutImpl>().clearFullscreenStream()
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

        layoutController.applyStreams(
            listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        )
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }

    @Test
    fun `pin screen share user message is not triggered when there is a camera stream and the manual layout is active`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()

        layoutController.applyStreams(
            listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1")))
        )
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is not triggered when the auto layout is active`() = runTest(testDispatcher) {
        layoutController.switchToAutoMode()

        layoutController.applyStreams(
            listOf(StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)))
        )
        verify(exactly = 0) {
            callUserMessageProviderMock.sendUserMessage(any())
        }
    }

    @Test
    fun `pin screen share user message is triggered when a video-less stream transitions to a screen share video`() = runTest(testDispatcher) {
        layoutController.switchToManualMode()

        layoutController.applyStreams(listOf(StreamUi("1", username = "user1")))
        layoutController.applyStreams(
            listOf(
                StreamUi("1", username = "user1", video = VideoUi(id = "1", isScreenShare = true)),
            )
        )
        verify(exactly = 1) {
            callUserMessageProviderMock.sendUserMessage(PinScreenshareMessage("1", "user1"))
        }
    }
}