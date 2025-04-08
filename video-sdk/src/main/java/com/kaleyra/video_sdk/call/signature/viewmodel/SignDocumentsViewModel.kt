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

package com.kaleyra.video_sdk.call.signature.viewmodel

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_sdk.call.mapper.SignDocumentMapper.toSignDocumentUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SignDocumentsViewModel(configure: suspend () -> Configuration) : BaseViewModel<SignDocumentUiState>(configure) {

    override fun initialState() = SignDocumentUiState()

    private val notificationManager by lazy { NotificationManagerCompat.from(ContextRetainer.context) }

    init {
        viewModelScope.launch {
            val call = call.first()

            call.toSignDocumentUi()
                .onEach { signDocuments ->
                    _uiState.update {
                        val updatedUiState = it.copy(signDocuments = ImmutableList(signDocuments.toList()))
                        val signingDocument = signDocuments.firstOrNull { signDocument -> signDocument.signState == SignDocumentUi.SignStateUi.Signing }
                        if (it.ongoingSignDocumentUi == null && signingDocument != null) {
                            signDocument(signingDocument)
                            updatedUiState.copy(ongoingSignDocumentUi = signingDocument)
                        } else updatedUiState
                    }
                }
                .launchIn(viewModelScope)
        }
    }


    fun signDocument(signDocumentUi: SignDocumentUi) {
        if (signDocumentUi.signState is SignDocumentUi.SignStateUi.Completed) return

        _uiState.update { it.copy(ongoingSignDocumentUi = signDocumentUi) }
        notificationManager.cancel(signDocumentUi.id.hashCode())
        viewModelScope.launch {
            val call = call.first()
            val signingDocument = call.sharedFolder.signDocuments.value.firstOrNull { it.id == signDocumentUi.id }
            signingDocument?.signState
                ?.onEach { signState ->
                    if (signState is SignDocument.SignState.Error || signState is SignDocument.SignState.Completed) {
                        _uiState.update { it.copy(ongoingSignDocumentUi = null) }
                    }
                }?.launchIn(viewModelScope)
            signingDocument?.let { call.sharedFolder.sign(it) }
        }
    }

    fun cancelSign(signDocumentUi: SignDocumentUi) {
        if (uiState.value.ongoingSignDocumentUi == signDocumentUi) _uiState.update { it.copy(ongoingSignDocumentUi = null) }
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignDocumentsViewModel(configure) as T
                }
            }
    }
}
