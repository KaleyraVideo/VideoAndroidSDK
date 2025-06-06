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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
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
    var sliderValue by remember(feedbackUiState) { mutableFloatStateOf(feedbackUiState.rating.toSliderValue()) }
    val sliderTextStringRes by remember(feedbackUiState) {
        derivedStateOf {
            feedbackUiValueFor(sliderValue).toRatingStringRes()
        }
    }
    val orientation = LocalConfiguration.current.orientation
    val windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current)
    val sliderLevels = remember { FeedbackUiRating.entries.count() }
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
            text = stringResource(sliderTextStringRes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(10.dp))
        StarSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            levels = sliderLevels,
            modifier = Modifier
                .onKeyEvent { keyEvent ->
                    // Implement keyboard accessibility for slider value adjustments
                    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (keyEvent.key) {
                        Key.DirectionRight -> {
                            sliderValue = (sliderValue + 1).coerceIn(1f, sliderLevels.toFloat())
                            true
                        }
                        Key.DirectionLeft -> {
                            sliderValue = (sliderValue - 1).coerceIn(1f, sliderLevels.toFloat())
                            true
                        }
                        else -> false
                    }
                }
        )

        val textFieldInteractionSource = remember { MutableInteractionSource() }
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
            interactionSource = textFieldInteractionSource,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp)
                .fillMaxWidth()
                .onFocusChanged {
                    isEditTextFocused = it.hasFocus
                }
                .highlightOnFocus(textFieldInteractionSource)
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
            horizontalArrangement = Arrangement.End
        ) {
            val negativeButtonInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = { onDismiss() },
                interactionSource = negativeButtonInteractionSource,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .highlightOnFocus(negativeButtonInteractionSource),
                content = {
                    Text(
                        text = stringResource(id = R.string.kaleyra_action_cancel),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
            )
            Spacer(Modifier.width(12.dp))
            val positiveButtonInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = { onUserFeedback(feedbackUiValueFor(sliderValue), textFieldValue.text) },
                shape = RoundedCornerShape(4.dp),
                interactionSource = positiveButtonInteractionSource,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.highlightOnFocus(positiveButtonInteractionSource)
            ) {
                Text(
                    text = stringResource(id = R.string.kaleyra_feedback_vote),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
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
