package com.kaleyra.video_sdk.call.stream.layoutsystem.controller

import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.AutoLayout
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.AutoLayoutImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.ManualLayout
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.ManualLayoutImpl
import com.kaleyra.video_sdk.call.stream.layoutsystem.layout.StreamLayout
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutSettings
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    val layoutStreams: StateFlow<List<StreamUi>>

    /**
     * A flow of the constraints for the stream layout (`StreamLayoutConstraints`).
     *
     * This flow emits updates whenever the layout constraints change, such as the
     * maximum number of streams allowed in different layouts.
     */
    val layoutConstraints: StateFlow<StreamLayoutConstraints>

    /**
     * A flow of the settings for the stream layout (`StreamLayoutSettings`).
     *
     * This flow emits updates whenever the layout settings change, such as whether
     * it's a group call or the default camera preference.
     */
    val layoutSettings: StateFlow<StreamLayoutSettings>

    /**
     * A provider for call user messages (`CallUserMessagesProvider`).
     *
     * This provider is responsible for providing messages related to call users,
     * which might be used to display information about the users in the stream layout.
     */
    val callUserMessageProvider: CallUserMessagesProvider
}

/**
 * `StreamLayoutControllerOutput` defines the output data provided by a stream layout controller.
 *
 * This interface specifies the information that a stream layout controller exposes to
 * external components, allowing them to react to changes in the stream layout. This
 * includes the list of stream items to be displayed, the current layout mode (automatic
 * or manual), and whether the limit for pinned streams has been reached.
 */
internal interface StreamLayoutControllerOutput {
    /**
     * A flow of the list of stream items (`StreamItem`).
     *
     * This flow emits updates whenever the list of stream items changes, reflecting
     * changes in the input data, layout constraints, or user interactions.
     *
     * Each `StreamItem` represents a single stream to be displayed in the layout,
     * and contains information about the stream's content, its position, and any
     * special attributes (e.g., whether it's pinned or in fullscreen).
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

    /**
     * A flow indicating whether the limit for pinned streams has been reached.
     *
     * When `true`, the maximum number of streams that can be pinned in the layout
     * has been reached. This typically means that no more streams can be pinned
     * until one or more existing pinned streams are unpinned.
     *
     * When `false`, there is still capacity to pin additional streams.
     *
     * This flow emits updates whenever the pinned stream limit status changes, such as
     * when a stream is pinned or unpinned, or when the layout constraints are updated.
     */
    val isPinnedStreamLimitReached: Flow<Boolean>
}

/**
 * `StreamLayoutController` manages the layout and behavior of streams within a call or
 * video conferencing interface.
 *
 * This interface combines the responsibilities of receiving input data (`StreamLayoutControllerInputs`),
 * providing output data (`StreamLayoutControllerOutput`), and performing actions that modify the
 * stream layout. It defines the operations that can be performed to control the arrangement,
 * visibility, and behavior of streams.
 *
 * **Key Responsibilities:**
 *
 * -   **Receiving Input:** Receives updates about available streams, layout constraints, and
 *     layout settings via the `StreamLayoutControllerInputs` interface.
 * -   **Providing Output:** Exposes the current state of the layout, including the list of
 *     visible streams, pinned streams, and the fullscreen stream, via the
 *     `StreamLayoutControllerOutput` interface.
 * -   **Applying Changes:** Allows external components to apply changes to the stream list,
 *     constraints, and settings.
 * -   **Layout Modes:** Supports switching between manual and automatic layout modes, offering
 *     different levels of user control.
 * -   **Stream Management:** Provides methods for pinning, unpinning, and setting streams to
 *     fullscreen, allowing for dynamic control over stream visibility and prominence.
 *
 * **Layout Modes:**
 *
 * -   **Manual Mode:** In manual mode, the user has direct control over the layout. They can
 *     pin and unpin streams, and the layout will primarily reflect these user-driven changes.
 * -   **Automatic Mode:** In automatic mode, the layout is managed automatically based on the
 *     available streams, layout constraints, and settings. The controller determines the best
 *     arrangement of streams without direct user intervention.
 *
 * **Stream Management Operations:**
 *
 * -   **Pinning:** Pinned streams are given priority in the layout and are typically displayed
 *     more prominently.
 * -   **Unpinning:** Unpinned streams are no longer prioritized and may be hidden or displayed
 *     in a less prominent manner.
 * -   **Fullscreen:** A single stream can be set to fullscreen, maximizing its visibility and
 *     hiding other streams.
 *
 * **Usage:**
 *
 * Components that need to control the stream layout should interact with this interface.
 * Implementations of this interface are responsible for managing the internal state of the
 * layout and updating the output accordingly.
 */
