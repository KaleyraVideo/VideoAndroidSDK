package com.kaleyra.video_sdk.call.screennew

import androidx.compose.runtime.Immutable

@Immutable
data class CallActionsUI(
    val hangUpAction: HangUpAction = HangUpAction(),
    val microphoneAction: MicAction? = null,
    val cameraAction: CameraAction? = null,
    val flipCameraAction: FlipCameraAction? = null,
    val audioAction: AudioAction? = null,
    val chatAction: ChatAction? = null,
    val fileShareAction: FileShareAction? = null,
    val screenShareAction: ScreenShareAction? = null,
    val whiteboardAction: WhiteboardAction? = null,
    val virtualBackgroundAction: VirtualBackgroundAction? = null,
)