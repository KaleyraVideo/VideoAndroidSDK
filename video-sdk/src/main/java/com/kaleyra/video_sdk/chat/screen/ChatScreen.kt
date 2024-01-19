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

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
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
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.CollaborationM3Theme
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

internal const val ConversationComponentTag = "ConversationComponentTag"

@Composable
internal fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: PhoneChatViewModel
) {
    val activity = LocalContext.current.findActivity()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(initialValue = null)

    if (!uiState.isUserConnected || uiState.connectionState is ConnectionState.Error) {
        LaunchedEffect(Unit) {
            activity.finishAndRemoveTask()
        }
    }

    CollaborationM3Theme(theme = theme) {
        ChatScreen(
            uiState = uiState,
            userMessage = userMessage,
            onBackPressed = onBackPressed,
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
    userMessage: UserMessage? = null,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.Message) -> Unit,
    onResetMessagesScroll: () -> Unit,
    onFetchMessages: () -> Unit,
    onShowCall: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val topBarRef = remember { FocusRequester() }
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by isKeyboardOpen()
    if (!isKeyboardOpen) focusManager.clearFocus()

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

    Box(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets
                .navigationBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        )
    ) {
        Scaffold(
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
            topBar = {
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
                        if (!uiState.isInCall) TopAppBarDefaults.windowInsets else WindowInsets(
                            0,
                            0,
                            0,
                            0
                        )
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
            },
            snackbarHost = {
                UserMessageSnackbarHandler(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = topAppBarPadding
                        },
                    userMessage = userMessage
                )
            },
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
            contentWindowInsets = ScaffoldDefaults
                .contentWindowInsets
                .exclude(WindowInsets.navigationBars)
                .exclude(WindowInsets.ime),
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
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

                ChatUserInput(
                    onTextChanged = onTyping,
                    onMessageSent = onMessageSent,
                    onDirectionLeft = topBarRef::requestFocus,
                    modifier = Modifier
                        .onGloballyPositioned {
                            fabPadding = it.boundsInRoot().height
                        }
                        .navigationBarsPadding()
                        .imePadding()
                )
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
                color = colorResource(id = R.color.kaleyra_color_answer_button)
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

@Composable
fun isKeyboardOpen(): State<Boolean> {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    return remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatScreenPreview() = KaleyraM3Theme {
    ChatScreen(
        uiState = mockChatUiState,
        onBackPressed = { },
        onMessageScrolled = { },
        onResetMessagesScroll = { },
        onFetchMessages = { },
        onShowCall = { },
        onSendMessage = { },
        onTyping = { },
        userMessage = RecordingMessage.Started,
    )
}
