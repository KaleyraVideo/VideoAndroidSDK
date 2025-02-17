package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.utils.extensions.CallTypeExtensions.toCallType
import org.junit.Assert
import org.junit.Test

class CallTypeExtensionsTest {

    @Test
    fun preferredTypeAudioVideo_toCallType_CallTypeAudioVideo() {
        Assert.assertEquals(Call.Type.audioVideo(), Call.PreferredType.audioVideo().toCallType())
    }

    @Test
    fun preferredTypeAudioUpgradable_toCallType_CallTypeAudioUpgradable() {
        Assert.assertEquals(Call.Type.audioUpgradable(), Call.PreferredType.audioUpgradable().toCallType())
    }

    @Test
    fun preferredTypeAudioOnly_toCallType_CallTypeAudioOnly() {
        Assert.assertEquals(Call.Type.audioOnly(), Call.PreferredType.audioOnly().toCallType())
    }
}
