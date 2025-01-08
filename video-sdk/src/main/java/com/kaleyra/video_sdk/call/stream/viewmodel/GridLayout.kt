package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface GridLayout: StreamLayout {

    val maxFeaturedStreams: StateFlow<Int>
}

internal class GridLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxFeaturedStreams: StateFlow<Int>,
    coroutineScope: CoroutineScope
) : GridLayout {

    private data class State(
        val allStreams: List<StreamUi> = emptyList(),
        val featuredStreams: List<StreamUi> = emptyList(),
        val maxFeaturedStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<State> = MutableStateFlow(State())

    private val _streamsPresentation: MutableStateFlow<List<StreamPresentation>> = MutableStateFlow(emptyList())

    override val streamsPresentation: StateFlow<List<StreamPresentation>> = _streamsPresentation

    init {
        combine(
            streams,
            maxFeaturedStreams
        ) { streams, maxFeaturedStreams ->
            _internalState.update { state ->
                when {
                    maxFeaturedStreams < 1 -> {
                        state.copy(allStreams = streams, featuredStreams = emptyList(), maxFeaturedStreams = 0)
                    }

                    state.allStreams != streams || state.maxFeaturedStreams != maxFeaturedStreams -> {
                        state.copy(
                            allStreams = streams,
                            featuredStreams = streams.take(maxFeaturedStreams),
                            maxFeaturedStreams = maxFeaturedStreams
                        )
                    }

                    else -> return@combine
                }
            }
        }.launchIn(coroutineScope)

        _internalState
            .onEach { state ->
                _streamsPresentation.value = state.featuredStreams.map { StreamPresentation(it) }
            }
            .launchIn(coroutineScope)
    }
}