internal interface StreamLayoutController : StreamLayoutControllerInputs,
    StreamLayoutControllerOutput {

    /**
     * Applies the given list of streams to the layout.
     *
     * This method updates the internal list of streams managed by the controller.
     * The layout will be updated to reflect the new list of streams.
     *
     * @param streams The list of streams to apply.
     */
    fun applyStreams(streams: List<StreamUi>)

    /**
     * Applies the given layout constraints to the layout.
     *
     * This method updates the internal layout constraints used by the controller.
     * The layout will be updated to adhere to the new constraints.
     *
     * @param constraints The layout constraints to apply.
     */
    fun applyConstraints(constraints: StreamLayoutConstraints)

    /**
     * Applies the given layout settings to the layout.
     *
     * This method updates the internal layout settings used by the controller.
     * The layout will be updated to reflect the new settings.
     *
     * @param settings The layout settings to apply.
     */
    fun applySettings(settings: StreamLayoutSettings)

    /**
     * Switches the stream layout to manual mode.
     *
     * In manual mode, the user has more control over the layout, such as pinning and
     * unpinning streams. The layout will primarily reflect user-driven changes.
     */
    fun switchToManualMode()

    /**
     * Switches the stream layout to automatic mode.
     *
     * In automatic mode, the layout is managed automatically based on the available
     * streams, layout constraints, and settings. The controller determines the best
     * arrangement of streams without direct user intervention.
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class StreamLayoutControllerImpl private constructor(
    override val callUserMessageProvider: CallUserMessagesProvider = CallUserMessagesProvider,
    coroutineScope: CoroutineScope
) : StreamLayoutController {

    companion object {
        @Volatile
        private var instance: StreamLayoutControllerImpl? = null

        fun getInstance(
            callUserMessageProvider: CallUserMessagesProvider = CallUserMessagesProvider,
            coroutineScope: CoroutineScope
        ): StreamLayoutControllerImpl {
            return instance ?: synchronized(this) {
                val instance = StreamLayoutControllerImpl(callUserMessageProvider, coroutineScope)
                Companion.instance = instance
                instance
            }
        }
    }

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

    private val _layoutStreams: MutableStateFlow<List<StreamUi>> = MutableStateFlow(emptyList())
    override val layoutStreams: StateFlow<List<StreamUi>> = _layoutStreams

    private val _layoutConstraints: MutableStateFlow<StreamLayoutConstraints> = MutableStateFlow(
        StreamLayoutConstraints()
    )
    override val layoutConstraints: StateFlow<StreamLayoutConstraints> = _layoutConstraints

    private val _layoutSettings: MutableStateFlow<StreamLayoutSettings> = MutableStateFlow(
        StreamLayoutSettings()
    )
    override val layoutSettings: StateFlow<StreamLayoutSettings> = _layoutSettings

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

    /*
     * A flow of the list of stream items (`StreamItem`) derived from the current layout's stream items.
     */
    override val streamItems: Flow<List<StreamItem>> = _internalState.flatMapLatest { it.streamLayout.streamItems }

    override val isInAutoMode: Flow<Boolean> = _internalState.map { it.streamLayout is AutoLayout }

    override val isPinnedStreamLimitReached: Flow<Boolean> = combine(
        streamItems,
        layoutConstraints.map { it.featuredStreamThreshold }
    ) { streamItems, featuredStreamThreshold -> streamItems.count { it.isPinned() } >= featuredStreamThreshold }

    init {
        layoutStreams
            .onEach { streams ->
                _internalState.update { state ->
                    val remoteScreenShareStreams = streams.filter { it.isRemoteScreenShare() }
                    // Check if a message should be sent to pin a new screen share.
                    if (shouldSendPinScreenShareMessage(state)) {
                        // Find the new remote screen shares.
                        val screenShareToRequestPin = findNewRemoteScreenShares(state, remoteScreenShareStreams).firstOrNull()
                        // Send a message to pin the new screen share.
                        screenShareToRequestPin?.let {
                            callUserMessageProvider.sendUserMessage(PinScreenshareMessage(it.id, it.userInfo?.username ?: ""))
                        }
                    }
                    // Update the list of remote screen share streams in the state.
                    state.copy(remoteScreenShareStreams = remoteScreenShareStreams)
                }
            }.launchIn(coroutineScope)
    }

    override fun applyStreams(streams: List<StreamUi>) {
        _layoutStreams.value = streams
    }

    override fun applyConstraints(constraints: StreamLayoutConstraints) {
        _layoutConstraints.value = constraints
    }

    override fun applySettings(settings: StreamLayoutSettings) {
        _layoutSettings.value = settings
    }

    override fun switchToManualMode() {
        manualLayout.clearPinnedStreams()
        updateInternalState(manualLayout)
    }

    override fun switchToAutoMode() {
        manualLayout.clearPinnedStreams()
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
     * @return `true` if a message should be sent, `false` otherwise.
     */
    private fun shouldSendPinScreenShareMessage(
        state: ControllerState,
    ): Boolean {
        // A message should be sent if the layout is manual.
        return state.streamLayout is ManualLayout
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