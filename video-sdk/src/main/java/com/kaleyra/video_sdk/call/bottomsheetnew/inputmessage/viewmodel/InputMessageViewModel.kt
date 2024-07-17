@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class InputMessageViewModel(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val userMessageChannel = Channel<InputMessage>(Channel.BUFFERED)

    val inputMessage: Flow<InputMessage> = userMessageChannel.receiveAsFlow().map { it }

    private var isMyMicEnabled: Boolean? = null
    private var isMyCameraEnabled: Boolean? = null

    init {
        viewModelScope.launch {
            val currentCall = conference.flatMapLatest { it.call }.first()

            currentCall.preferredType.onEach { preferredType ->

                currentCall
                    .isMyMicEnabled()
                    .takeIf { preferredType.hasAudio() }
                    ?.drop(
                        when {
                            isMyMicEnabled == true -> 0
                            currentCall.preferredType.value.isAudioEnabled() ->
                                if (isMyMicEnabled == null) 2 else 1

                            else ->
                                if (isMyMicEnabled == null) 1 else 0
                        }
                    )
                    ?.filterNot { currentCall.state.value is Call.State.Disconnecting || currentCall.state.value is Call.State.Disconnected }
                    ?.onEach { isMyMicEnabled ->
                        this@InputMessageViewModel.isMyMicEnabled = isMyMicEnabled
                        userMessageChannel.send(if (isMyMicEnabled) MicMessage.Enabled else MicMessage.Disabled)
                    }?.launchIn(this)

                currentCall
                    .isMyCameraEnabled()
                    .takeIf { preferredType.hasVideo() }
                    ?.drop(
                        when {
                            isMyCameraEnabled == true -> 0
                            currentCall.preferredType.value.isVideoEnabled() ->
                                if (isMyCameraEnabled == null) 2 else 1

                            else ->
                                if (isMyCameraEnabled == null) 1 else 0
                        }
                    )
                    ?.filterNot { currentCall.state.value is Call.State.Disconnecting || currentCall.state.value is Call.State.Disconnected }
                    ?.onEach { isMyCameraEnabled ->
                        this@InputMessageViewModel.isMyCameraEnabled = isMyCameraEnabled
                        userMessageChannel.send(if (isMyCameraEnabled) CameraMessage.Enabled else CameraMessage.Disabled)
                    }?.launchIn(this)
            }.launchIn(this)
        }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InputMessageViewModel(configure) as T
                }
            }
    }
}
