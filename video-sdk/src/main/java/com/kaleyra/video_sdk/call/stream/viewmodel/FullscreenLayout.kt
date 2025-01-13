package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal interface MaximizedStreamLayout: StreamLayout

internal class MaximizedStreamLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    coroutineScope: CoroutineScope
): MaximizedStreamLayout {

    private val maximizedStreamId = MutableStateFlow<String?>(null)

    private val _streamsPresentation: MutableStateFlow<List<StreamPresentation>> = MutableStateFlow(emptyList())

    override val streamsPresentation: StateFlow<List<StreamPresentation>> = _streamsPresentation

    init {
        combine(
            streams,
            maximizedStreamId
        ) { streams, maximizedStreamId ->
            streams.find { it.id == maximizedStreamId }?.let { listOf(StreamPresentation(it)) } ?: emptyList()
        }
            .distinctUntilChanged()
            .onEach { presentation -> _streamsPresentation.value = presentation }
            .launchIn(coroutineScope)
    }

    fun maximize(streamId: String) {
        maximizedStreamId.value = streamId
    }

    fun clear() {
        maximizedStreamId.value = null
    }
}