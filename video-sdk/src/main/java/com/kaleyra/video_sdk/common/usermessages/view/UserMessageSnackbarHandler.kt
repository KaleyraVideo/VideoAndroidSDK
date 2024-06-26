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

package com.kaleyra.video_sdk.common.usermessages.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.common.snackbar.AudioOutputGenericFailureSnackbar
import com.kaleyra.video_sdk.common.snackbar.AudioOutputInSystemCallFailureSnackbar
import com.kaleyra.video_sdk.common.snackbar.CameraRestrictionSnackbar
import com.kaleyra.video_sdk.common.snackbar.MutedSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingEndedSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingErrorSnackbar
import com.kaleyra.video_sdk.common.snackbar.RecordingStartedSnackbar
import com.kaleyra.video_sdk.common.snackbar.UsbConnectedSnackbar
import com.kaleyra.video_sdk.common.snackbar.UsbDisconnectedSnackbar
import com.kaleyra.video_sdk.common.snackbar.UsbNotSupportedSnackbar
import com.kaleyra.video_sdk.common.snackbar.WhiteboardHideRequestSnackbar
import com.kaleyra.video_sdk.common.snackbar.WhiteboardShowRequestSnackbar
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull

internal const val RecordingStarted = "RecordingStarted"
internal const val RecordingStopped = "RecordingStopped"
internal const val RecordingError = "RecordingError"
internal const val WhiteboardShowRequest = "WhiteboardShowRequest"
internal const val WhiteboardHideRequest = "WhiteboardHideRequest"
internal const val UsbConnected = "UsbConnected"
internal const val UsbDisconnected = "UsbDisconnected"
internal const val UsbNotSupported = "UsbNotSupported"
internal const val CameraRestriction = "CameraRestriction"
internal const val AudioOutputGenericFailure = "AudioOutputGenericFailure"
internal const val AudioOutputInSystemCallFailure = "AudioOutputInSystemCallFailure"
internal const val MutedByAdmin = "MutedByAdmin"

@Composable
internal fun UserMessageSnackbarHandler(
    userMessage: UserMessage?,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val updatedState by rememberUpdatedState(userMessage)
    LaunchedEffect(snackbarHostState) {
        snapshotFlow { updatedState }
            .filterNotNull()
            .buffer()
            .collect {
                snackbarHostState.showSnackbar(
                    message = when (it) {
                        is MutedMessage -> it.admin ?: ""
                        is UsbCameraMessage.Connected -> it.name
                        is WhiteboardShowRequestMessage -> it.username ?: ""
                        is WhiteboardHideRequestMessage -> it.username ?: ""
                        else -> ""
                    },
                    actionLabel = when (it) {
                        is RecordingMessage.Started -> RecordingStarted
                        is RecordingMessage.Stopped -> RecordingStopped
                        is RecordingMessage.Failed -> RecordingError
                        is UsbCameraMessage.Connected -> UsbConnected
                        is UsbCameraMessage.Disconnected -> UsbDisconnected
                        is UsbCameraMessage.NotSupported -> UsbNotSupported
                        is CameraRestrictionMessage -> CameraRestriction
                        is AudioConnectionFailureMessage.Generic -> AudioOutputGenericFailure
                        is AudioConnectionFailureMessage.InSystemCall -> AudioOutputInSystemCallFailure
                        is MutedMessage -> MutedByAdmin
                        is WhiteboardShowRequestMessage -> WhiteboardShowRequest
                        is WhiteboardHideRequestMessage -> WhiteboardHideRequest
                        else -> null
                    }
                )
            }
    }

    SnackbarHost(
        modifier = modifier.padding(vertical = 12.dp),
        hostState = snackbarHostState,
        snackbar = {
            when (it.actionLabel) {
                RecordingStarted -> RecordingStartedSnackbar()
                RecordingStopped -> RecordingEndedSnackbar()
                RecordingError -> RecordingErrorSnackbar()
                UsbConnected -> UsbConnectedSnackbar(it.message)
                UsbDisconnected -> UsbDisconnectedSnackbar()
                UsbNotSupported -> UsbNotSupportedSnackbar()
                CameraRestriction -> CameraRestrictionSnackbar()
                AudioOutputGenericFailure -> AudioOutputGenericFailureSnackbar()
                AudioOutputInSystemCallFailure -> AudioOutputInSystemCallFailureSnackbar()
                MutedByAdmin -> MutedSnackbar(it.message)
                WhiteboardShowRequest -> WhiteboardShowRequestSnackbar(it.message.takeIf { it.isNotBlank() })
                WhiteboardHideRequest -> WhiteboardHideRequestSnackbar(it.message.takeIf { it.isNotBlank() })
            }
        }
    )
}
