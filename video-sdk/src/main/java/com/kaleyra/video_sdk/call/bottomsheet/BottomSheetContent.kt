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

package com.kaleyra.video_sdk.call.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.AudioOutputComponent
import com.kaleyra.video_sdk.call.callactions.CallActionsComponent
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Call Actions Component tag
 */
const val CallActionsComponentTag = "CallActionsComponentTag"

/**
 * Screen Share Component Tag
 */
const val ScreenShareComponentTag = "ScreenShareComponentTag"

/**
 * Audio Output Component tag
 */
const val AudioOutputComponentTag = "AudioOutputComponentTag"

/**
 * File Share Component tag
 */
const val FileShareComponentTag = "FileShareComponentTag"

/**
 * Whiteboard Component Tag
 */
const val WhiteboardComponentTag = "WhiteboardComponentTag"

/**
 * Virtual Background Component Tag
 */
const val VirtualBackgroundComponentTag = "VirtualBackgroundComponentTag"

@Immutable
internal enum class BottomSheetComponent {
    CallActions, AudioOutput, ScreenShare, FileShare, Whiteboard, VirtualBackground
}

@Immutable
internal data class BottomSheetContentState(
    val initialComponent: BottomSheetComponent,
    val initialLineState: LineState
) {

    var currentComponent: BottomSheetComponent by mutableStateOf(initialComponent)
        private set

    var currentLineState: LineState by mutableStateOf(initialLineState)
        private set

    fun navigateToComponent(component: BottomSheetComponent) {
        currentComponent = component
    }

    fun expandLine() {
        currentLineState = LineState.Expanded
    }

    fun collapseLine(color: Color? = null) {
        currentLineState = LineState.Collapsed(argbColor = color?.toArgb())
    }

    companion object {
        fun Saver(): Saver<BottomSheetContentState, *> = Saver(
            save = { Pair(it.currentComponent, it.currentLineState) },
            restore = { BottomSheetContentState(it.first, it.second) }
        )
    }
}

@Composable
internal fun rememberBottomSheetContentState(
    initialSheetComponent: BottomSheetComponent,
    initialLineState: LineState
) = rememberSaveable(saver = BottomSheetContentState.Saver()) {
    BottomSheetContentState(initialSheetComponent, initialLineState)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BottomSheetContent(
    contentState: BottomSheetContentState,
    modifier: Modifier = Modifier,
    onLineClick: () -> Unit,
    onCallActionClick: (CallAction) -> Unit,
    onAudioDeviceClick: () -> Unit,
    onScreenShareTargetClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    contentVisible: Boolean = true,
    isDarkTheme: Boolean = false,
    isTesting: Boolean = false
) {
    val whiteboardScope = rememberCoroutineScope()

    val whiteboardViewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestCollaborationViewModelConfiguration, WhiteboardView(LocalContext.current))
    )

    LaunchedEffect(whiteboardViewModel.uiState) {
        whiteboardViewModel.uiState.map { it.showingRequest }.filterNotNull().onEach { showingRequest ->
            val adminDisplayName = KaleyraVideo.conference.call.replayCache.firstOrNull()?.participants?.value?.list?.firstOrNull {
                it.userId == showingRequest.adminUserId
            }?.combinedDisplayName?.firstOrNull()

            when (showingRequest) {
                is Whiteboard.Event.Request.Show -> {
                    val displaySnackbar = contentState.currentComponent != BottomSheetComponent.Whiteboard
                    contentState.navigateToComponent(BottomSheetComponent.Whiteboard)
                    onCallActionClick(CallAction.Whiteboard())
                    if (!displaySnackbar) return@onEach
                    delay(500)
                    CallUserMessagesProvider.sendUserMessage(WhiteboardShowRequestMessage(adminDisplayName))
                }
                is Whiteboard.Event.Request.Hide -> {
                    if (contentState.currentComponent != BottomSheetComponent.Whiteboard) return@onEach
                    contentState.navigateToComponent(BottomSheetComponent.CallActions)
                    delay(500)
                    CallUserMessagesProvider.sendUserMessage(WhiteboardHideRequestMessage(adminDisplayName))
                }
            }
        }.launchIn(whiteboardScope)
    }

    Column(modifier) {
        Line(
            state = contentState.currentLineState,
            onClickLabel = stringResource(id = R.string.kaleyra_call_show_buttons),
            onClick = onLineClick
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
//            AnimatedContent(
//                targetState = contentState.currentComponent,
//                transitionSpec = {
//                    fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
//                },
//                label = "bottomSheetContent"
//            ) { target ->

                println("recomposition on target: ${contentState.currentComponent}")

                when (contentState.currentComponent) {
                    BottomSheetComponent.CallActions -> {
                        CallActionsComponent(
                            onItemClick = { action ->
                                contentState.navigateToComponent(
                                    component = when (action) {
                                        is CallAction.Audio -> BottomSheetComponent.AudioOutput
                                        is CallAction.ScreenShare -> BottomSheetComponent.ScreenShare
                                        is CallAction.FileShare -> BottomSheetComponent.FileShare
                                        is CallAction.Whiteboard -> {
                                            whiteboardScope.launch {
                                                KaleyraVideo.conference.call.firstOrNull()?.whiteboard?.load()
                                            }
                                            BottomSheetComponent.Whiteboard
                                        }
                                        is CallAction.VirtualBackground -> BottomSheetComponent.VirtualBackground
                                        else -> BottomSheetComponent.CallActions
                                    }
                                )
                                onCallActionClick(action)
                            },
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.testTag(CallActionsComponentTag)
                        )
                    }
                    BottomSheetComponent.AudioOutput -> {
                        AudioOutputComponent(
                            onDeviceConnected = onAudioDeviceClick,
                            onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                            modifier = Modifier.testTag(AudioOutputComponentTag),
                            isTesting = isTesting
                        )
                    }
                    BottomSheetComponent.ScreenShare -> {
                        ScreenShareComponent(
                            onItemClick = { onScreenShareTargetClick() },
                            onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                            modifier = Modifier.testTag(ScreenShareComponentTag)
                        )
                    }
                    BottomSheetComponent.FileShare -> {
                        FileShareComponent(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .testTag(FileShareComponentTag),
                            isTesting = isTesting
                        )
                    }
                    BottomSheetComponent.Whiteboard -> {
                        WhiteboardComponent(
                            viewModel = whiteboardViewModel,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .testTag(WhiteboardComponentTag)
                        )
                    }
                    BottomSheetComponent.VirtualBackground -> {
                        VirtualBackgroundComponent(
                            onItemClick = { onVirtualBackgroundClick() },
                            onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                            modifier = Modifier
                                .testTag(VirtualBackgroundComponentTag)
                        )
                    }
                }
            }
//        }
    }
}

