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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.AudioOutputComponent
import com.kaleyra.video_sdk.call.callactions.CallActionsComponent
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.fileshare.FileShareComponent
import com.kaleyra.video_sdk.call.screenshare.ScreenShareComponent
import com.kaleyra.video_sdk.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.video_sdk.call.whiteboard.WhiteboardComponent

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

    var targetComponent: BottomSheetComponent by mutableStateOf(initialComponent)
        private set

    var currentLineState: LineState by mutableStateOf(initialLineState)
        private set

    fun navigateToComponent(component: BottomSheetComponent) {
        targetComponent = component
    }

    fun updateCurrentComponent(component: BottomSheetComponent) {
        if (targetComponent != component) return
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

@Composable
internal fun BottomSheetContent(
    contentState: BottomSheetContentState,
    modifier: Modifier = Modifier,
    onLineClick: () -> Unit,
    onCallActionClick: (CallAction) -> Unit,
    onAudioDeviceClick: () -> Unit,
    onScreenShareTargetClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    contentVisible: Boolean = true,
    isDarkTheme: Boolean = false,
    isTesting: Boolean = false
) {
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
            when (contentState.targetComponent) {
                BottomSheetComponent.CallActions -> {
                    CallActionsComponent(
                        onItemClick = { action ->
                            contentState.navigateToComponent(
                                component = when (action) {
                                    is CallAction.Audio -> BottomSheetComponent.AudioOutput
                                    is CallAction.ScreenShare -> BottomSheetComponent.ScreenShare
                                    is CallAction.FileShare -> BottomSheetComponent.FileShare
                                    is CallAction.Whiteboard -> BottomSheetComponent.Whiteboard
                                    is CallAction.VirtualBackground -> BottomSheetComponent.VirtualBackground
                                    else -> BottomSheetComponent.CallActions
                                }
                            )
                            onCallActionClick(action)
                        },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.testTag(CallActionsComponentTag)
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.CallActions)
                }

                BottomSheetComponent.AudioOutput -> {
                    AudioOutputComponent(
                        onDeviceConnected = onAudioDeviceClick,
                        onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                        modifier = Modifier.testTag(AudioOutputComponentTag),
                        isTesting = isTesting
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.AudioOutput)
                }

                BottomSheetComponent.ScreenShare -> {
                    ScreenShareComponent(
                        modifier = Modifier.testTag(ScreenShareComponentTag),
                        onItemClick = { onScreenShareTargetClick() },
                        onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                        onAskInputPermissions = onAskInputPermissions
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.ScreenShare)
                }

                BottomSheetComponent.FileShare -> {
                    FileShareComponent(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag(FileShareComponentTag),
                        isTesting = isTesting
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.FileShare)
                }

                BottomSheetComponent.Whiteboard -> {
                    WhiteboardComponent(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag(WhiteboardComponentTag)
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.Whiteboard)
                }

                BottomSheetComponent.VirtualBackground -> {
                    VirtualBackgroundComponent(
                        onItemClick = { onVirtualBackgroundClick() },
                        onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                        modifier = Modifier
                            .testTag(VirtualBackgroundComponentTag)
                    )
                    contentState.updateCurrentComponent(BottomSheetComponent.VirtualBackground)
                }
            }
        }
    }
}
