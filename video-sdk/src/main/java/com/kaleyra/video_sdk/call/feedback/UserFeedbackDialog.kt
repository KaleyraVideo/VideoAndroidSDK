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

package com.kaleyra.video_sdk.call.feedback

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaleyra.video_sdk.call.feedback.view.FeedbackForm
import com.kaleyra.video_sdk.call.feedback.view.FeedbackSent
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.delay

private const val AutoDismissMs = 3000L

@Composable
internal fun UserFeedbackDialog(onUserFeedback: (Float, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var isFeedbackSent by remember { mutableStateOf(false) }

        if (isFeedbackSent) {
            LaunchedEffect(Unit) {
                delay(AutoDismissMs)
                onDismiss()
            }
        }

        Box(Modifier.padding(24.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .animateContentSize()
            ) {
                if (!isFeedbackSent) {
                    FeedbackForm(
                        onUserFeedback = { value: Float, text: String ->
                            onUserFeedback(value, text)
                            isFeedbackSent = true
                        },
                        onDismiss = onDismiss
                    )
                } else FeedbackSent(onDismiss)
            }
        }

    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun UserFeedbackDialogPreview() = KaleyraTheme {
    UserFeedbackDialog(onUserFeedback = { _, _ -> }, onDismiss = {})
}