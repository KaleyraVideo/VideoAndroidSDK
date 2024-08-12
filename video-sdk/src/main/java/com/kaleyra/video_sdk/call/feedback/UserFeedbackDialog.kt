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
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.feedback.view.FeedbackForm
import com.kaleyra.video_sdk.call.feedback.view.FeedbackSent
import com.kaleyra.video_sdk.call.feedback.viewmodel.FeedbackViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.ActivityComponentExtensions.isAtLeastResumed
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import kotlinx.coroutines.delay

private const val AutoDismissMs = 3000L

@Composable
internal fun UserFeedbackDialog(
    onDismiss: () -> Unit,
    viewModel: FeedbackViewModel = viewModel(factory = FeedbackViewModel.provideFactory(::requestCollaborationViewModelConfiguration))) {

    val feedbackUiState = viewModel.uiState.collectAsStateWithLifecycle()

    UserFeedbackDialog(
        feedbackUiState = feedbackUiState.value,
        onUserFeedback = { rating, comment ->
            viewModel.sendUserFeedback(comment, rating)
        }, onDismiss = onDismiss)
}

@Composable
internal fun UserFeedbackDialog(
    feedbackUiState: FeedbackUiState,
    onUserFeedback: (FeedbackUiRating, String) -> Unit,
    onDismiss: () -> Unit) {
    if (feedbackUiState is FeedbackUiState.Hidden) return
    val activity = LocalContext.current.findActivity() as ComponentActivity
    if (!activity.isAtLeastResumed()) return

    val orientation = LocalConfiguration.current.orientation
    val windowSizeClass = currentWindowAdaptiveInfo()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        var isFeedbackSent by remember { mutableStateOf(false) }

        if (isFeedbackSent) {
            LaunchedEffect(Unit) {
                delay(AutoDismissMs)
                onDismiss()
            }
        }

        val configuration = LocalConfiguration.current
        Box(Modifier
            .width(min(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp))
            .padding(
                if (
                    orientation == Configuration.ORIENTATION_LANDSCAPE &&
                    windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
                ) 8.dp else 24.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .animateContentSize()
            ) {
                if (!isFeedbackSent) {
                    FeedbackForm(
                        feedbackUiState = feedbackUiState,
                        onUserFeedback = { rating: FeedbackUiRating, text: String ->
                            onUserFeedback(rating, text)
                            isFeedbackSent = true
                        },
                        onDismiss = onDismiss
                    )
                } else FeedbackSent(onDismiss)
            }
        }
    }
}

@MultiConfigPreview
@Composable
internal fun UserFeedbackDialog() = KaleyraTheme {
    UserFeedbackDialog(
        onDismiss = {}
    )
}

@MultiConfigPreview
@Composable
internal fun UserFeedbackDialogPreview() = KaleyraTheme {
    UserFeedbackDialog(
        FeedbackUiState.Display(comment = "test comment", rating = FeedbackUiRating.Good),
        onUserFeedback = { _, _ -> },
        onDismiss = {}
    )
}

@MultiConfigPreview
@Composable
internal fun UserFeedbackDialogDefaultFeedbackPreview() = KaleyraTheme {
    UserFeedbackDialog(
        FeedbackUiState.Display(),
        onUserFeedback = { _, _ -> },
        onDismiss = {}
    )
}
