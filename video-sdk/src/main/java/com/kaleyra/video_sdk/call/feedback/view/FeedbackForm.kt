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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

private const val DefaultRating = 5f
private const val SliderLevels = 5

/**
 * Feedback Form Tag
 */
const val FeedbackFormTag = "FeedbackFormTag"

@Composable
internal fun FeedbackForm(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    var isEditTextFocused by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(DefaultRating) }

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
                lineHeight = 22.sp,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = ratingTextFor(sliderValue),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(10.dp))
        StarSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            levels = SliderLevels,
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
            maxLines = 4,
            minLines = if (isEditTextFocused) 4 else 1,
            textStyle = TextStyle(
                fontSize = 14.sp
            ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp),
                content = {
                    Text(
                        text = stringResource(id = R.string.kaleyra_action_cancel),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
            )
            Button(
                onClick = { onUserFeedback(sliderValue, textFieldValue.text) },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = stringResource(id = R.string.kaleyra_feedback_vote),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ratingTextFor(sliderValue: Float): String {
    val stringId by remember(sliderValue) {
        derivedStateOf {
            when {
                sliderValue < 2f -> R.string.kaleyra_feedback_bad
                sliderValue < 3f -> R.string.kaleyra_feedback_poor
                sliderValue < 4f -> R.string.kaleyra_feedback_neutral
                sliderValue < 5f -> R.string.kaleyra_feedback_good
                else -> R.string.kaleyra_feedback_excellent
            }
        }
    }
    return stringResource(id = stringId)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FeedbackFormPreview() = KaleyraM3Theme {
    Surface {
        FeedbackForm({ _, _ -> }, {})
    }
}
