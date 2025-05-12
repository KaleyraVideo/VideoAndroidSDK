/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.chat.screen

import android.app.DownloadManager.EXTRA_DOWNLOAD_ID
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationExtra
import com.kaleyra.video_common_ui.notification.signature.EXTRA_SIGN_ID
import com.kaleyra.video_common_ui.notification.signature.SignatureNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.signature.SignatureNotificationExtra
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickBroadcastReceiver
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.viewmodel.SignDocumentsViewModel
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.viewmodel.SharedViewModelStore
import com.kaleyra.video_sdk.chat.appbar.view.GroupAppBar
import com.kaleyra.video_sdk.chat.appbar.view.OneToOneAppBar
import com.kaleyra.video_sdk.chat.conversation.ConversationComponent
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.scrollToBottomFabEnabled
import com.kaleyra.video_sdk.chat.conversation.view.ResetScrollFab
import com.kaleyra.video_sdk.chat.input.ChatUserInput
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.model.mockChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import com.kaleyra.video_sdk.common.usermessages.model.DownloadFileMessage
import com.kaleyra.video_sdk.common.usermessages.model.PinScreenshareMessage
import com.kaleyra.video_sdk.common.usermessages.model.SignatureMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.CollaborationTheme
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.launch

internal const val ConversationComponentTag = "ConversationComponentTag"

