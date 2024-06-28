package com.kaleyra.video_common_ui.callservice

import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi

internal interface CallForegroundService {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getForegroundServiceType(
        hasCameraPermission: Boolean,
        hasMicrophonePermission: Boolean,
        hasScreenSharingInput: Boolean
    ): Int {
        val cameraFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasCameraPermission) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0
        val microphoneFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasMicrophonePermission) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        val screenSharingFlag = if (hasScreenSharingInput) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
        return ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or cameraFlag or microphoneFlag or screenSharingFlag
    }
}