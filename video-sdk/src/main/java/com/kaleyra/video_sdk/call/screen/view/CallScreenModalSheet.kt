package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kaleyra.video_sdk.call.audiooutput.AudioOutputComponent
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.participants.ParticipantsComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.InputMessageDragHandleTag
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.launch

@Immutable
internal enum class ModalSheetComponent {
    Audio,
    ScreenShare,
    FileShare,
    Whiteboard,
    VirtualBackground,
    Participants
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenModalSheet(
    modalSheetComponent: ModalSheetComponent?,
    sheetState: SheetState,
    onRequestDismiss: () -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit,
    modifier: Modifier = Modifier,
    onComponentDisplayed: (ModalSheetComponent?) -> Unit = {},
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

    if (modalSheetComponent != null) {
        ModalBottomSheet(
            onDismissRequest = onRequestDismiss,
            shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
            sheetState = sheetState,
            dragHandle = { CallBottomSheetDefaults.HDragHandle() },
            windowInsets = WindowInsets(0.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = modifier
        ) {
            when (modalSheetComponent) {
                ModalSheetComponent.Audio -> AudioOutputComponent(onDismiss = onDismiss)

                ModalSheetComponent.ScreenShare -> ScreenShareComponent(
                    onDismiss = onDismiss,
                    onAskInputPermissions = onAskInputPermissions
                )

                ModalSheetComponent.FileShare -> FileShareComponent(
                    onDismiss = onDismiss,
                    onUserMessageActionClick = onUserMessageActionClick,
                )

                ModalSheetComponent.Whiteboard -> WhiteboardComponent(
                    onDismiss = onDismiss,
                    onUserMessageActionClick = onUserMessageActionClick
                )

                ModalSheetComponent.VirtualBackground -> VirtualBackgroundComponent(
                    onDismiss = onDismiss
                )

                ModalSheetComponent.Participants -> ParticipantsComponent(
                    onDismiss = onDismiss
                )
            }
            onComponentDisplayed(modalSheetComponent)
        }
    } else {
        onComponentDisplayed(null)
    }
}