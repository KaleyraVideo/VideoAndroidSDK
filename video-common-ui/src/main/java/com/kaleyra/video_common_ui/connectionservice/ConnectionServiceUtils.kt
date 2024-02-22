package com.kaleyra.video_common_ui.connectionservice

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_utils.ContextRetainer

object ConnectionServiceUtils {
    @get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    val isConnectionServiceSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !DeviceUtils.isSmartGlass

    val isConnectionServiceEnabled: Boolean
        get() = isConnectionServiceSupported &&
                ContextRetainer.context.hasConnectionServicePermissions() &&
                KaleyraVideo.conference.connectionServiceOption != ConnectionServiceOption.Disabled

}

