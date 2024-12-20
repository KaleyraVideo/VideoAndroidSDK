package com.kaleyra.video_sdk.call.stream.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal interface StreamLayout {

    val streams: StateFlow<List<StreamUi>>

    val streamsPresentation: StateFlow<List<StreamPresentation>>
}

internal data class AutoLayout(
    override val streams: StateFlow<List<StreamUi>>,
    override val streamsPresentation: StateFlow<List<StreamPresentation>>,
) : StreamLayout

internal data class GridLayout(
    override val streams: StateFlow<List<StreamUi>>,
    override val streamsPresentation: StateFlow<List<StreamPresentation>>,
) : StreamLayout

@Immutable
data class StreamPresentation(
    val stream: StreamUi,
    val isPromoted: Boolean = false,
)

//interface AutoLayout : StreamsLayout {
//    val isOto: StateFlow<Boolean>
//
//    val isDefaultBackCamera: Boolean
//}

//interface VideoLayoutController {
//
//    val call: Call
//
//    val autoLayout: AutoLayout
//
//    val gridLayout: GridLayout
//
//    val pinLayout: PinLayout
//
//    val fullscreenLayout: FullscreenLayout
//
//    fun pin(streamId: String, prepend: Boolean = false, force: Boolean = false): Boolean
//
//    fun unpin(streamId: String)
//
//    fun enableFullscreen(id: String)
//
//    fun exitFullscreen()
//}

//class VideoLayoutControllerImpl(
//    override val call: Call,
//    override val autoLayout: AutoLayout = AutoLayoutImpl(),
//    override val gridLayout: GridLayout = GridLayoutImpl(),
//    override val pinLayout: PinLayout = PinLayoutImpl(),
//    override val fullscreenLayout: FullscreenLayout = FullscreenImpl(),
//    coroutineScope: CoroutineScope,
//) : VideoLayoutController {
//
//    private val _layout: MutableStateFlow<StreamsLayout> = MutableStateFlow(autoLayout)
//    val layout: StateFlow<StreamsLayout> = _layout
//
//    override fun pin(streamId: String, prepend: Boolean, force: Boolean): Boolean {
//        pinLayout.pin(streamId, prepend, force)
//        return false
//    }
//
//    override fun unpin(streamId: String) {
//
//    }
//
//    override fun enableFullscreen(id: String) {
//        _layout.value = fullscreenLayout
//    }
//
//    override fun exitFullscreen() {
//        // TODO restore previous layout
////        _layout.value = fullscreenLayout
//    }
//}

//class AutoLayoutImpl(
//    override val streams: StateFlow<List<StreamUi>>,
//    override val isOto: StateFlow<Boolean>,
//    override val isDefaultBackCamera: Boolean,
//    coroutineScope: CoroutineScope,
//) : AutoLayout {
//    private val _arrangedStreams: MutableStateFlow<List<StreamPresentation>> =
//        MutableStateFlow(emptyList())
//    override val arrangedStreams: StateFlow<List<StreamPresentation>> = _arrangedStreams
//
//    init {
//        combine(
//            streams,
//            isOto
//        ) { streams, isOto ->
//            _arrangedStreams.value = if (isOto) otoArrangement(streams) else mtmArrangement(streams)
//        }.launchIn(coroutineScope)
//    }
//
//    private fun otoArrangement(streams: List<StreamUi>): List<StreamPresentation> {
//        return streams
//            .sortedWith(
//                compareByDescending<StreamUi> { it.isRemoteScreenShare() }
//                    .thenByDescending { it.isMyCameraStream() && isDefaultBackCamera }
//                    .thenByDescending { it.isRemoteCameraStream() }
//            )
//            .mapIndexed { index, stream ->
//                val viewMode =
//                    if (index == 0) StreamPresentation.ViewMode.Featured else StreamPresentation.ViewMode.Default
//                StreamPresentation(stream, viewMode)
//            }
//    }
//
//    private fun mtmArrangement(streams: List<StreamUi>): List<StreamPresentation> {
//        return listOf()
//    }
//}
internal fun StreamUi.isRemoteScreenShare(): Boolean = !isMine && video?.isScreenShare == true

internal fun StreamUi.isLocalScreenShare(): Boolean = isMine && video?.isScreenShare == true

internal fun StreamUi.isMyCameraStream(): Boolean = isMine && video != null && !video.isScreenShare

internal fun StreamUi.isRemoteCameraStream(): Boolean =
    !isMine && video != null && !video.isScreenShare


