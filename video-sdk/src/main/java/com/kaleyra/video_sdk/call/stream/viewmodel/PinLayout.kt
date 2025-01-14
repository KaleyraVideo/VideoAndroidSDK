package com.kaleyra.video_sdk.call.stream.viewmodel

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.math.max

internal interface PinLayout: StreamLayout {

    val maxAllowedPinnedStreams: StateFlow<Int>

    val maxAllowedThumbnailStreams: StateFlow<Int>

    val callUserMessageProvider: CallUserMessagesProvider

    fun pin(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    fun unpin(streamId: String)

    fun clear()
}

internal class PinLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxAllowedPinnedStreams: StateFlow<Int>,
    override val maxAllowedThumbnailStreams: StateFlow<Int>,
    override val callUserMessageProvider: CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : PinLayout {

    private data class State(
        val allStreams: List<StreamUi> = emptyList(),
        val pinnedStreams: List<StreamUi> = emptyList(),
        val maxAllowedPinnedStreams: Int = 0,
        val maxAllowedThumbnailStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<State> = MutableStateFlow(State())

    private val _streamsPresentation: MutableStateFlow<List<StreamItem>> = MutableStateFlow(emptyList())

    override val streamItems: StateFlow<List<StreamItem>> = _streamsPresentation

    val hasPinnedStreams: Boolean
        get() = _internalState.value.pinnedStreams.isNotEmpty()

    init {
        combine(
            streams,
            maxAllowedPinnedStreams,
            maxAllowedThumbnailStreams
        ) { streams, maxAllowedPinnedStreams, maxAllowedThumbnailStreams ->
            _internalState.update { state ->
                val screenShareToRequestPin = findNewRemoteScreenShares(state, streams).firstOrNull()
                screenShareToRequestPin?.let { requestPinForScreenShare(it, callUserMessageProvider) }

                state.copy(
                    allStreams = streams,
                    pinnedStreams = if (maxAllowedPinnedStreams > 0) retainPinnedStreams(state, streams, maxAllowedPinnedStreams) else emptyList(),
                    maxAllowedPinnedStreams = max(0, maxAllowedPinnedStreams),
                    maxAllowedThumbnailStreams = maxAllowedThumbnailStreams,
                )
            }
        }.launchIn(coroutineScope)

        _internalState
            .onEach { _streamsPresentation.value = prepareStreamsForDisplay(it) }
            .launchIn(coroutineScope)
    }

    override fun pin(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        _internalState.update { state ->
            val maxAllowedPinnedStreams = state.maxAllowedPinnedStreams
            val pinnedStreams = state.pinnedStreams
            val stream = state.allStreams.find { it.id == streamId } ?: return false

            val canPin = canPinStream(stream, pinnedStreams, maxAllowedPinnedStreams, force)
            if (!canPin) return false

            state.copy(pinnedStreams = pinStream(stream, pinnedStreams, maxAllowedPinnedStreams, prepend))
        }
        return true
    }

    override fun unpin(streamId: String) {
        _internalState.update { state ->
            state.copy(pinnedStreams = state.pinnedStreams.filter { it.id != streamId })
        }
    }

    override fun clear() {
        _internalState.update { state ->
            state.copy(pinnedStreams = emptyList())
        }
    }

    private fun canPinStream(
        stream: StreamUi,
        pinnedStreams: List<StreamUi>,
        maxAllowedPinnedStreams: Int,
        force: Boolean,
    ): Boolean {
        if (maxAllowedPinnedStreams < 1) return false
        if (pinnedStreams.any { it.id == stream.id } || (pinnedStreams.size >= maxAllowedPinnedStreams && !force)) return false
        return true
    }

    private fun prepareStreamsForDisplay(currentState: State): List<StreamItem> = with(currentState) {
        if (pinnedStreams.isEmpty()) return emptyList()

        val nonPinnedStreams = allStreams.filter { it !in pinnedStreams }
        return if (nonPinnedStreams.size <= maxAllowedThumbnailStreams) {
            (pinnedStreams + nonPinnedStreams).map { stream ->
                StreamItem.Stream(
                    id = stream.id,
                    stream = stream,
                    state = if (stream in pinnedStreams) StreamItemState.PINNED else StreamItemState.THUMBNAIL
                )
            }
        } else {
            val (thumbnailStreams, remainingStreams) = nonPinnedStreams.withIndex().partition { it.index < maxAllowedThumbnailStreams - 1 }

            val pinnedStreamItems = pinnedStreams.map { StreamItem.Stream(it.id, it, state = StreamItemState.PINNED) }
            val thumbnailStreamItems = thumbnailStreams.map { indexedValue ->
                StreamItem.Stream(indexedValue.value.id, indexedValue.value, state = StreamItemState.THUMBNAIL)
            }
            val moreItem = StreamItem.More(
                id = remainingStreams.first().value.id,
                users = remainingStreams.map { UserPreview(it.value.username, it.value.avatar) }
            )

            pinnedStreamItems + thumbnailStreamItems + moreItem
        }
    }

    private fun retainPinnedStreams(currentState: State, newStreams: List<StreamUi>, maxAllowedPinnedStreams: Int): List<StreamUi> {
        return currentState.pinnedStreams
            .mapNotNull { stream -> newStreams.find { it.id == stream.id } }
            .take(maxAllowedPinnedStreams)
    }

    private fun findNewRemoteScreenShares(currentState: State, newStreams: List<StreamUi>): List<StreamUi> {
        val displayedStreamIds = currentState.allStreams.map { it.id }
        return newStreams.filter { isNewRemoteScreenShare(it, displayedStreamIds) }
    }

    private fun requestPinForScreenShare(screenShare: StreamUi, callUserMessageProvider: CallUserMessagesProvider) {
        callUserMessageProvider.sendUserMessage(
            PinScreenshareMessage(screenShare.id, screenShare.username)
        )
    }

    private fun pinStream(
        stream: StreamUi,
        currentPinnedStreams: List<StreamUi>,
        maxAllowedPinnedStreams: Int,
        prepend: Boolean = false,
    ): List<StreamUi> {
        return if (prepend) {
            (listOf(stream) + currentPinnedStreams).take(maxAllowedPinnedStreams)
        } else {
            (currentPinnedStreams + stream).takeLast(maxAllowedPinnedStreams)
        }
    }

    private fun isNewRemoteScreenShare(
        stream: StreamUi,
        presentedStreamIds: List<String>,
    ): Boolean = stream.isRemoteScreenShare() && stream.id !in presentedStreamIds
}