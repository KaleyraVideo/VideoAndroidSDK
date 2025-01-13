package com.kaleyra.video_sdk.call.stream.viewmodel

import android.util.Log
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface FullscreenLayout : StreamLayout {

    fun setFullscreenStream(streamId: String)

    fun clearFullscreenStream()
}

internal class FullscreenLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    coroutineScope: CoroutineScope
): FullscreenLayout {

    private val _fullscreenStreamId = MutableStateFlow<String?>(null)

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamItems

    init {
        combine(
            streams,
            _fullscreenStreamId
        ) { allStreams, fullscreenStreamId ->
            val fullscreenStream = allStreams.firstOrNull { it.id == fullscreenStreamId }
            listOfNotNull(
                fullscreenStream?.let { stream ->
                    StreamItem.Stream(stream.id, stream, state = StreamItemState.FULLSCREEN)
                }
            )
        }
            .distinctUntilChanged()
            .onEach { streamItems -> _streamItems.update { streamItems } }
            .launchIn(coroutineScope)
    }

    override fun setFullscreenStream(streamId: String) {
        _fullscreenStreamId.value = streamId
    }

    override fun clearFullscreenStream() {
        _fullscreenStreamId.value = null
    }
}