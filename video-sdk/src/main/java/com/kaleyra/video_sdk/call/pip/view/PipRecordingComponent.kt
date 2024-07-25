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

package com.kaleyra.video_sdk.call.pip.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.extensions.ModifierExtensions.pulse
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

internal const val RecordingDotTestTag = "RecordingDotTestTag"

@Composable
internal fun PipRecordingComponent(
    modifier: Modifier = Modifier,
    viewModel: CallAppBarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallAppBarViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRecording by remember { derivedStateOf { uiState.recording } }

    if (isRecording) {
        PipRecordingComponent(modifier)
    }
}

@Composable
internal fun PipRecordingComponent(modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = .35f),
        contentColor = Color.White,
        shape = RoundedCornerShape(5.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp, start = 2.dp, end = 4.dp)
        ) {
            RecordingDot()
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.kaleyra_call_info_rec).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecordingDot(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_kaleyra_recording_dot),
        contentDescription = null,
        tint = colorResource(id = R.color.kaleyra_recording_color),
        modifier = modifier
            .size(20.dp)
            .pulse()
            .testTag(RecordingDotTestTag)
    )
}

@Preview
@Composable
internal fun PipRecordingComponentPreview() {
    KaleyraM3Theme {
        PipRecordingComponent()
    }
}