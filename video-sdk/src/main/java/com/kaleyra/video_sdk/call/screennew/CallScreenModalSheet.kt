package com.kaleyra.video_sdk.call.screennew

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kaleyra.video_sdk.call.audiooutput.AudioOutputComponent
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.launch

@Immutable
internal enum class CallScreenModalSheetType {
    Audio,
    ScreenShare,
    FileShare,
    Whiteboard,
    VirtualBackground
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenModalSheet(
    modalSheetType: CallScreenModalSheetType?,
    sheetState: SheetState,
    onRequestDismiss: () -> Unit,
    onUserMessageActionClick: (UserMessage.Action) -> Unit,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val scope = rememberCoroutineScope()
    val onDismiss: () -> Unit = remember(sheetState) {
        {
            // Check the lifecycle state to prevent the modal bottom sheet from dismissing when entering the PiP mode
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                scope.launch {
                    sheetState.hide()
                    onRequestDismiss()
                }
            }
        }
    }

    if (modalSheetType != null) {
        ModalBottomSheet(
            onDismissRequest = onRequestDismiss,
            sheetState = sheetState,
            dragHandle = null,
            windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
            modifier = modifier
        ) {
            when (modalSheetType) {
                CallScreenModalSheetType.Audio -> AudioOutputComponent(onDismiss = onDismiss)

                CallScreenModalSheetType.ScreenShare -> ScreenShareComponent(onDismiss = onDismiss)

                CallScreenModalSheetType.FileShare -> FileShareComponent(
                    onDismiss = onDismiss,
                    onUserMessageActionClick = onUserMessageActionClick,
                )

                CallScreenModalSheetType.Whiteboard -> WhiteboardComponent(
                    onDismiss = onDismiss,
                    onUserMessageActionClick = onUserMessageActionClick
                )

                CallScreenModalSheetType.VirtualBackground -> VirtualBackgroundComponent(
                    onDismiss = onDismiss
                )
            }
        }
    }
}