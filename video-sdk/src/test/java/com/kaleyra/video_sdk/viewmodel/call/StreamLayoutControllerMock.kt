package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutConstraints
import com.kaleyra.video_sdk.call.stream.layoutsystem.controller.StreamLayoutController
import com.kaleyra.video_sdk.call.stream.layoutsystem.config.StreamLayoutSettings
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class StreamLayoutControllerMock(
    initialLayoutStreams: List<StreamUi> = emptyList(),
    initialLayoutConstraints: StreamLayoutConstraints = StreamLayoutConstraints(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
    initialLayoutSettings: StreamLayoutSettings = StreamLayoutSettings(),
    initialPinnedStreamIds: List<String> = emptyList(),
    initialFullscreenId: String? = null,
    initialStreamItems: List<StreamItem> = emptyList(),
    initialIsInAutoMode: Boolean = false,
    initialIsPinnedStreamLimitReached: Boolean = false,
    override val callUserMessageProvider: CallUserMessagesProvider = mockk(relaxed = true),
) : StreamLayoutController {

    private val _layoutStreams = MutableStateFlow(initialLayoutStreams)
    override val layoutStreams: StateFlow<List<StreamUi>> = _layoutStreams

    private val _layoutConstraints = MutableStateFlow(initialLayoutConstraints)
    override val layoutConstraints: StateFlow<StreamLayoutConstraints> = _layoutConstraints

    private val _layoutSettings = MutableStateFlow(initialLayoutSettings)
    override val layoutSettings: StateFlow<StreamLayoutSettings> = _layoutSettings

    private val _streamItems: MutableStateFlow<List<StreamItem>> = MutableStateFlow(initialStreamItems)
    override val streamItems: Flow<List<StreamItem>> = _streamItems

    private val _isInAutoMode: MutableStateFlow<Boolean> = MutableStateFlow(initialIsInAutoMode)
    override val isInAutoMode: Flow<Boolean> = _isInAutoMode

    override val isPinnedStreamLimitReached: Flow<Boolean> = MutableStateFlow(initialIsPinnedStreamLimitReached)

    var fullscreenId: String? = initialFullscreenId
        private set

    var pinnedStreamIds: List<String> = initialPinnedStreamIds
        private set

    fun setStreamItems(items: List<StreamItem>) {
        _streamItems.value = items
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
        _isInAutoMode.value = false
    }

    override fun switchToAutoMode() {
        _isInAutoMode.value = true
    }

    override fun pinStream(streamId: String, prepend: Boolean, force: Boolean): Boolean {
        val threshold = layoutConstraints.value.featuredStreamThreshold
        if (pinnedStreamIds.size >= threshold && !force) return false
        pinnedStreamIds = if (prepend) listOf(streamId) + pinnedStreamIds.take(threshold - 1) else pinnedStreamIds.take(threshold - 1) + streamId
        return true
    }

    override fun unpinStream(streamId: String) {
        pinnedStreamIds = pinnedStreamIds - streamId
    }

    override fun clearPinnedStreams() {
        pinnedStreamIds = emptyList()
    }

    override fun setFullscreenStream(id: String) {
        fullscreenId = id
    }

    override fun clearFullscreenStream() {
        fullscreenId = null
    }
}