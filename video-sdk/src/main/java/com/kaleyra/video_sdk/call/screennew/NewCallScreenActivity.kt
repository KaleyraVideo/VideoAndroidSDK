package com.kaleyra.video_sdk.call.screennew

import android.annotation.SuppressLint
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
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

class NewCallScreenActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            val micAction = MicAction()
            val screenShareAction = ScreenShareAction()
            var callActionsUI by remember {
                mutableStateOf(
                    ImmutableList(
                        listOf(
                            HangUpAction(),
                            micAction,
                            CameraAction(),
                            FlipCameraAction(),
                            AudioAction(),
                            FileShareAction(),
                            screenShareAction,
                            WhiteboardAction(),
                            VirtualBackgroundAction(),
                            ChatAction(),
                        )
                    )
                )
            }

            KaleyraM3Theme {
                CallScreen(
                    windowSizeClass = calculateWindowSizeClass(this),
                    actions = callActionsUI,
                    onMicToggled = { toggled ->
//                        micAction.copy(isToggled = toggled)
                    },
                    onCameraToggled = { toggled ->
                    },
                    onScreenShareToggle = { toggled ->
                        val index = callActionsUI.value.indexOfFirst { it is ScreenShareAction }
                        val list = callActionsUI.value.toMutableList()
                        list[index] = screenShareAction.copy(isToggled = toggled)
                        callActionsUI = ImmutableList(list)
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