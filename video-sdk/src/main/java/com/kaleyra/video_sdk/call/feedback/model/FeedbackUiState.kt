package com.kaleyra.video_sdk.call.feedback.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState

@Immutable
enum class FeedbackUiRating {
    Excellent,
    Good,
    Neutral,
    Poor,
    Awful
}

/**
 * Feedback ui state
 * @property comment String? optional comment
 * @property rating FeedbackUiRating rating value
 * @constructor
 */
@Immutable
data class FeedbackUiState(val rating: FeedbackUiRating? = FeedbackUiRating.Excellent, val comment: String? = null): UiState