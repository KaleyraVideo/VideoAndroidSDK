package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUI.Button
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.toCallUIButton

object CallTypeExtensions {

    fun Call.PreferredType.toCallButtons(legacyCallActions:Collection<CallUI.Action>? = null) =
        when {
            legacyCallActions != null -> legacyCallActions.map { it.toCallUIButton() }.toMutableSet()
            hasVideo() -> Button.Collections.videoCall.toMutableSet()
            else -> Button.Collections.audioCall.toMutableSet()
        }
}