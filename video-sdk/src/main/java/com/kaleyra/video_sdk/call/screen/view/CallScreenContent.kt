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

package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kaleyra.video_sdk.call.dialing.DialingComponent
import com.kaleyra.video_sdk.call.ringing.RingingComponent
import com.kaleyra.video_sdk.call.screen.model.CallStateUi

/**
 * Composable function used to render the call screen component
 * @param callState CallStateUi call state ui
 * @param maxWidth Dp max width in dp
 * @param onBackPressed Function0<Unit> callback invoked when the back button is pressed
 * @param onStreamFullscreenClick Function1<String?, Unit> callback invoked when a fullscreen button is clicked
 * @param shouldShowUserMessages Boolean flag indicating whether to show user messages, true to show them, false otherwise
 * @param isDarkTheme Boolean flag indicating if the dark theme is enabled, true if enabled, false otherwise
 * @param modifier Modifier compose modifier
 */
@Composable
internal fun CallScreenContent(
    callState: CallStateUi,
    maxWidth: Dp,
    onBackPressed: () -> Unit,
    onStreamFullscreenClick: (String?) -> Unit,
    shouldShowUserMessages: Boolean = true,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        val targetState by remember(callState) {
            derivedStateOf {
                when (callState) {
                    is CallStateUi.Ringing -> 0
                    CallStateUi.Dialing -> 1
                    else -> 2
                }
            }
        }
        when (targetState) {
            0 -> RingingComponent(onBackPressed = onBackPressed, isDarkTheme = isDarkTheme)
            1 -> DialingComponent(onBackPressed = onBackPressed)
            else -> CallComponent(
                shouldShowUserMessages = shouldShowUserMessages,
                maxWidth = maxWidth,
                onBackPressed = onBackPressed,
                onStreamFullscreenClick = onStreamFullscreenClick
            )
        }
    }
}
