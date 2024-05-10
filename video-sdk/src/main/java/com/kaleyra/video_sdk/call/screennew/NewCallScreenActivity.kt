package com.kaleyra.video_sdk.call.screennew

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

class NewCallScreenActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            var callActionsUI by remember {
                mutableStateOf(
                    CallActionsUI(
                        microphoneAction = MicAction(),
                        cameraAction = CameraAction(),
                        flipCameraAction = FlipCameraAction(),
                        audioAction = AudioAction(),
                        chatAction = ChatAction(),
                        fileShareAction = FileShareAction(),
                        screenShareAction = ScreenShareAction(),
                        whiteboardAction = WhiteboardAction(),
                        virtualBackgroundAction = VirtualBackgroundAction()
                    )
                )
            }
            KaleyraM3Theme {
                CallScreen(
                    windowSizeClass = calculateWindowSizeClass(this),
                    actions = callActionsUI,
                    onMicToggled = { toggled ->
                        val micAction = callActionsUI.microphoneAction?.copy(isToggled = toggled)
                        callActionsUI = callActionsUI.copy(microphoneAction = micAction)
                    },
                    onCameraToggled = { toggled ->
                        val micAction = callActionsUI.cameraAction?.copy(isToggled = toggled)
                        callActionsUI = callActionsUI.copy(cameraAction = micAction)
                    },
                    onScreenShareToggle = { toggled ->
                        val micAction = callActionsUI.screenShareAction?.copy(isToggled = toggled)
                        callActionsUI = callActionsUI.copy(screenShareAction = micAction)
                    },
                    onHangUpClick = { /*TODO*/ },
                    onFlipCameraClick = { /*TODO*/ },
                    onAudioClick = { /*TODO*/ },
                    onChatClick = { /*TODO*/ },
                    onFileShareClick = { /*TODO*/ },
                    onWhiteboardClick = { /*TODO*/ },
                    onVirtualBackgroundClick = { /*TODO*/ },
                    onParticipantClick = { },
                    onBackPressed = { }
                )
            }
        }
    }
}