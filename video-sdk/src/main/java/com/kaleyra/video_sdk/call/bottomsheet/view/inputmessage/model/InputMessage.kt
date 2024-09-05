package com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.model

import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
interface InputMessage {
    val id: String
}

sealed class MicMessage(
    override val id: String = UUID.randomUUID().toString()
): InputMessage {

    data object Enabled: MicMessage()

    data object Disabled: MicMessage()
}

sealed class CameraMessage(
    override val id: String = UUID.randomUUID().toString()
): InputMessage {

    data object Enabled: CameraMessage()

    data object Disabled: CameraMessage()
}