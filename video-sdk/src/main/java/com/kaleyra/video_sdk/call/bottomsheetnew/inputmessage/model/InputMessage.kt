package com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model

import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
internal interface InputMessage {
    val id: String
}

internal sealed class MicMessage(
    override val id: String = UUID.randomUUID().toString()
): InputMessage {

    data object Enabled: MicMessage()

    data object Disabled: MicMessage()
}

internal sealed class CameraMessage(
    override val id: String = UUID.randomUUID().toString()
): InputMessage {

    data object Enabled: CameraMessage()

    data object Disabled: CameraMessage()
}