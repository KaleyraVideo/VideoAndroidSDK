package com.kaleyra.video_sdk.call.feedback.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.mapper.StreamMapper.doAnyOfMyStreamsIsLive
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiRating
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
internal class FeedbackViewModel(configure: suspend () -> Configuration) : BaseViewModel<FeedbackUiState>(configure) {

    init {
        viewModelScope.launch {
            val call = call.first()
            if (!call.withFeedback) return@launch
            var doAnyOfMyStreamsIsLive = false
            call.doAnyOfMyStreamsIsLive().filter { it }.onEach { doAnyOfMyStreamsIsLive = true }.launchIn(this)
            call.state
                .takeWhile { it is Call.State.Disconnected.Ended.Kicked || it !is Call.State.Disconnected.Ended  }
                .onCompletion {
                    if (!doAnyOfMyStreamsIsLive) return@onCompletion
                    _uiState.emit(FeedbackUiState.Display())
                }.launchIn(this)
        }
    }

    override fun initialState() = FeedbackUiState.Hidden

    fun sendUserFeedback(comment: String?, rating: FeedbackUiRating) {
        if (_uiState.value is FeedbackUiState.Hidden) return
        _uiState.update {
            (it as FeedbackUiState.Display).copy(comment = comment, rating = rating)
        }
        val call = call.getValue() ?: return
        val me = call.participants.value.me ?: return
        (uiState.value as? FeedbackUiState.Display)?.let {
            me.feedback.value = CallParticipant.Me.Feedback(callParticipantRatingValueFor(it.rating!!), it.comment)
        }
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