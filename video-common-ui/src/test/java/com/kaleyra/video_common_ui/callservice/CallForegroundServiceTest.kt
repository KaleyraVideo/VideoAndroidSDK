package com.kaleyra.video_common_ui.callservice

import android.content.pm.ServiceInfo
import android.os.Build
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CallForegroundServiceTest {

    private val callForegroundService = object : CallForegroundService {}

    @Test
    fun getForegroundServiceType_phoneCallFlagSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasScreenSharingInput = false
        )
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
    fun cameraPermissionTrue_getForegroundServiceType_cameraFlagSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = true,
            hasMicrophonePermission = false,
            hasScreenSharingInput = false
        )
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
    fun cameraPermissionFalse_getForegroundServiceType_cameraFlagNotSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasScreenSharingInput = false
        )
        assertEquals(0, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
    fun microphonePermissionTrue_getForegroundServiceType_microphoneFlagSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = true,
            hasScreenSharingInput = false
        )
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
    fun microphonePermissionFalse_getForegroundServiceType_microphoneFlagNotSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasScreenSharingInput = false
        )
        assertEquals(0, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.R])
    fun apiHigherThan30_getForegroundServiceType_cameraAndMicFlagsSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = true,
            hasMicrophonePermission = true,
            hasScreenSharingInput = false
        )
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE, serviceType and (ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE))
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.Q])
    fun apiLowerThan30_getForegroundServiceType_cameraAndMicFlagsNotSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = true,
            hasMicrophonePermission = true,
            hasScreenSharingInput = false
        )
        assertEquals(0, serviceType and (ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE))
    }

    @Test
    fun hasScreenSharingInputTrue_getForegroundServiceType_mediaProjectionFlagSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasScreenSharingInput = true
        )
        assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    @Test
    fun hasScreenSharingInputFalse_getForegroundServiceType_mediaProjectionFlagNotSet() {
        val serviceType = callForegroundService.getForegroundServiceType(
            hasCameraPermission = false,
            hasMicrophonePermission = false,
            hasScreenSharingInput = false
        )
        assertEquals(0, serviceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }
}