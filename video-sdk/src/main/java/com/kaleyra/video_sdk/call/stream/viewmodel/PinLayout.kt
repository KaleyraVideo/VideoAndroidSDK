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

/**
 * An interface representing a layout that supports pinning streams.
 *
 * This interface extends [StreamLayout] and provides additional functionality
 * related to managing pinned streams, such as the maximum number of allowed
 * pinned streams and a provider for call-related user messages.
 */
internal interface PinLayout: StreamLayout {

    /**
     * A [StateFlow] that emits the maximum number of streams that can be pinned at any given time.
     *
     * This value can change dynamically, and the layout should adapt accordingly.
     */
    val maxAllowedPinnedStreams: StateFlow<Int>

    /**
     * A [CallUserMessagesProvider] that provides messages to be displayed to the user
     * in the context of a call, such as notifications related to pinning or unpinning streams.
     */
    val callUserMessageProvider: CallUserMessagesProvider

    /**
     * Pins a stream with the given [streamId].
     *
     * @param streamId The ID of the stream to pin.
     * @param prepend If true, the stream is added to the beginning of the pinned list.
     * @param force If true, the stream is pinned even if the maximum number of pinned streams is reached, maintaining the max number constraint.
     * @return True if the stream was successfully pinned, false otherwise.
     */
    fun pin(streamId: String, prepend: Boolean, force: Boolean): Boolean

    /**
     * Unpins a stream with the given [streamId].
     *
     * @param streamId The ID of the stream to unpin.
     */
    fun unpin(streamId: String)

    /**
     * Unpins a stream with the given [streamId].
     *
     * @param streamId The ID of the stream to unpin.
     */
    fun clear()
}

/**
 * Implementation of the [PinLayout] interface, responsible for managing the pinning of streams.
 *
 * This class handles the logic for pinning and unpinning streams, maintaining the state of pinned streams,
 * and preparing streams for display. It interacts with the provided [StateFlow]s for streams and
 * maximum allowed pinned streams, and uses a [CallUserMessagesProvider] to send user messages.
 *
 * @property streams A [StateFlow] emitting a list of [StreamUi] representing all available streams.
 * @property maxAllowedPinnedStreams A [StateFlow] emitting the maximum number of streams that can be pinned.
 * @property callUserMessageProvider A [CallUserMessagesProvider] for sending user messages related to pinning.
 * @property coroutineScope The [CoroutineScope] in which to launch coroutines for stream updates.
 */
