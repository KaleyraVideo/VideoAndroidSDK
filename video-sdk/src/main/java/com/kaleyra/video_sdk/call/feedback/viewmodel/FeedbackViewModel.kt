package com.kaleyra.video_sdk.call.feedback.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.update

@Immutable
internal class FeedbackViewModel(configure: suspend () -> Configuration) : BaseViewModel<FeedbackUiState>(configure) {
    override fun initialState() = FeedbackUiState()

    fun sendUserFeedback(comment: String?, rating: FeedbackUiRating) {
        _uiState.update { it.copy(comment = comment, rating = rating) }
        val call = call.getValue() ?: return
        val me = call.participants.value.me ?: return
        me.feedback.value = CallParticipant.Me.Feedback(callParticipantRatingValueFor(uiState.value.rating!!), uiState.value.comment)
    }

    private fun callParticipantRatingValueFor(feedbackUiRating: FeedbackUiRating) = when (feedbackUiRating) {
        FeedbackUiRating.Awful -> 1
        FeedbackUiRating.Poor -> 2
        FeedbackUiRating.Neutral -> 3
        FeedbackUiRating.Good -> 4
        else -> 5
    }


    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FeedbackViewModel(configure) as T
                }
            }
    }
}