package com.kaleyra.video_sdk.call.screen.view.vcallscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.participants.ParticipantsComponent
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.call.signature.SignDocumentsComponent
import com.kaleyra.video_sdk.call.signature.SignDocumentViewComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.chat.screen.ChatScreen
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import kotlinx.coroutines.launch

internal val SideBarBorderWidth = 1.dp
internal val SidePanelPadding = 4.dp
internal val SideBarShape = RoundedCornerShape(size = 8.dp)

internal val SidePanelTag = "SidePanelTag"

@Composable
internal fun SidePanel(
    modularComponent: ModularComponent,
    onDismiss: () -> Unit,
    onRequestOtherModularComponent: (ModularComponent) -> Unit,
    onChatDeleted: () -> Unit,
    onChatCreationFailed: () -> Unit,
    onComponentDisplayed: (ModularComponent?) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    modifier: Modifier = Modifier
) {
    var currentModularComponent by remember { mutableStateOf(modularComponent) }
    currentModularComponent = modularComponent

    var displaySignDocumentsOnSignDocumentViewDismiss by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val onDismiss: (ModularComponent) -> Unit = remember(modularComponent) {
        { dismissingComponent ->
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                scope.launch {
                    if (currentModularComponent == dismissingComponent) {
                        onDismiss()
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .padding(SidePanelPadding)
            .border(
                width = SideBarBorderWidth,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = SideBarShape
            )
            .testTag(SidePanelTag)
    ) {

        AnimatedContent(
            targetState = modularComponent,
            label = "side panel content"
        ) { target ->
            when (target) {

                ModularComponent.FileShare -> FileShareComponent(
                    onDismiss = { onDismiss(ModularComponent.FileShare) },
                    isLargeScreen = true
                )

                ModularComponent.Whiteboard -> WhiteboardComponent(
                    onDismiss = { onDismiss(ModularComponent.Whiteboard) },
                    isLargeScreen = true
                )

                ModularComponent.SignDocuments -> SignDocumentsComponent(
                    onDismiss = {
                        if (!displaySignDocumentsOnSignDocumentViewDismiss) onDismiss(ModularComponent.SignDocuments)
                    },
                    onSignDocumentSelected = {
                        displaySignDocumentsOnSignDocumentViewDismiss = true
                        onRequestOtherModularComponent(ModularComponent.SignDocumentView)
                    },
                    isLargeScreen = true
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
                        isLargeScreen = true
                    )
                }

                ModularComponent.Participants -> ParticipantsComponent(
                    onDismiss = { onDismiss(ModularComponent.Participants) },
                    isLargeScreen = true
                )

                ModularComponent.Chat -> ChatComponent(
                    onBackPressed = { onDismiss(ModularComponent.Chat) },
                    onChatConfigurationFailure = { onDismiss(ModularComponent.Chat) },
                    onChatDeleted = onChatDeleted,
                    onChatCreationFailed = onChatCreationFailed,
                )

                else -> Unit
            }
            onComponentDisplayed(modularComponent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatComponent(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onChatDeleted: () -> Unit,
    onChatCreationFailed: () -> Unit,
    onChatConfigurationFailure: () -> Unit,
    mainViewModel: MainViewModel = viewModel(factory = MainViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    chatViewModel: PhoneChatViewModel = viewModel(factory = PhoneChatViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
) {
    Column(modifier.fillMaxSize()) {
        LaunchedEffect(Unit) {
            val loggerUserId = chatViewModel.getLoggedUserId()
            val chatId = mainViewModel.getChatId()
            if (loggerUserId == null || chatId == null) onChatConfigurationFailure()
            else chatViewModel.setChat(loggerUserId, chatId)
        }
        chatViewModel.connectedUser

        ComponentAppBar(
            onBackPressed = onBackPressed,
            title = stringResource(id = R.string.kaleyra_chat),
            actions = { Spacer(Modifier.width(56.dp)) },
            isLargeScreen = true,
            modifier = modifier
        )
        ChatScreen(
            viewModel = chatViewModel,
            embedded = true,
            onBackPressed = onBackPressed,
            onChatDeleted = onChatDeleted,
            onChatCreationFailed = onChatCreationFailed
        )
    }
}