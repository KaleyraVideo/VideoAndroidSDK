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

package com.kaleyra.video_sdk.call.callactions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.model.mockCallActions
import com.kaleyra.video_sdk.call.callactions.view.CallActionsContent
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun CallActionsComponent(
    viewModel: CallActionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration,)
    ),
    onItemClick: (action: CallAction) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallActionsComponent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onItemClick = remember(onItemClick) {
            { action ->
                when (action) {
                    is CallAction.Microphone -> viewModel.toggleMic(activity)
                    is CallAction.Camera -> viewModel.toggleCamera(activity)
                    is CallAction.SwitchCamera -> viewModel.switchCamera()
                    is CallAction.HangUp -> viewModel.hangUp()
                    is CallAction.ScreenShare -> {
                        if (!viewModel.tryStopScreenShare()) {
                            onItemClick(action)
                        }
                    }
                    is CallAction.Chat -> {
                        activity.unlockDevice(onUnlocked = {
                            viewModel.showChat(activity.baseContext)
                        })
                    }
                    else -> onItemClick(action)
                }
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun CallActionsComponent(
    uiState: CallActionsUiState,
    onItemClick: (action: CallAction) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        CallActionsContent(
            items = uiState.actionList,
            itemsPerRow = uiState.actionList.count().coerceIn(1, 4),
            isDarkTheme = isDarkTheme,
            onItemClick = onItemClick
        )
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsComponentPreview() {
    KaleyraTheme {
        Surface {
            CallActionsComponent(
                uiState = CallActionsUiState(actionList = mockCallActions),
                onItemClick = { }
            )
        }
    }
}