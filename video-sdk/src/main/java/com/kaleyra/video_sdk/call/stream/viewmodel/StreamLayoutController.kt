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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * `StreamLayoutControllerInputs` defines the input dependencies required by a stream layout controller.
 *
 * This interface specifies the data sources and providers that a stream layout controller needs
 * to manage the stream layout. It includes information about the available streams, layout
 * constraints, layout settings, and a provider for call user messages.
 */
internal interface StreamLayoutControllerInputs {
    /**
     * A flow of the list of available streams (`StreamUi`).
     *
     * This flow emits updates whenever the list of available streams changes.
     */
    val layoutStreams: Flow<List<StreamUi>>

    /**
     * A flow of the constraints for the stream layout (`StreamLayoutConstraints`).
     *
     * This flow emits updates whenever the layout constraints change, such as the
     * maximum number of streams allowed in different layouts.
     */
    val layoutConstraints: Flow<StreamLayoutConstraints>

    /**
     * A flow of the settings for the stream layout (`StreamLayoutSettings`).
     *
     * This flow emits updates whenever the layout settings change, such as whether
     * it's a group call or the default camera preference.
     */
    val layoutSettings: Flow<StreamLayoutSettings>

    /**
     * A provider for call user messages (`CallUserMessagesProvider`).
     *
     * This provider is responsible for providing messages related to call users,
     * which might be used to display information about the users in the stream layout.
     */
    val callUserMessageProvider: CallUserMessagesProvider
}

/**
 * `StreamLayoutControllerOutput` defines the output provided by a stream layout controller.
 *
 * This interface specifies the data that a stream layout controller produces, which is the
 * list of `StreamItem`s to be displayed, and whether automatic layout mode is enabled.
 */
internal interface StreamLayoutControllerOutput {
    /**
     * A flow of the list of stream items (`StreamItem`).
     *
     * This flow emits updates whenever the list of stream items changes, reflecting
     * changes in the input data, layout constraints, or user interactions.
     */
    val streamItems: Flow<List<StreamItem>>

    /**
     * A flow indicating whether automatic layout mode is enabled.
     *
     * When `true`, the stream layout controller automatically manages the arrangement
     * and positioning of stream items. When `false`, the layout may be fixed or
     * manually controlled.
     *
     * This flow emits updates whenever the auto mode status changes.
     */
    val isInAutoMode: Flow<Boolean>
}

/**
 * `StreamLayoutController` combines the input and output interfaces for a stream layout controller
 * and defines the operations that can be performed on the layout.
 *
 * This interface represents a controller that manages the stream layout, including switching
 * between different layout modes (manual and auto), pinning and unpinning streams, and
 * setting streams to fullscreen.
 */
internal interface StreamLayoutController : StreamLayoutControllerInputs, StreamLayoutControllerOutput {
    /**
     * Switches the stream layout to manual mode.
     *
     * In manual mode, the user has more control over the layout, such as pinning and
     * unpinning streams.
     */
    fun switchToManualMode()

    /**
     * Switches the stream layout to automatic mode.
     *
     * In automatic mode, the layout is managed automatically based on the available
     * streams and layout constraints.
     */
    fun switchToAutoMode()

    /**
     * Pins a stream to the layout. Pinned streams are typically displayed prominently.
     *
     * @param streamId The ID of the stream to pin.
     * @param prepend If `true`, the stream is added to the beginning of the pinned list.
     *                If `false` (default), the stream is added to the end.
     * @param force If `true`, the stream will be pinned even if the maximum number of pinned
     *              streams has been reached, potentially unpinning another stream.
     *              If `false` (default), the stream will not be pinned if the maximum is reached.
     * @return `true` if the stream was successfully pinned, `false` otherwise.
     */
    fun pinStream(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean

    /**
     * Unpins a stream from the layout. Unpinned streams are no longer displayed prominently.
     *
     * @param streamId The ID of the stream to unpin.
     */
    fun unpinStream(streamId: String)

    /**
     * Clears all pinned streams from the layout.
     */
    fun clearPinnedStreams()

    /**
     * Sets a stream to fullscreen mode. Only one stream can be in fullscreen at a time.
     *
     * @param id The ID of the stream to set to fullscreen.
     */
    fun setFullscreenStream(id: String)

    /**
     * Clears the fullscreen stream, if any.
     */
    fun clearFullscreenStream()
}

/**
 * `StreamLayoutControllerImpl` is a concrete implementation of the `StreamLayoutController` interface.
 *
 * It manages the stream layout, including switching between automatic and manual layouts,
 * handling stream pinning and fullscreen, and sending messages related to screen sharing.
 *
 * @property layoutStreams A flow of the list of available streams (`StreamUi`).
 * @property layoutConstraints A flow of the constraints for the stream layout (`StreamLayoutConstraints`).
 * @property layoutSettings A flow of the settings for the stream layout (`StreamLayoutSettings`).
 * @property callUserMessageProvider A provider for sending messages to call users.
 * @property coroutineScope The coroutine scope used for launching coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class StreamLayoutControllerImpl(
    override val layoutStreams: Flow<List<StreamUi>>,
    override val layoutConstraints: Flow<StreamLayoutConstraints>,
    override val layoutSettings: Flow<StreamLayoutSettings>,
    override val callUserMessageProvider: CallUserMessagesProvider = CallUserMessagesProvider,
    coroutineScope: CoroutineScope,
) : StreamLayoutController {

    /**
     * Represents the internal state of the controller.
     *
     * @property streamLayout The currently active stream layout (`StreamLayout`).
     * @property previousStreamLayout The previously active stream layout, used for restoring the layout when exiting fullscreen.
     * @property remoteScreenShareStreams The list of streams that are currently sharing their screen remotely.
     */
    private data class ControllerState(
        val streamLayout: StreamLayout,
        val previousStreamLayout: StreamLayout? = null,
        val remoteScreenShareStreams: List<StreamUi> = emptyList(),
    )

