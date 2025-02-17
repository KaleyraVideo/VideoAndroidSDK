package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.toCallUIButton

object CallTypeExtensions {

    fun Call.Type.toCallButtons(legacyCallActions: Set<CallUI.Action>? = null): MutableSet<CallUI.Button> =
        when {
            legacyCallActions != null -> legacyCallActions.map { it.toCallUIButton() }.toMutableSet()
            hasVideo() -> CallUI.Button.Collections.videoCall.toMutableSet()
            else -> CallUI.Button.Collections.audioCall.toMutableSet()
        }

    fun Call.PreferredType.toCallType() =
        if (this.hasAudio() && this.hasVideo() && this.video is Call.Video.Enabled) Call.Type.audioVideo()
        else if (this.hasAudio() && this.hasVideo() && this.video is Call.Video.Disabled) Call.Type.audioUpgradable()
        else Call.Type.audioOnly()
}
