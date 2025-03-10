@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal object AudioMapper {
    fun StateFlow<Input.Audio?>.mapToAudioUi(): Flow<AudioUi?> =
        flow {
            val initialValue = value?.let { audio ->
                AudioUi(
                    id = audio.id,
                    isEnabled = audio.enabled.value.isAtLeastRemotelyEnabled(),
                    isMutedForYou = !audio.enabled.value.isAtLeastLocallyEnabled(),
                    isSpeaking = audio.speaking.value
                )
            }
            emit(initialValue)

            val flow = this@mapToAudioUi.filterIsInstance<Input.Audio>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.enabled },
                flow.flatMapLatest { it.speaking }
            ) { id, enabled, speaking ->
                AudioUi(
                    id = id,
                    isEnabled = enabled.isAtLeastRemotelyEnabled(),
                    isMutedForYou = !enabled.isAtLeastLocallyEnabled(),
                    isSpeaking = speaking)
            }.collect {
                emit(it)
            }
        }.distinctUntilChanged()

    fun Call.toMyCameraStreamAudioUi(): Flow<AudioUi?> =
        this
            .toMyCameraStream()
            .flatMapLatest { it.audio.mapToAudioUi() }
}