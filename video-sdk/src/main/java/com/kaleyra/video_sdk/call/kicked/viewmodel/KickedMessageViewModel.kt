package com.kaleyra.video_sdk.call.kicked.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class KickedMessageViewModel(configure: suspend () -> Configuration) : BaseViewModel<KickedMessageUiState>(configure) {

    init {
        viewModelScope.launch {
            val call = call.first()
            call.state
            call.toCallStateUi()
                .takeWhile { it !is CallStateUi.Disconnected.Ended.Kicked }
                .onCompletion {
                    val callKickedState = call.state.value as? Call.State.Disconnected.Ended.Kicked ?: return@onCompletion
                    _uiState.emit(KickedMessageUiState.Display(adminName = callKickedState.userId))
                }.launchIn(this)
        }
    }

    override fun initialState() = KickedMessageUiState.Hidden

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KickedMessageViewModel(configure) as T
                }
            }
    }
}