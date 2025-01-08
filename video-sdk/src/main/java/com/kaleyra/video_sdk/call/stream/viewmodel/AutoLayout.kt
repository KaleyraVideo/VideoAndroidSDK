package com.kaleyra.video_sdk.call.stream.viewmodel

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface StreamLayout {

    val streams: StateFlow<List<StreamUi>>

    val streamsPresentation: StateFlow<List<StreamPresentation>>
}

internal interface AutoLayout : StreamLayout {

    val callUserMessageProvider: CallUserMessagesProvider

    val isOto: StateFlow<Boolean>

    val maxFeaturedStreams: StateFlow<Int>

    val isDefaultBackCamera: Boolean
}

@Immutable
data class StreamPresentation(
    val stream: StreamUi,
    val isPromoted: Boolean = false,
)

class AutoLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val isOto: StateFlow<Boolean>,
    override val maxFeaturedStreams: StateFlow<Int>,
    override val isDefaultBackCamera: Boolean,
    override val callUserMessageProvider: CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : AutoLayout {

    /**
     * Represents the internal state of the AutoLayout.
     *
     * @property allStreams All streams received.
     * @property displayedStreams Streams currently displayed.
     * @property isOneToOneMode Indicates if the layout is in one-to-one mode.
     * @property maxFeaturedStreams Maximum number of featured streams.
     */
    private data class LayoutState(
        val allStreams: List<StreamUi> = emptyList(),
        val displayedStreams: List<StreamPresentation> = emptyList(),
        val isOneToOneMode: Boolean = true,
        val maxFeaturedStreams: Int = 0
    )

    private val _internalState: MutableStateFlow<LayoutState> = MutableStateFlow(LayoutState())

    private val _streamsPresentation: MutableStateFlow<List<StreamPresentation>> = MutableStateFlow(emptyList())

    override val streamsPresentation: StateFlow<List<StreamPresentation>> = _streamsPresentation

    init {
        combine(
            streams,
            isOto,
            maxFeaturedStreams
        ) { streams, isOto, maxFeaturedStreams ->
            _internalState.update { currentState ->
                val newDisplayedStreams = determineDisplayedStreams(currentState, streams, isOto, maxFeaturedStreams, callUserMessageProvider)
                currentState.copy(
                    allStreams = streams,
                    displayedStreams = newDisplayedStreams,
                    isOneToOneMode = isOto,
                    maxFeaturedStreams = maxFeaturedStreams
                )
            }
        }.launchIn(coroutineScope)

        _internalState
            .map { it.displayedStreams }
            .distinctUntilChanged()
            .onEach { _streamsPresentation.value = it }
            .launchIn(coroutineScope)
    }

    /**
     * Determines the list of streams to be displayed in the UI and their presentation order.
     *
     * This function decides how to arrange and present a list of streams based on various factors,
     * including whether the call is in one-to-one mode, the presence of remote screen shares,
     * and the current state of the layout. It also handles the logic for requesting to pin
     * new remote screen shares.
     *
     * @param currentState The current state of the layout, including the currently displayed streams.
     * @param streams The list of available streams to be displayed.
     * @param isOto `true` if the call is in one-to-one mode, `false` otherwise.
     * @param maxFeaturedStreams The maximum number of streams that can be displayed in a featured manner.
     * @param callUserMessageProvider A provider for sending messages to the user or other parts of the application.
     * @return A list of [StreamPresentation] objects representing the streams to be displayed and their order.
     *
     * The function follows these steps:
     * 1. Counts the number of remote screen shares in the provided `streams`.
     * 2. Determines if streams should be prioritized based on the following conditions:
     *    - The call is in one-to-one mode (`isOto`).
     *    - There is exactly one remote screen share.
     *    - There are no remote screen shares, and the default camera is the back camera.
     * 3. If streams should be prioritized:
     *    - Calls `arrangeStreamsByPriority` to arrange the streams in a prioritized order.
     * 4. If streams should not be prioritized:
     *    - Attempts to find a new remote screen share that has not been displayed before using `findNewRemoteScreenShares`.
     *    - If a new screen share is found:
     *      - Sends a message to the user via `callUserMessageProvider` to request pinning the new screen share.
     *      - Calls `arrangeStreamsByPriority` again, passing the ID of the currently promoted stream (if any) to potentially maintain its prominence.
     *    - If no new screen share is found:
     *      - Calls `arrangeStreamsInGrid` to arrange the streams in a grid layout, limited by `maxFeaturedStreams`.
     */
    private fun determineDisplayedStreams(
        currentState: LayoutState,
        streams: List<StreamUi>,
        isOto: Boolean,
        maxFeaturedStreams: Int,
        callUserMessageProvider: CallUserMessagesProvider
    ): List<StreamPresentation> {
        val remoteScreenShareCount = streams.count { it.isRemoteScreenShare() }
        val shouldPrioritize = isOto || remoteScreenShareCount == 1 || (remoteScreenShareCount == 0 && isDefaultBackCamera)
        return if (shouldPrioritize) {
            arrangeStreamsByPriority(streams)
        } else {
            val screenShareToRequestPin = findNewRemoteScreenShares(currentState, streams).firstOrNull()
            if (screenShareToRequestPin != null) {
                callUserMessageProvider.sendUserMessage(PinScreenshareMessage(screenShareToRequestPin.id, screenShareToRequestPin.username))
                arrangeStreamsByPriority(
                    streams,
                    currentState.displayedStreams.find { it.isPromoted }?.stream?.id
                )
            } else arrangeStreamsInGrid(streams, maxFeaturedStreams)
        }
    }

    /**
     * Arranges streams based on a priority order.
     *
     * @param streams The list of streams to arrange.
     * @param priorityStreamId The ID of the stream to prioritize (if any).
     * @return A list of StreamPresentation objects in the desired order.
     */
    private fun arrangeStreamsByPriority(streams: List<StreamUi>, priorityStreamId: String? = null): List<StreamPresentation> {
        return streams
            .sortedWith(
                compareByDescending<StreamUi> { it.id == priorityStreamId }
                    .thenByDescending { it.isRemoteScreenShare() }
                    .thenByDescending { it.isMyCameraStream() && isDefaultBackCamera }
                    .thenByDescending { it.isRemoteCameraStream() }
            )
            .mapIndexed { index, stream -> StreamPresentation(stream, isPromoted = index == 0) }
    }

    /**
     * Arranges streams in a grid layout, taking only the specified maximum number of featured streams.
     *
     * @param streams The list of streams to arrange.
     * @param maxFeaturedStreams The maximum number of streams to feature.
     * @return A list of StreamPresentation objects in the grid layout.
     */
    private fun arrangeStreamsInGrid(streams: List<StreamUi>, maxFeaturedStreams: Int): List<StreamPresentation> {
        return streams.take(maxFeaturedStreams).map { StreamPresentation(it) }
    }

    /**
     * Finds new remote screen shares that have not been displayed before.
     *
     * @param currentState The current state of the pin layout.
     * @param newStreams The new list of available streams.
     * @return A list of new remote screen shares.
     */
    private fun findNewRemoteScreenShares(currentState: LayoutState, newStreams: List<StreamUi>): List<StreamUi> {
        val displayedStreamIds = currentState.allStreams.map { it.id }
        return newStreams.filter { isNewRemoteScreenShare(it, displayedStreamIds) }
    }

    /**
     * Determines if a stream is a new remote screen share.
     *
     * @param stream The stream to check.
     * @param presentedStreamIds The IDs of streams that have already been presented.
     * @return True if the stream is a new remote screen share, false otherwise.
     */
    private fun isNewRemoteScreenShare(
        stream: StreamUi,
        presentedStreamIds: List<String>,
    ): Boolean = stream.isRemoteScreenShare() && stream.id !in presentedStreamIds
}

