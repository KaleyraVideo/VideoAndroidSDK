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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.participants.ParticipantsComponent
import com.kaleyra.video_sdk.call.screen.view.ModalSheetComponent
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.chat.screen.ChatScreen
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage

private val SideBarBorderWidth = 1.dp
private val SideBarShape = RoundedCornerShape(size = 8.dp)

@Composable
internal fun SidePanel(
    modalSheetComponent: ModalSheetComponent,
    onDismiss: () -> Unit,
    onSideBarComponentDisplayed: (ModalSheetComponent?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .border(
                width = SideBarBorderWidth,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = SideBarShape
            )
    ) {

        AnimatedContent(
            targetState = modalSheetComponent,
            label = "side panel content"
        ) { target ->
            when (target) {

                ModalSheetComponent.FileShare -> FileShareComponent(
                    onDismiss = onDismiss,
                    isLargeScreen = true
                )

                ModalSheetComponent.Whiteboard -> WhiteboardComponent(
                    onDismiss = onDismiss,
                    isLargeScreen = true
                )

                ModalSheetComponent.Participants -> ParticipantsComponent(
                    onDismiss = onDismiss,
                    isLargeScreen = true
                )

                ModalSheetComponent.Chat -> ChatComponent(
                    onBackPressed = onDismiss,
                    onChatConfigurationFailure = onDismiss
                )

                else -> Unit
            }
            onSideBarComponentDisplayed(modalSheetComponent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatComponent(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onChatConfigurationFailure: () -> Unit,
    mainViewModel: MainViewModel = viewModel(factory = MainViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    chatViewModel: PhoneChatViewModel = viewModel(factory = PhoneChatViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
) {
    Column(modifier.fillMaxSize()) {
        LaunchedEffect(Unit) {
            val loggerUserId = chatViewModel.getLoggedUserId()
            val otherUserId = mainViewModel.getOtherUserId()
            if (loggerUserId == null || otherUserId == null) onChatConfigurationFailure()
            else chatViewModel.setChat(loggedUserId = loggerUserId, userId = otherUserId)
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
            onBackPressed = onBackPressed
        )
    }
}