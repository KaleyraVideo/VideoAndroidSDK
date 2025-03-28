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

package com.kaleyra.video_sdk.termsandconditions.screen

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.termsandconditions.model.TermsAndConditionsUiState
import com.kaleyra.video_sdk.termsandconditions.viewmodel.TermsAndConditionsViewModel
import com.kaleyra.video_sdk.theme.TermsAndConditionsTheme

internal const val TermsProgressIndicatorTag = "TermsProgressIndicatorTag"

@Composable
internal fun TermsAndConditionsScreen(
    viewModel: TermsAndConditionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TermsAndConditionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    title: String,
    message: String,
    acceptText: String,
    declineText: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TermsAndConditionsScreen(
        uiState = uiState,
        title = title,
        message = message,
        acceptText = acceptText,
        declineText = declineText,
        onAccept = onAccept,
        onDecline = remember(onDecline) {
            {
                onDecline()
                viewModel.decline()
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun TermsAndConditionsScreen(
    uiState: TermsAndConditionsUiState,
    title: String,
    message: String,
    acceptText: String,
    declineText: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()

    if (uiState.isDeclined || uiState.isConnected) {
        LaunchedEffect(Unit) {
            activity.finishAndRemoveTask()
        }
    }

    Surface(modifier) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            val (titleRef, messageRef, buttonsRef) = createRefs()

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleRef) {
                        top.linkTo(parent.top)
                    }
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .constrainAs(messageRef) {
                        top.linkTo(titleRef.bottom, margin = 24.dp)
                        bottom.linkTo(buttonsRef.top)
                        height = Dimension.fillToConstraints
                    }
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(buttonsRef) {
                        top.linkTo(messageRef.bottom, margin = 24.dp)
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                TextButton(
                    onClick = onDecline,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    content = { Text(text = declineText, style = MaterialTheme.typography.labelLarge) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                var loading by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        loading = true
                        onAccept()
                    },
                    modifier = Modifier.animateContentSize()
                ) {
                    if (!loading) {
                        Text(text = acceptText, style = MaterialTheme.typography.labelLarge)
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .testTag(TermsProgressIndicatorTag)
                        )
                    }
                }
            }
        }
    }

}

@DayModePreview
@NightModePreview
@Composable
internal fun TermsAndConditionsScreenPreview() {
    TermsAndConditionsTheme {
        TermsAndConditionsScreen(
            uiState = TermsAndConditionsUiState(),
            "Terms and Conditions",
            "Terms message ",
            "Accept",
            "Decline",
            {},
            {}
        )
    }
}