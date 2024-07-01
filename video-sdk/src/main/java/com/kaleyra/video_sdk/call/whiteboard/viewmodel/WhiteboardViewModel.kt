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

package com.kaleyra.video_sdk.call.whiteboard.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video.whiteboard.WhiteboardView
import com.kaleyra.video_sdk.call.mapper.CallActionsMapper.isFileSharingSupported
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.isWhiteboardLoading
import com.kaleyra.video_sdk.call.mapper.WhiteboardMapper.toWhiteboardUploadUi
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class WhiteboardViewModel(configure: suspend () -> Configuration, whiteboardView: WhiteboardView) : BaseViewModel<WhiteboardUiState>(configure) {

    override fun initialState() = WhiteboardUiState()

    private val whiteboard = call
        .map { it.whiteboard }
        .shareInEagerly(viewModelScope)

    private var resetWhiteboardUploadState = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            val call = call.first()
            
            whiteboard
                .take(1)
                .onEach { setUpWhiteboard(it, whiteboardView) }
                .launchIn(viewModelScope)

            call.state
                .onEach {
                    if (it !is Call.State.Disconnected.Ended) return@onEach
                    whiteboard.getValue()?.unload()
                }.launchIn(viewModelScope)

            call
                .isFileSharingSupported()
                .onEach { isFileSharingSupported -> _uiState.update { it.copy(isFileSharingSupported = isFileSharingSupported) } }
                .launchIn(viewModelScope)

            call
                .isWhiteboardLoading()
                .onEach { isLoading -> _uiState.update { it.copy(isLoading = isLoading) } }
                .launchIn(viewModelScope)
        }
    }

    override fun onCleared() {
        super.onCleared()
        val whiteboard = whiteboard.getValue()
        whiteboard?.view?.value = null
    }

    fun onReloadClick() {
        val whiteboard = whiteboard.getValue()
        whiteboard?.load()
    }
    
    fun onWhiteboardClosed() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun uploadMediaFile(uri: Uri) {
        val whiteboard = whiteboard.getValue()
        val sharedFile = whiteboard?.addMediaFile(uri)?.getOrNull() ?: return
        observeAndUpdateUploadState(sharedFile)
    }

    private fun setUpWhiteboard(whiteboard: Whiteboard, whiteboardView: WhiteboardView) {
        whiteboard.view.value = whiteboardView
        whiteboard.load()
        _uiState.update { it.copy(whiteboardView = whiteboardView) }
    }

    private fun observeAndUpdateUploadState(sharedFile: SharedFile) {
        resetWhiteboardUploadState.set(false)
        sharedFile
            .toWhiteboardUploadUi()
            .onEach { upload -> _uiState.update { it.copy(upload = upload) } }
            .takeWhile { it is WhiteboardUploadUi.Uploading && it.progress != 1f }
            .onCompletion {
                resetWhiteboardUploadState.set(true)
                val delayMs = if (sharedFile.state.value is SharedFile.State.Error) ErrorResetUploadDelay else DefaultResetUploadDelay
                delay(delayMs)
                if (resetWhiteboardUploadState.compareAndSet(true, false)) {
                    _uiState.update { it.copy(upload = null) }
                }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val ErrorResetUploadDelay = 3000L
        private const val DefaultResetUploadDelay = 300L

        fun provideFactory(configure: suspend () -> Configuration, whiteboardView: WhiteboardView) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WhiteboardViewModel(configure, whiteboardView) as T
                }
            }
    }
}
