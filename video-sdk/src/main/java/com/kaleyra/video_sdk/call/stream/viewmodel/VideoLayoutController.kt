package com.kaleyra.video_sdk.call.stream.viewmodel

import android.util.Log
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal interface VideoLayoutController {

    val streams: StateFlow<List<StreamUi>>

    val isOneToOneCall: StateFlow<Boolean>

    val maxFeaturedStreams: StateFlow<Int>

    val maxAllowedPinnedStreams: StateFlow<Int>

    val maxAllowedThumbnailStreams: StateFlow<Int>

    val streamItems: StateFlow<List<StreamItem>>

    val callUserMessageProvider: CallUserMessagesProvider

    val isDefaultBackCamera: Boolean

    fun enterMosaicLayout()

    fun enterAutoLayout()

    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpinStream(streamId: String)

    fun setFullscreenStream(id: String)

    fun clearFullscreenStream()
}

internal class VideoLayoutControllerImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val isOneToOneCall: StateFlow<Boolean>,
    override val maxFeaturedStreams: StateFlow<Int>,
    override val maxAllowedPinnedStreams: StateFlow<Int>,
    override val maxAllowedThumbnailStreams: StateFlow<Int>,
    override val isDefaultBackCamera: Boolean,
    // TODO implement this logic
    override val callUserMessageProvider: CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : VideoLayoutController {

    private val autoLayout: AutoLayout = AutoLayoutImpl(
        streams = streams,
        isOneToOneCall = isOneToOneCall,
        maxAllowedFeaturedStreams = maxFeaturedStreams,
        maxAllowedThumbnailStreams = maxAllowedThumbnailStreams,
        isDefaultBackCamera = isDefaultBackCamera,
        coroutineScope = coroutineScope
    )

    private val manualLayout: ManualLayout = ManualLayoutImpl(
        streams = streams,
        maxAllowedFeaturedStreams = maxFeaturedStreams,
        maxAllowedPinnedStreams = maxAllowedPinnedStreams,
        maxAllowedThumbnailStreams = maxAllowedThumbnailStreams,
        coroutineScope = coroutineScope
    )

    private data class ControllerState(
        val streamLayout: StreamLayout,
        val previousStreamLayout: StreamLayout? = null,
        val allStreamsWithVideo: List<StreamUi> = emptyList()
    )

    private val _internalState: MutableStateFlow<ControllerState> = MutableStateFlow(ControllerState(autoLayout))

    override val streamItems: StateFlow<List<StreamItem>> = _internalState
        .flatMapLatest { it.streamLayout.streamItems }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        streams
            .onEach { streams ->
                val streamsWithVideo = streams.filter { it.video != null }
                _internalState.update { state ->
                    if (state.streamLayout is ManualLayout || (state.streamLayout is AutoLayout && streams.count { it.isRemoteScreenShare() } > 1)) {
                        val screenShareToRequestPin = findNewRemoteScreenShares(
                            state,
                            streamsWithVideo
                        ).firstOrNull()
                        screenShareToRequestPin?.let {
                            callUserMessageProvider.sendUserMessage(
                                PinScreenshareMessage(it.id, it.username)
                            )
                        }
                    }
                    state.copy(allStreamsWithVideo = streamsWithVideo)
                }

            }.launchIn(coroutineScope)
    }

    override fun enterMosaicLayout() {
        manualLayout.clearPinnedStreams()
        updateInternalState(manualLayout)
    }

    override fun enterAutoLayout() {
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

    private fun findNewRemoteScreenShares(currentState: ControllerState, newStreams: List<StreamUi>): List<StreamUi> {
        val displayedStreamIds = currentState.allStreamsWithVideo.map { it.id }
        val a = newStreams.filter { isNewRemoteScreenShare(it, displayedStreamIds) }
        return a
    }

    private fun isNewRemoteScreenShare(
        stream: StreamUi,
        presentedStreamIds: List<String>,
    ): Boolean = stream.isRemoteScreenShare() && stream.id !in presentedStreamIds

    private fun updateInternalState(newLayout: StreamLayout) {
        _internalState.update { state ->
            state.copy(
                streamLayout = newLayout,
                previousStreamLayout = state.streamLayout
            )
        }
    }

}