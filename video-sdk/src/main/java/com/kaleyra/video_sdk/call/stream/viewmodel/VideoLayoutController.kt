package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface VideoLayoutController {

    val streams: StateFlow<List<StreamUi>>

    val streamsPresentation: StateFlow<List<StreamPresentation>>

    val maxFeaturedStreams: StateFlow<Int>

    val maxAllowedPinnedStreams: StateFlow<Int>

    val callUserMessageProvider: CallUserMessagesProvider

    val isOneToOne: StateFlow<Boolean>

    val isDefaultBackCamera: Boolean

    fun pin(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpin(streamId: String)

    fun enableFullscreen(id: String)

    fun exitFullscreen()
}

class VideoLayoutControllerImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxFeaturedStreams: StateFlow<Int>,
    override val maxAllowedPinnedStreams: StateFlow<Int>,
    override val isDefaultBackCamera: Boolean,
    override val isOneToOne: StateFlow<Boolean>,
    override val callUserMessageProvider: CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : VideoLayoutController {

    private val autoLayout = AutoLayoutImpl(streams, isOneToOne , maxFeaturedStreams, isDefaultBackCamera, callUserMessageProvider, coroutineScope)

    private val gridLayout = GridLayoutImpl(streams, maxFeaturedStreams, coroutineScope)

    private val pinLayout = PinLayoutImpl(streams, maxAllowedPinnedStreams, callUserMessageProvider, coroutineScope)

    private val maximizedStreamLayout = MaximizedStreamLayoutImpl(streams, coroutineScope)

    override val streamsPresentation: StateFlow<List<StreamPresentation>> = autoLayout.streamsPresentation

    override fun pin(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        pinLayout.pin(streamId, prepend, force)
        return false
    }

    override fun unpin(streamId: String) {

    }

    override fun enableFullscreen(id: String) {
        maximizedStreamLayout.maximize(id)
    }

    override fun exitFullscreen() {
        // TODO restore previous layout
        maximizedStreamLayout.clear()
    }
}