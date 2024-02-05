package com.kaleyra.video_common_ui.connectionservice

data class CallAudioState(
    val currentOutput: CallAudioOutput? = null,
    val availableOutputs: List<CallAudioOutput> = listOf()
)