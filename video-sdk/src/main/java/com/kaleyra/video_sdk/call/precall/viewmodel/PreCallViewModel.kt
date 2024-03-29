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

package com.kaleyra.video_sdk.call.precall.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.call.mapper.InputMapper.isAudioVideo
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.video_sdk.call.mapper.WatermarkMapper.toWatermarkInfo
import com.kaleyra.video_sdk.call.precall.model.PreCallUiState
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal abstract class PreCallViewModel<T : PreCallUiState<T>>(configure: suspend () -> Configuration) : BaseViewModel<T>(configure),
    UserMessageViewModel {

    abstract override fun initialState(): T

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        company
            .flatMapLatest { it.combinedTheme }
            .toWatermarkInfo(company.flatMapLatest { it.name })
            .onEach { watermarkInfo -> _uiState.update { it.clone(watermarkInfo = watermarkInfo) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _uiState.update { it.clone(isLink = call.first().isLink) }
        }

        call
            .toMyStreamsUi()
            .onEach { streams -> _uiState.update { it.clone(video = streams.firstOrNull()?.video) } }
            .launchIn(viewModelScope)

        call
            .toOtherDisplayNames()
            .onEach { names ->
                if (uiState.value.participants.value == names) return@onEach
                _uiState.update { it.clone(participants = ImmutableList(names)) }
            }
            .launchIn(viewModelScope)

        call
            .toOtherDisplayImages()
            .onEach { images ->
                val avatar = images.firstOrNull()
                if (avatar == null || uiState.value.avatar?.value == avatar) return@onEach
                _uiState.update { it.clone(avatar = ImmutableUri(avatar)) }
            }
            .launchIn(viewModelScope)

        call
            .isAudioVideo()
            .onEach { isAudioVideo -> _uiState.update { it.clone(isAudioVideo = isAudioVideo) } }
            .launchIn(viewModelScope)
    }
}
