package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.signature.SignDocumentsComponent
import com.kaleyra.video_sdk.call.signature.SignDocumentViewComponent
import com.kaleyra.video_sdk.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import kotlinx.coroutines.launch

internal val CallScreenModalSheetTag = "CallScreenModalSheetTag"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreenModalSheet(
    modularComponent: ModularComponent?,
    sheetState: SheetState,
    onRequestDismiss: () -> Unit,
    onRequestOtherModularComponent: (ModularComponent) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onUserMessageActionClick: (UserMessage) -> Unit,
    modifier: Modifier = Modifier,
    onComponentDisplayed: (ModularComponent?) -> Unit = {},
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    isTesting: Boolean = false,
) {
    var currentModularComponent by remember { mutableStateOf(modularComponent) }
    currentModularComponent = modularComponent

    var displaySignDocumentsOnSignDocumentViewDismiss by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val onDismiss: (ModularComponent) -> Unit = remember(sheetState) {
        { dismissingComponent ->
            // Check the lifecycle state to prevent the modal bottom sheet from dismissing when entering the PiP mode
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                scope.launch {
                    if (currentModularComponent == dismissingComponent) {
                        sheetState.hide()
                        onRequestDismiss()
                    }
                }
            }
        }
    }

    if (modularComponent != null) {
        ModalBottomSheet(
            onDismissRequest = onRequestDismiss,
            shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
            sheetState = sheetState,
            dragHandle = (@Composable { CallBottomSheetDefaults.HDragHandle() }).takeIf { !isFullScreenComponent(modularComponent) },
            contentWindowInsets = { WindowInsets(0.dp) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = modifier.testTag(CallScreenModalSheetTag)
        ) {
            when (modularComponent) {
                ModularComponent.Audio -> AudioOutputComponent(onDismiss = { onDismiss(ModularComponent.Audio) })

                ModularComponent.ScreenShare -> ScreenShareComponent(
                    onDismiss = { onDismiss(ModularComponent.ScreenShare) },
                    onAskInputPermissions = onAskInputPermissions
                )

                ModularComponent.FileShare -> FileShareComponent(
                    onDismiss = { onDismiss(ModularComponent.FileShare) },
                    onUserMessageActionClick = onUserMessageActionClick,
                    isTesting = isTesting
                )

                ModularComponent.Whiteboard -> WhiteboardComponent(
                    onDismiss = { onDismiss(ModularComponent.Whiteboard) },
                    onUserMessageActionClick = onUserMessageActionClick
                )

                ModularComponent.SignDocuments -> SignDocumentsComponent(
                    onDismiss = {
                        if (!displaySignDocumentsOnSignDocumentViewDismiss) onDismiss(ModularComponent.SignDocuments)
                    },
                    onSignDocumentSelected = {
                        displaySignDocumentsOnSignDocumentViewDismiss = true
                        onRequestOtherModularComponent(ModularComponent.SignDocumentView)
                    },
                    onUserMessageActionClick = onUserMessageActionClick,
                    isTesting = isTesting
                )

                ModularComponent.SignDocumentView -> {
                    val onSignDocumentClosed = remember(displaySignDocumentsOnSignDocumentViewDismiss) {
                        {
                            if (displaySignDocumentsOnSignDocumentViewDismiss) onRequestOtherModularComponent(ModularComponent.SignDocuments)
                            else onDismiss(ModularComponent.SignDocumentView)
                            displaySignDocumentsOnSignDocumentViewDismiss = false
                        }
                    }
                    SignDocumentViewComponent(
                        onDocumentSigned = onSignDocumentClosed,
                        onBackPressed = onSignDocumentClosed,
                        onUserMessageActionClick = onUserMessageActionClick,
                        isTesting = isTesting
                    )
                }

                ModularComponent.VirtualBackground -> VirtualBackgroundComponent(
                    onDismiss = { onDismiss(ModularComponent.VirtualBackground) }
                )

                ModularComponent.Participants -> ParticipantsComponent(
                    onDismiss = { onDismiss(ModularComponent.Participants) }
                )

                ModularComponent.Chat -> Unit
            }
            onComponentDisplayed(modularComponent)
        }
    } else {
        onComponentDisplayed(null)
    }
}

private fun isFullScreenComponent(component: ModularComponent): Boolean {
    return component == ModularComponent.FileShare
        || component == ModularComponent.Participants
        || component == ModularComponent.Whiteboard
        || component == ModularComponent.SignDocuments
        || component == ModularComponent.SignDocumentView
}