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

package com.kaleyra.video_sdk.call.callinfo.view

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.extensions.TextStyleExtensions.shadow
import com.kaleyra.video_sdk.theme.KaleyraTheme

const val CallInfoTitleTestTag = "CallInfoTitleTestTag"
const val CallInfoSubtitleTestTag = "CallInfoSubtitleTestTag"

@Composable
fun CallInfoComponent(
    modifier: Modifier = Modifier,
    viewModel: CallInfoViewModel = viewModel(factory = CallInfoViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    isPipMode: Boolean = false
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CallInfoComponent(
        modifier = modifier,
        callInfoUiState = uiState,
        isPipMode = isPipMode
    )
}

@Composable
fun CallInfoComponent(
    modifier: Modifier = Modifier,
    callInfoUiState: CallInfoUiState,
    isPipMode: Boolean = false
) {
    val callDisplayState = callInfoUiState.displayState?.resolve(LocalContext.current) ?: ""
    val callee = callInfoUiState.displayNames.value.joinToString(", ")

    var displayTitle: String? = null
    var displaySubtitle: String? = null

    when (callInfoUiState.callStateUi) {
        CallStateUi.Connecting,
        CallStateUi.Dialing,
        CallStateUi.Ringing,
        CallStateUi.RingingRemotely -> {
            if (callInfoUiState.displayNames.isEmpty()) displayTitle = callDisplayState
            else {
                displayTitle = callee
                displaySubtitle = callDisplayState
            }
        }

        CallStateUi.Connected, CallStateUi.Disconnecting -> {
            displayTitle = ""
            displaySubtitle = ""
        }

        CallStateUi.Reconnecting -> {
            displayTitle = callDisplayState
            displaySubtitle = ""
        }

        CallStateUi.Disconnected.Ended.Declined,
        CallStateUi.Disconnected.Ended.LineBusy,
        CallStateUi.Disconnected.Ended.CurrentUserInAnotherCall,
        CallStateUi.Disconnected.Ended.Timeout,
        CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice,

        CallStateUi.Disconnected.Ended.Error,
        CallStateUi.Disconnected.Ended.Error.Server,
        CallStateUi.Disconnected.Ended.Error.Unknown,
        CallStateUi.Disconnected.Ended,
        CallStateUi.Disconnected.Ended.HungUp,
        is CallStateUi.Disconnected.Ended.Kicked -> {
            displayTitle = LocalContext.current.resources.getString(R.string.kaleyra_strings_info_call_ended)
            displaySubtitle = callDisplayState
        }

        else -> Unit
    }

    if (callInfoUiState.callStateUi != null && (!displayTitle.isNullOrEmpty() || !displaySubtitle.isNullOrEmpty())) {
        Column(
            modifier = Modifier.then(modifier).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            displayTitle?.takeIf { it.isNotEmpty() }?.let {
                val titleTextStyle = (if (isPipMode) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge)
                    .shadow(color = MaterialTheme.colorScheme.surface)
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .basicMarquee()
                        .testTag(CallInfoTitleTestTag),
                    textAlign = if (isPipMode) TextAlign.Start else TextAlign.Center,
                    maxLines = 1,
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = titleTextStyle.fontSize,
                    fontWeight = titleTextStyle.fontWeight ?: FontWeight.Normal,
                    style = titleTextStyle
                )
            }

            Spacer(Modifier.size(4.dp))

            displaySubtitle?.takeIf { it.isNotEmpty() }?.let {
                val subtitleTextStyle = (if (isPipMode) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium)
                    .shadow(color = MaterialTheme.colorScheme.surface)
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag(CallInfoSubtitleTestTag),
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = if (isPipMode) TextAlign.Start else TextAlign.Center,
                    maxLines = 1,
                    style = subtitleTextStyle
                )
            }
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun CallInfoConnectingWithDisplayNames() {
    KaleyraTheme {
        Surface {
            CallInfoComponent(
                modifier = Modifier.background(color = Color.Blue),
                callInfoUiState = CallInfoUiState(
                    callStateUi = CallStateUi.Connecting,
                    displayState = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting),
                    displayNames = ImmutableList(listOf("Fede", "Kri", "Ste"))
                )
            )
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun CallInfoConnectingWithNoDisplayNames() {
    KaleyraTheme {
        Surface(modifier = Modifier) {
            CallInfoComponent(
                modifier = Modifier.background(color = Color.Blue),
                callInfoUiState = CallInfoUiState(
                    callStateUi = CallStateUi.Connecting,
                    displayState = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting),
                    displayNames = ImmutableList()
                )
            )
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun CallInfoComponentHidden() {
    KaleyraTheme {
        Surface {
            CallInfoComponent(
                modifier = Modifier.background(color = Color.Blue),
                callInfoUiState = CallInfoUiState()
            )
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun CallInfoPipMode() {
    KaleyraTheme {
        Surface {
            CallInfoComponent(
                modifier = Modifier.background(color = Color.Blue),
                callInfoUiState = CallInfoUiState(
                    callStateUi = CallStateUi.Connecting,
                    displayState = TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting),
                    displayNames = ImmutableList(listOf("Fede", "Kri", "Ste"))
                ),
                isPipMode = true
            )
        }
    }
}