@Composable
internal fun ChatScreen(
    onBackPressed: () -> Unit,
    onChatDeleted: () -> Unit,
    onChatCreationFailed: () -> Unit,
    viewModel: PhoneChatViewModel,
    embedded: Boolean = false
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

//    if (!uiState.isUserConnected || uiState.connectionState is ConnectionState.Error) {
//        LaunchedEffect(Unit) {
//            activity.finishAndRemoveTask()
//        }
//    }

    CollaborationTheme(theme = theme) {
        ChatScreen(
            uiState = uiState,
            onBackPressed = onBackPressed,
            onChatDeleted = onChatDeleted,
            onChatCreationFailed = onChatCreationFailed,
            embedded = embedded,
            onMessageScrolled = viewModel::onMessageScrolled,
            onResetMessagesScroll = viewModel::onAllMessagesScrolled,
            onFetchMessages = viewModel::fetchMessages,
            onShowCall = viewModel::showCall,
            onSendMessage = viewModel::sendMessage,
            onTyping = viewModel::typing
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.Message) -> Unit,
    onResetMessagesScroll: () -> Unit,
    onFetchMessages: () -> Unit,
    onShowCall: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit,
    embedded: Boolean = false,
    onChatDeleted: () -> Unit,
    onChatCreationFailed: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val topBarRef = remember { FocusRequester() }
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val onMessageSent: ((String) -> Unit) = remember(scope, scrollState) {
        { text ->
            scope.launch {
                onSendMessage(text)
                scrollState.scrollToItem(0)
            }
        }
    }

    val fabRef = remember { FocusRequester() }
    val scrollToBottomFabEnabled by scrollToBottomFabEnabled(scrollState)
    val onFabClick = remember(scope, scrollState) {
        {
            scope.launch { scrollState.scrollToItem(0) }
            onResetMessagesScroll()
        }
    }
    var fabPadding by remember { mutableStateOf(0f) }
    var topAppBarPadding by remember { mutableStateOf(0f) }

    val chatUserInputContainerColor: Color by animateColorAsState(
        targetValue =
            if (scrollState.canScrollBackward) MaterialTheme.colorScheme.outline.copy(.16f)
            else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "chatUserInputContainerColor",
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    var isInCall by remember { mutableStateOf(uiState.isInCall) }
    isInCall = uiState.isInCall
    var snackBarHost: @Composable () -> Unit by remember(isInCall) { mutableStateOf({}) }

    if (isInCall) {
        val userMessagesViewModel: UserMessagesViewModel = SharedViewModelStore.getViewModel(
            kClass = UserMessagesViewModel::class,
            viewModelFactory = UserMessagesViewModel.provideFactory(LocalAccessibilityManager.current, ::requestCollaborationViewModelConfiguration),
            boundActivities = arrayOf(LocalContext.current.findActivity()::class.java.name)
        )
//        = viewModel(factory = UserMessagesViewModel.provideFactory(LocalAccessibilityManager.current, ::requestCollaborationViewModelConfiguration))
        val streamViewModel: StreamViewModel = viewModel(
            factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
        )
        val signDocumentsViewModel: SignDocumentsViewModel = viewModel(
            factory = SignDocumentsViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
        )
        val fileShareViewModel: FileShareViewModel = viewModel(
            factory = FileShareViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration, filePickProvider = FilePickBroadcastReceiver)
        )
        val onUserMessageActionClick: (UserMessage) -> Unit = remember(userMessagesViewModel) {
            { message: UserMessage ->
                var intentExtras: Bundle? = null
                when (message) {
                    is PinScreenshareMessage -> {
                        streamViewModel.pinStream(message.streamId, prepend = true, force = true)
                    }

                    is SignatureMessage.New -> {
                        if (signDocumentsViewModel.uiState.value.signDocuments.value.fastFilter { it.signState !is SignDocumentUi.SignStateUi.Completed }.size == 1) {
                            val signDocument = signDocumentsViewModel.uiState.value.signDocuments.value.first { it.id == message.signId }
                            signDocumentsViewModel.signDocument(signDocument)
                            intentExtras = Bundle().apply {
                                putString(SignatureNotificationExtra.NOTIFICATION_ACTION_EXTRA, SignatureNotificationActionReceiver.ACTION_SIGN)
                                putString(EXTRA_SIGN_ID, signDocument.id)
                            }
                        }
                    }

                    is DownloadFileMessage.New -> {
                        fileShareViewModel.download(message.downloadId)
                        intentExtras = Bundle().apply {
                            putString(FileShareNotificationExtra.NOTIFICATION_ACTION_EXTRA, FileShareNotificationActionReceiver.ACTION_DOWNLOAD)
                            putString(EXTRA_DOWNLOAD_ID, message.downloadId)
                        }
                    }

                    else -> Unit
                }
                KaleyraVideo.conference.call.replayCache.firstOrNull()?.show(intentExtras)
            }
        }

        snackBarHost = @Composable {
            StackedUserMessageComponent(viewModel = userMessagesViewModel, onActionClick = onUserMessageActionClick)
        }
    }

    Box(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets
                .navigationBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        )
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .semantics {
                    testTagsAsResourceId = true
                }
                .onPreviewKeyEvent {
                    return@onPreviewKeyEvent when {
                        it.type != KeyEventType.KeyDown && it.key == Key.DirectionLeft -> {
                            topBarRef.requestFocus(); true
                        }

                        scrollToBottomFabEnabled && it.type != KeyEventType.KeyDown && it.key == Key.DirectionRight -> {
                            fabRef.requestFocus(); true
                        }

                        else -> false
                    }
                },
            topBar = (@Composable {
                Column(
                    Modifier
                        .focusRequester(topBarRef)
                        .onGloballyPositioned {
                            topAppBarPadding = it.boundsInRoot().height
                        }) {

                    if (uiState.isInCall) {
                        OngoingCallLabel(onClick = onShowCall)
                    }

                    val topAppBarInsets =
                        if (!uiState.isInCall) TopAppBarDefaults.windowInsets else WindowInsets(0, 0, 0, 0)
                    when (uiState) {
                        is ChatUiState.OneToOne -> {
                            OneToOneAppBar(
                                connectionState = uiState.connectionState,
                                recipientDetails = uiState.recipientDetails,
                                scrollBehavior = scrollBehavior,
                                scrollState = scrollState,
                                windowInsets = topAppBarInsets,
                                isInCall = uiState.isInCall,
                                actions = uiState.actions,
                                onBackPressed = onBackPressed,
                            )
                        }

                        is ChatUiState.Group -> {
                            GroupAppBar(
                                image = uiState.image,
                                name = uiState.name.ifBlank { stringResource(R.string.kaleyra_chat_group_title) },
                                scrollBehavior = scrollBehavior,
                                scrollState = scrollState,
                                windowInsets = topAppBarInsets,
                                connectionState = uiState.connectionState,
                                participantsDetails = uiState.participantsDetails,
                                participantsState = uiState.participantsState,
                                isInCall = uiState.isInCall,
                                actions = uiState.actions,
                                onBackPressed = onBackPressed,
                            )
                        }
                    }
                }
            }).takeIf { !embedded } ?: {},
            floatingActionButton = {
                ResetScrollFab(
                    modifier = Modifier
                        .focusRequester(fabRef)
                        .graphicsLayer {
                            translationY = -fabPadding
                        },
                    counter = uiState.conversationState.unreadMessagesCount,
                    onClick = onFabClick,
                    enabled = scrollToBottomFabEnabled
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { paddingValues ->
            var stackedSnackBarTopPadding by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current
            Box {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .onGloballyPositioned { coordinates ->
                            stackedSnackBarTopPadding = with(density) { coordinates.boundsInRoot().top.toDp() }
                            coordinates.boundsInRoot().top
                        }
                ) {
                    ConversationComponent(
                        conversationState = uiState.conversationState,
                        participantsDetails = if (uiState is ChatUiState.Group) uiState.participantsDetails else null,
                        onMessageScrolled = onMessageScrolled,
                        onApproachingTop = onFetchMessages,
                        scrollState = scrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag(ConversationComponentTag)
                    )

                    HorizontalDivider(color = chatUserInputContainerColor)

                    if (uiState.hasFailedCreation) onChatCreationFailed()
                    else if (uiState.isDeleted) onChatDeleted()

                    val displayChatInput = !uiState.hasFailedCreation && !uiState.isDeleted

                    if (displayChatInput) {
                        ChatUserInput(
                            modifier = Modifier
                                .onGloballyPositioned {
                                    fabPadding = it.boundsInRoot().height
                                }
                                .navigationBarsPadding()
                                .let { if (!embedded) it.imePadding() else it },
                            onTextChanged = onTyping,
                            onMessageSent = onMessageSent,
                            onDirectionLeft = topBarRef::requestFocus
                        )
                    }
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = stackedSnackBarTopPadding)
                ) {
                    (snackBarHost.takeIf { !embedded } ?: {}).invoke()
                }
            }
        }
    }
}

@Composable
internal fun OngoingCallLabel(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val systemUiController = rememberSystemUiController()

    DisposableEffect(Unit) {
        val hasStatusBarDarkIcons = systemUiController.statusBarDarkContentEnabled
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = false)

        onDispose {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = hasStatusBarDarkIcons
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .fillMaxWidth()
            .background(
                shape = RectangleShape,
                color = KaleyraTheme.colors.positiveContainer
            )
            .highlightOnFocus(interactionSource)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_ongoing_call_label),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatScreenPreview() = KaleyraTheme {
    ChatScreen(
        uiState = mockChatUiState,
        onBackPressed = { },
        onMessageScrolled = { },
        onResetMessagesScroll = { },
        onFetchMessages = { },
        onShowCall = { },
        onSendMessage = { },
        onTyping = { },
        onChatCreationFailed = {},
        onChatDeleted = {},
    )
}
