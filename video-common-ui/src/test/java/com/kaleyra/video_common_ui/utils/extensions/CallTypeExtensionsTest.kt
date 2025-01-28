package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.extensions.CallTypeExtensions.toCallButtons
import org.junit.Assert
import org.junit.Test

class CallTypeExtensionsTest {

    @Test
    fun legacyActions_toCallUIButtons_legacyActionsMappedToCallButtons() {
        val callButtons = Call.PreferredType.audioVideo().toCallButtons(setOf(CallUI.Action.HangUp))

        Assert.assertEquals(1, callButtons.size)
        Assert.assertEquals(CallUI.Button.HangUp, callButtons.first())
    }

    @Test
    fun audioCall_toCallUIButtons_legacyActionsMappedToCallButtons() {
        val callButtons = Call.PreferredType.audioOnly().toCallButtons()

        Assert.assertEquals(CallUI.Button.Collections.audioCall, callButtons)
    }

    @Test
    fun videoCall_toCallUIButtons_legacyActionsMappedToCallButtons() {
        val callButtons = Call.PreferredType.audioVideo().toCallButtons()

        Assert.assertEquals(CallUI.Button.Collections.videoCall, callButtons)
    }
}