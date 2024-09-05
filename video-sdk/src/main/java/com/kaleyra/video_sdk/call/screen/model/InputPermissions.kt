package com.kaleyra.video_sdk.call.screen.model

import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@Immutable
@OptIn(ExperimentalPermissionsApi::class)
internal data class InputPermissions (
    val shouldAskCameraPermission: Boolean = false,
    val wasMicPermissionAsked: Boolean = false,
    val wasCameraPermissionAsked: Boolean = false,
    var micPermission: PermissionState? = null,
    var cameraPermission: PermissionState? = null,
)