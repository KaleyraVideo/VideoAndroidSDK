package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal interface StreamLayoutController {

    val streamItems: Flow<List<StreamItem>>

    val streams: Flow<List<StreamUi>>

    val maxPinnedStreams: Flow<Int>

    val maxMosaicStreams: Flow<Int>

    val maxThumbnailStreams: Flow<Int>

    val isOneToOneCall: Flow<Boolean>

    val isDefaultBackCamera: Boolean

    val mosaicStreamItemsProvider: MosaicStreamItemsProvider

    val featuredStreamItemsProvider: FeaturedStreamItemsProvider

    val fullscreenStreamItemProvider: FullscreenStreamItemProvider

    val callUserMessageProvider: CallUserMessagesProvider

    fun switchToManualLayout()

    fun switchToAutoLayout()

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpinStream(streamId: String)

    fun clearPinnedStreams()

    fun setFullscreenStream(id: String)

    fun clearFullscreenStream()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class StreamLayoutControllerImpl(
    override val streams: Flow<List<StreamUi>>,
    override val maxMosaicStreams: Flow<Int>,
    override val maxPinnedStreams: Flow<Int>,
    override val maxThumbnailStreams: Flow<Int>,
    override val isOneToOneCall: Flow<Boolean>,
    override val isDefaultBackCamera: Boolean,
    override val mosaicStreamItemsProvider: MosaicStreamItemsProvider = MosaicStreamItemsProviderImpl(),
    override val featuredStreamItemsProvider: FeaturedStreamItemsProvider = FeaturedStreamItemsProviderImpl(),
    override val fullscreenStreamItemProvider: FullscreenStreamItemProvider = FullscreenStreamItemProviderImpl(),
    override val callUserMessageProvider: CallUserMessagesProvider = CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : StreamLayoutController {

    private data class ControllerState(
        val streamLayout: StreamLayout,
        val previousStreamLayout: StreamLayout? = null,
        val remoteScreenShareStreams: List<StreamUi> = emptyList()
    )

    private val autoLayout: AutoLayout = AutoLayoutImpl(
        streams = streams,
        isOneToOneCall = isOneToOneCall,
        maxMosaicStreams = maxMosaicStreams,
        maxThumbnailStreams = maxThumbnailStreams,
        mosaicStreamItemsProvider = mosaicStreamItemsProvider,
        featuredStreamItemsProvider = featuredStreamItemsProvider,
        isDefaultBackCamera = isDefaultBackCamera,
        coroutineScope = coroutineScope
    )

    private val manualLayout: ManualLayout = ManualLayoutImpl(
        streams = streams,
        maxMosaicStreams = maxMosaicStreams,
        maxPinnedStreams = maxPinnedStreams,
        maxThumbnailStreams = maxThumbnailStreams,
        mosaicStreamItemsProvider = mosaicStreamItemsProvider,
        featuredStreamItemsProvider = featuredStreamItemsProvider,
        fullscreenStreamItemProvider = fullscreenStreamItemProvider,
        coroutineScope = coroutineScope
    )

    private val _internalState: MutableStateFlow<ControllerState> = MutableStateFlow(ControllerState(autoLayout))

    override val streamItems: Flow<List<StreamItem>> = _internalState.flatMapLatest { it.streamLayout.streamItems }

    init {
        streams
            .onEach { streams ->
                val remoteScreenShareStreams = streams.filter { it.isRemoteScreenShare() }
                _internalState.update { state ->
                    if (shouldSendPinScreenShareMessage(state, remoteScreenShareStreams)) {
                        val screenShareToRequestPin = findNewRemoteScreenShares(state, remoteScreenShareStreams).firstOrNull()
                        screenShareToRequestPin?.let {
                            callUserMessageProvider.sendUserMessage(PinScreenshareMessage(it.id, it.username))
                        }
                    }
                    state.copy(remoteScreenShareStreams = remoteScreenShareStreams)
                }

            }.launchIn(coroutineScope)
    }

    override fun switchToManualLayout() {
        manualLayout.clearPinnedStreams()
        updateInternalState(manualLayout)
    }

    override fun switchToAutoLayout() {
        updateInternalState(autoLayout)
    }

    override fun pinStream(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        val isPinned = manualLayout.pinStream(streamId, prepend, force)
        updateInternalState(manualLayout)
        return isPinned
    }

    override fun unpinStream(streamId: String) {
        manualLayout.unpinStream(streamId)
    }

    override fun clearPinnedStreams() {
        manualLayout.clearPinnedStreams()
    }

    override fun setFullscreenStream(id: String) {
        manualLayout.setFullscreenStream(id)
        updateInternalState(manualLayout)
    }

    override fun clearFullscreenStream() {
        manualLayout.clearFullscreenStream()
        _internalState.value.previousStreamLayout?.let { layout ->
            updateInternalState(layout)
        }
    }

    private fun shouldSendPinScreenShareMessage(state: ControllerState, remoteScreenShareStreams: List<StreamUi>): Boolean {
        return state.streamLayout is ManualLayout || (state.streamLayout is AutoLayout && remoteScreenShareStreams.size > 1)
    }

    private fun findNewRemoteScreenShares(currentState: ControllerState, remoteScreenShareStreams: List<StreamUi>): List<StreamUi> {
        val currentRemoteScreenShareIds = currentState.remoteScreenShareStreams.map { it.id }
        return remoteScreenShareStreams.filter { stream -> stream.id !in currentRemoteScreenShareIds }
    }

    private fun updateInternalState(newLayout: StreamLayout) {
        _internalState.update { state ->
            state.copy(
                streamLayout = newLayout,
                previousStreamLayout = state.streamLayout
            )
        }
    }
}