internal class PinLayoutImpl(
    override val streams: StateFlow<List<StreamUi>>,
    override val maxAllowedPinnedStreams: StateFlow<Int>,
    override val callUserMessageProvider: CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : PinLayout {

    /**
     * Represents the internal state of the [PinLayoutImpl].
     *
     * @property allStreams The list of all available streams.
     * @property pinnedStreams The list of currently pinned streams.
     */
    private data class State(
        val allStreams: List<StreamUi> = emptyList(),
        val pinnedStreams: List<StreamUi> = emptyList(),
        val maxAllowedPinnedStreams: Int = 0
    )

    /**
     * Internal [MutableStateFlow] holding the current [State] of the pin layout.
     */
    private val _internalState: MutableStateFlow<State> = MutableStateFlow(State())

    /**
     * Internal [MutableStateFlow] emitting the list of [StreamPresentation] for display.
     */
    private val _streamsPresentation: MutableStateFlow<List<StreamPresentation>> = MutableStateFlow(emptyList())

    /**
     * Public [StateFlow] emitting the list of [StreamPresentation] for display.
     */
    override val streamsPresentation: StateFlow<List<StreamPresentation>> = _streamsPresentation

    /**
     * Initializes stream management with two data flow pipelines:
     *
     * 1. **State Updates:** Combines `streams` and `maxAllowedPinnedStreams` to update `_internalState`, refreshing pinned streams and handling pin requests.
     * 2. **UI Updates:** Observes `_internalState` and updates `_streamsPresentation` with display-ready data.
     *
     * Pipelines run within `coroutineScope`.
     */
    init {
        combine(
            streams,
            maxAllowedPinnedStreams
        ) { streams, maxAllowedPinnedStreams ->
            _internalState.update { state ->
                when {
                    // If pinning is disabled, update allStreams and clear pinned streams.
                    maxAllowedPinnedStreams < 1 -> {
                        state.copy(allStreams = streams, pinnedStreams = emptyList(), maxAllowedPinnedStreams = 0)
                    }
                    // If pinning is enabled and either the streams or the maxAllowedPinnedStreams have changed, refresh the pinned streams.
                    state.allStreams != streams || state.maxAllowedPinnedStreams != maxAllowedPinnedStreams -> {
                        state.copy(
                            allStreams = streams,
                            pinnedStreams = refreshPinnedStreams(
                                currentState = state,
                                newStreams = streams,
                                maxAllowedPinnedStreams = maxAllowedPinnedStreams,
                                onScreenSharePinRequest = { stream ->
                                    requestPinForScreenShare(stream, callUserMessageProvider)
                                }
                            ),
                            maxAllowedPinnedStreams = maxAllowedPinnedStreams
                        )
                    }
                    // If pinning is enabled and nothing has changed, return the current state.
                    else -> return@combine
                }
            }
        }.launchIn(coroutineScope)

        _internalState
            .onEach { _streamsPresentation.value = prepareStreamsForDisplay(it.allStreams, it.pinnedStreams) }
            .launchIn(coroutineScope)
    }

    /**
     * Pins a stream with the given [streamId].
     *
     * @param streamId The ID of the stream to pin.
     * @param prepend If true, the stream is added to the beginning of the pinned list.
     * @param force If true, the stream is pinned even if the maximum number of pinned streams is reached, maintaining the max number constraint.
     * @return True if the stream was successfully pinned, false otherwise.
     */
    override fun pin(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        _internalState.update { state ->
            val maxAllowedPinnedStreams = maxAllowedPinnedStreams.value
            val pinnedStreams = state.pinnedStreams
            val stream = state.allStreams.find { it.id == streamId } ?: return false

            val canPin = canPinStream(stream, pinnedStreams, maxAllowedPinnedStreams, force)
            if (!canPin) return false

            state.copy(pinnedStreams = pinStream(stream, pinnedStreams, maxAllowedPinnedStreams, prepend))
        }
        return true
    }

    /**
     * Unpins a stream with the given [streamId].
     *
     * @param streamId The ID of the stream to unpin.
     */
    override fun unpin(streamId: String) {
        _internalState.update { state ->
            state.copy(pinnedStreams = state.pinnedStreams.filter { it.id != streamId })
        }
    }

    /**
     * Clears all pinned streams.
     */
    override fun clear() {
        _internalState.update { state ->
            state.copy(pinnedStreams = emptyList())
        }
    }

    /**
     * Determines if a stream can be pinned.
     *
     * @param stream The stream to check.
     * @param pinnedStreams The current list of pinned streams.
     * @param maxAllowedPinnedStreams The maximum number of streams that can be pinned.
     * @param force If true, the stream can be pinned even if the maximum number of pinned streams is reached.
     * @return True if the stream can be pinned, false otherwise.
     */
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

    /**
     * Prepares the streams for display by combining pinned and unpinned streams.
     *
     * @param streams The list of all available streams.
     * @param pinnedStreams The list of currently pinned streams.
     * @return A list of [StreamPresentation] ready for display.
     */
    private fun prepareStreamsForDisplay(
        streams: List<StreamUi>,
        pinnedStreams: List<StreamUi>,
    ): List<StreamPresentation> {
        val streamsToDisplay = pinnedStreams + streams.filter { it !in pinnedStreams }
        return streamsToDisplay.map { StreamPresentation(it, isPromoted = it in pinnedStreams) }
    }

    /**
     * Refreshes the list of pinned streams based on the current state and new streams.
     *
     * This function handles the logic for automatically pinning new remote screen shares and
     * requesting user confirmation for pinning other screen shares.
     *
     * @param currentState The current state of the pin layout.
     * @param newStreams The new list of available streams.
     * @param maxAllowedPinnedStreams The maximum number of streams that can be pinned.
     * @param onScreenSharePinRequest A callback to request user confirmation for pinning a screen share.
     * @return The updated list of pinned streams.
     */
    private fun refreshPinnedStreams(
        currentState: State,
        newStreams: List<StreamUi>,
        maxAllowedPinnedStreams: Int,
        onScreenSharePinRequest: (StreamUi) -> Unit,
    ): List<StreamUi> {
        // Filters and limits the current pinned streams to only those present in the new stream list, respecting the maximum allowed.
        val retainedPinnedStreams = currentState.pinnedStreams
            .filter { it in newStreams }
            .take(maxAllowedPinnedStreams)

        // Find new remote screen shares that have appeared in the new list of streams.
        val newRemoteScreenShares = findNewRemoteScreenShares(currentState, newStreams)
        if (newRemoteScreenShares.isEmpty()) return retainedPinnedStreams

        // Determine which new screen shares should be automatically pinned and which require a pin request.
        val (screenShareToAutoPin, screenShareToRequestPin) = findScreenSharesToPin(
            newRemoteScreenShares, retainedPinnedStreams, maxAllowedPinnedStreams
        )
        // Update the pinned streams list by adding the screen share that should be automatically pinned.
        val updatedPinnedStreams = screenShareToAutoPin?.let {
            pinStream(
                stream = it,
                currentPinnedStreams = retainedPinnedStreams,
                maxAllowedPinnedStreams = maxAllowedPinnedStreams
            )
        } ?: retainedPinnedStreams
        // If there's a screen share that requires a pin request, invoke the callback function.
        screenShareToRequestPin?.let(onScreenSharePinRequest)

        return updatedPinnedStreams
    }

    /**
     * Finds new remote screen shares that have not been displayed before.
     *
     * @param currentState The current state of the pin layout.
     * @param newStreams The new list of available streams.
     * @return A list of new remote screen shares.
     */
    private fun findNewRemoteScreenShares(currentState: State, newStreams: List<StreamUi>): List<StreamUi> {
        val displayedStreamIds = currentState.allStreams.map { it.id }
        return newStreams.filter { isNewRemoteScreenShare(it, displayedStreamIds) }
    }

    /**
     * Determines which screen shares should be automatically pinned and which should require user confirmation.
     *
     * @param newRemoteScreenShares The list of new remote screen shares.
     * @param retainedPinnedStreams The currently pinned streams that are still present in the new stream list, up to the maximum allowed.
     * @return A pair containing the screen share to auto-pin (if any) and the screen share to request pin for (if any).
     */
    private fun findScreenSharesToPin(
        newRemoteScreenShares: List<StreamUi>,
        retainedPinnedStreams: List<StreamUi>,
        maxAllowedPinnedStreams: Int
    ): Pair<StreamUi?, StreamUi?> {
        // Automatically pin the first new remote screen share if there are no retained pinned streams and pinning is enabled.
        val screenShareToAutoPin = newRemoteScreenShares.firstOrNull { retainedPinnedStreams.isEmpty() && maxAllowedPinnedStreams > 0 }
        // Select the first new remote screen share that is not the one selected for automatic pinning for a pin request.
        val screenShareToRequestPin = newRemoteScreenShares.firstOrNull { it != screenShareToAutoPin }
        return screenShareToAutoPin to screenShareToRequestPin
    }

    /**
     * Sends a user message to request pinning a screen share.
     *
     * @param screenShare The screen share to request pinning for.
     */
    private fun requestPinForScreenShare(screenShare: StreamUi, callUserMessageProvider: CallUserMessagesProvider) {
        callUserMessageProvider.sendUserMessage(
            PinScreenshareMessage(screenShare.id, screenShare.username)
        )
    }

    /**
     * Pins a stream to the list of pinned streams.
     *
     * @param stream The stream to pin.
     * @param currentPinnedStreams The current list of pinned streams.
     * @param maxAllowedPinnedStreams The maximum number of streams that can be pinned.
     * @param prepend If true, the stream is added to the beginning of the pinned list.
     * @return The updated list of pinned streams.
     */
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