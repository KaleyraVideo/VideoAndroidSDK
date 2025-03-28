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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.call.feedback.view

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.feedbackUiValueFor
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.toRatingStringRes
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiMapping.toSliderValue
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo

/**
 * Feedback Form Tag
 */
const val FeedbackFormTag = "FeedbackFormTag"

@Composable
internal fun FeedbackForm(
    feedbackUiState: FeedbackUiState? = FeedbackUiState.Display(),
    onUserFeedback: (FeedbackUiRating, String) -> Unit,
    onDismiss: () -> Unit) {
    if (feedbackUiState == null || feedbackUiState is FeedbackUiState.Hidden) return
    feedbackUiState as FeedbackUiState.Display
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = feedbackUiState.comment ?: "")) }
    var isEditTextFocused by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(feedbackUiState.rating) }
    val orientation = LocalConfiguration.current.orientation
    val windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag(FeedbackFormTag),
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(visible = !isEditTextFocused) {
            Text(
                text = stringResource(id = R.string.kaleyra_feedback_evaluate_call),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        }
        if (!isEditTextFocused) Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = composableRatingTextFor(feedbackUiRating = sliderValue!!),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(10.dp))
        StarSlider(
            value = composableSliderValueFor(sliderValue!!),
            onValueChange = { sliderValue = feedbackUiValueFor(it) },
            levels = FeedbackUiRating.entries.count(),
        )

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            placeholder = {
                if (textFieldValue.text.isBlank()) {
                    Text(
                        text = stringResource(id = R.string.kaleyra_feedback_leave_a_comment),
                        fontSize = 14.sp,
                    )
                }
            },
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .fillMaxWidth()
                .onFocusChanged {
                    isEditTextFocused = it.hasFocus
                }
                .animateContentSize(),
            maxLines = when {
                orientation == Configuration.ORIENTATION_LANDSCAPE && windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> 1
                else -> 4
            },
            minLines = when {
                orientation == Configuration.ORIENTATION_LANDSCAPE && windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> 1
                isEditTextFocused -> 4
                else -> 1
            },
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 12.dp),
                content = {
                    Text(
                        text = stringResource(id = R.string.kaleyra_action_cancel),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
            )
            Button(
                onClick = { onUserFeedback(sliderValue!!, textFieldValue.text) },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = stringResource(id = R.string.kaleyra_feedback_vote),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
internal fun composableRatingTextFor(feedbackUiRating: FeedbackUiRating): String {
    val stringId = remember(feedbackUiRating) {
        feedbackUiRating.toRatingStringRes()
    }
    return stringResource(id = stringId)
}

@Composable
internal fun composableSliderValueFor(feedbackUiRating: FeedbackUiRating): Float {
    val sliderValueFloat = remember(feedbackUiRating) {
        feedbackUiRating.toSliderValue()
    }
    return sliderValueFloat
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FeedbackFormPreview() = KaleyraTheme {
    Surface {
        FeedbackForm(
            FeedbackUiState.Display(comment = "comment this call", rating = FeedbackUiRating.Good),
            { _, _ -> }, {}
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FeedbackFormDefaultRatingPreview() = KaleyraTheme {
    Surface {
        FeedbackForm(
            FeedbackUiState.Display(),
            { _, _ -> }, {}
        )
    }
}
