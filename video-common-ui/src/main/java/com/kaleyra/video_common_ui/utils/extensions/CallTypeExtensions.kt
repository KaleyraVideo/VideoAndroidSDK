package com.kaleyra.video_common_ui.utils.extensions

import com.kaleyra.video.conference.Call

object CallTypeExtensions {

    fun Call.PreferredType.toCallType() =
        if (this.hasAudio() && this.hasVideo() && this.video is Call.Video.Enabled) Call.Type.audioVideo()
        else if (this.hasAudio() && this.hasVideo() && this.video is Call.Video.Disabled) Call.Type.audioUpgradable()
        else Call.Type.audioOnly()
}