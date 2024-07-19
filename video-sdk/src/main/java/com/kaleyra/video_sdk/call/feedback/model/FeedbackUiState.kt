package com.kaleyra.video_sdk.call.feedback.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * FeedbackUiRating enum class representing the rating value
 */
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
 */
@Immutable
sealed class FeedbackUiState: UiState {

    /**
     * Feedback ui state
     * @property comment String? optional comment
     * @property rating FeedbackUiRating rating value
     * @constructor
     */
    data class Display(val rating: FeedbackUiRating? = FeedbackUiRating.Excellent, val comment: String? = null): FeedbackUiState()

    /**
     * Feedback ui state hidden, not ready to be displayed yet
     */
    data object Hidden: FeedbackUiState()

}
