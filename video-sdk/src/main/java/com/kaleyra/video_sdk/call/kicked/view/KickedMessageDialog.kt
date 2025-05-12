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

package com.kaleyra.video_sdk.call.kicked.view

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.kicked.viewmodel.KickedMessageViewModel
import com.kaleyra.video_sdk.extensions.ActivityComponentExtensions.isAtLeastResumed
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.delay

private const val AutoDismissMs = 3000L

@Composable
fun KickedMessageDialog(
    viewModel: KickedMessageViewModel = viewModel(factory = KickedMessageViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    onDismiss: () -> Unit) {

    val kickedUiState = viewModel.uiState.collectAsStateWithLifecycle()

    KickedMessageDialog(
        kickedUiState = kickedUiState.value,
        onDismiss = onDismiss
    )
}

@Composable
fun KickedMessageDialog(kickedUiState: KickedMessageUiState, onDismiss: () -> Unit) {
    if (kickedUiState is KickedMessageUiState.Hidden) return
    val activity = LocalContext.current.findActivity() as ComponentActivity
    if (!activity.isAtLeastResumed()) return
    kickedUiState as KickedMessageUiState.Display

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f))
    ) {
        Dialog(onDismissRequest = onDismiss) {

            LaunchedEffect(Unit) {
                delay(AutoDismissMs)
                onDismiss()
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = stringResource(id = R.string.kaleyra_strings_action_dismiss),
                        role = Role.Button,
                        onClick = onDismiss
                    )
            ) {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.kaleyra_call_participant_removed,
                        count = if (kickedUiState.adminName?.isNotBlank() == true) 1 else 0,
                        kickedUiState.adminName ?: ""),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
internal fun KickedMessagePreview() = KaleyraTheme {
    KickedMessageDialog {

    }
}