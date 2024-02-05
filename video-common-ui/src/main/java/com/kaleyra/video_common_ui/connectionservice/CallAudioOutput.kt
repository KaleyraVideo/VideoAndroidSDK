package com.kaleyra.video_common_ui.connectionservice

sealed class CallAudioOutput {
    data object Muted : CallAudioOutput()
    data object Earpiece : CallAudioOutput()
    data object Speaker : CallAudioOutput()
    data object WiredHeadset : CallAudioOutput()
    data class Bluetooth(val id: String, val name: String? = null) : CallAudioOutput()
}