    /**
     * The automatic stream layout implementation.
     */
    private val autoLayout: AutoLayout = AutoLayoutImpl(
        layoutStreams,
        layoutConstraints,
        layoutSettings,
        coroutineScope = coroutineScope
    )

    /**
     * The manual stream layout implementation.
     */
    private val manualLayout: ManualLayout = ManualLayoutImpl(
        layoutStreams,
        layoutConstraints,
        coroutineScope = coroutineScope
    )

    /**
     * A mutable state flow that holds the current controller state.
     */
    private val _internalState: MutableStateFlow<ControllerState> = MutableStateFlow(ControllerState(autoLayout))

    /**
     * A flow of the list of stream items (`StreamItem`) derived from the current layout's stream items.
     */
    override val streamItems: Flow<List<StreamItem>> = _internalState.flatMapLatest { it.streamLayout.streamItems }

    override val isInAutoMode: Flow<Boolean> = _internalState.map { it.streamLayout is AutoLayout }

    init {
        layoutStreams
            .onEach { streams ->
                val remoteScreenShareStreams = streams.filter { it.isRemoteScreenShare() }
                _internalState.update { state ->
                    // Check if a message should be sent to pin a new screen share.
                    if (shouldSendPinScreenShareMessage(state, remoteScreenShareStreams)) {
                        // Find the new remote screen shares.
                        val screenShareToRequestPin =
                            findNewRemoteScreenShares(state, remoteScreenShareStreams).firstOrNull()
                        // Send a message to pin the new screen share.
                        screenShareToRequestPin?.let {
                            callUserMessageProvider.sendUserMessage(PinScreenshareMessage(it.id, it.username))
                        }
                    }
                    // Update the list of remote screen share streams in the state.
                    state.copy(remoteScreenShareStreams = remoteScreenShareStreams)
                }

            }.launchIn(coroutineScope)
    }

    override fun switchToManualMode() {
        manualLayout.clearPinnedStreams()
        updateInternalState(manualLayout)
    }

    override fun switchToAutoMode() {
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

    /**
     * Checks if a message should be sent to pin a new screen share.
     *
     * @param state The current controller state.
     * @param remoteScreenShareStreams The list of remote screen share streams.
     * @return `true` if a message should be sent, `false` otherwise.
     */
    private fun shouldSendPinScreenShareMessage(
        state: ControllerState,
        remoteScreenShareStreams: List<StreamUi>,
    ): Boolean {
        // A message should be sent if the layout is manual or if the layout is auto and there is more than one remote screen share.
        return state.streamLayout is ManualLayout || (state.streamLayout is AutoLayout && remoteScreenShareStreams.size > 1)
    }

    /**
     * Finds the new remote screen shares that were not present in the previous state.
     *
     * @param currentState The current controller state.
     * @param remoteScreenShareStreams The list of remote screen share streams.
     * @return A list of new remote screen share streams.
     */
    private fun findNewRemoteScreenShares(
        currentState: ControllerState,
        remoteScreenShareStreams: List<StreamUi>,
    ): List<StreamUi> {
        val currentRemoteScreenShareIds = currentState.remoteScreenShareStreams.map { it.id }
        return remoteScreenShareStreams.filter { stream -> stream.id !in currentRemoteScreenShareIds }
    }

    /**
     * Updates the internal state with a new layout.
     *
     * @param newLayout The new stream layout to switch to.
     */
    private fun updateInternalState(newLayout: StreamLayout) {
        _internalState.update { state ->
            state.copy(
                streamLayout = newLayout,
                previousStreamLayout = state.streamLayout
            )
        }
    }
}