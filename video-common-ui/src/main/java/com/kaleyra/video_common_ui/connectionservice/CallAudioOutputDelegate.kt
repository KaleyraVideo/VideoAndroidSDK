package com.kaleyra.video_common_ui.connectionservice

import kotlinx.coroutines.flow.StateFlow

interface CallAudioOutputDelegate {

    val callOutputState: StateFlow<CallAudioState>

    fun setAudioOutput(output: CallAudioOutput)
}