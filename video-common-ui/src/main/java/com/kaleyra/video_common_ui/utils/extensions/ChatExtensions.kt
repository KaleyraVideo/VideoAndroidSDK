package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video_common_ui.ChatUI

object ChatExtensions {

    fun ChatUI.Action.toChatUIButtons(): ChatUI.Button = when(this) {
        is ChatUI.Action.CreateCall -> ChatUI.Button.Call(preferredType, maxDuration, recordingType)
        ChatUI.Action.ShowParticipants -> ChatUI.Button.Participants
    }
}