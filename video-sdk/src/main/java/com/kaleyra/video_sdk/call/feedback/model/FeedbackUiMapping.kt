package com.kaleyra.video_sdk.call.feedback.model

import com.kaleyra.video_sdk.R

internal object FeedbackUiMapping {

    internal fun FeedbackUiRating.toSliderValue(): Float = when (this) {
        FeedbackUiRating.Awful -> 1f
        FeedbackUiRating.Poor -> 2f
        FeedbackUiRating.Neutral -> 3f
        FeedbackUiRating.Good -> 4f
        FeedbackUiRating.Excellent -> 5f
    }


    internal fun feedbackUiValueFor(float: Float): FeedbackUiRating = when (float) {
        1f -> FeedbackUiRating.Awful
        2f -> FeedbackUiRating.Poor
        3f -> FeedbackUiRating.Neutral
        4f -> FeedbackUiRating.Good
        else -> FeedbackUiRating.Excellent
    }

    internal fun FeedbackUiRating.toRatingStringRes(): Int = when (this) {
        FeedbackUiRating.Awful -> R.string.kaleyra_feedback_bad
        FeedbackUiRating.Poor -> R.string.kaleyra_feedback_poor
        FeedbackUiRating.Neutral -> R.string.kaleyra_feedback_neutral
        FeedbackUiRating.Good -> R.string.kaleyra_feedback_good
        FeedbackUiRating.Excellent -> R.string.kaleyra_feedback_excellent
    }
}