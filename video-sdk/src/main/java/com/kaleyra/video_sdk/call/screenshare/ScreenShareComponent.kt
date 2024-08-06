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

package com.kaleyra.video_sdk.call.screenshare

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.view.ScreenShareContent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun ScreenShareComponent(
    modifier: Modifier = Modifier,
    viewModel: ScreenShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScreenShareViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onAskInputPermissions: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onClick = remember {
        { target: ScreenShareTargetUi ->
            when (target) {
                ScreenShareTargetUi.Application -> viewModel.shareApplicationScreen(activity, {}, {})
                ScreenShareTargetUi.Device -> {
                    onAskInputPermissions(true)
                    activity.unlockDevice(
                        onUnlocked = {
                            viewModel.shareDeviceScreen(
                                activity,
                                onScreenSharingStarted = {
                                    onAskInputPermissions(false)
                                },
                                onScreenSharingAborted = {
                                    onAskInputPermissions(false)
                                }
                            )
                        },
                        onDismiss = {
                            onDismiss()
                            onAskInputPermissions(false)
                        })
                }
            }
            onDismiss()
        }
    }
    ScreenShareComponent(
        uiState = uiState,
        onItemClick = onClick,
        onCloseClick = onDismiss,
        modifier = modifier
    )
}

@Composable
internal fun ScreenShareComponent(
    uiState: ScreenShareUiState,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        modifier = Modifier.padding(top = 16.dp).then(modifier),
        title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
        onCloseClick = onCloseClick,
    ) {
        ScreenShareContent(
            items = uiState.targetList,
            onItemClick = onItemClick
        )
    }
}

@MultiConfigPreview
@Composable
internal fun ScreenShareComponentPreview() {
    KaleyraM3Theme {
        Surface {
            ScreenShareComponent(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}
