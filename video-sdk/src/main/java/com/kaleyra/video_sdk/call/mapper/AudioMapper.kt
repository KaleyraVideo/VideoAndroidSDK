package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Input
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
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
                    isEnabled = audio.enabled.value.remote,
                    isMutedForYou = !audio.enabled.value.local
                )
            }
            emit(initialValue)

            val flow = this@mapToAudioUi.filterIsInstance<Input.Audio>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.enabled },
            ) { id, enabled ->
                AudioUi(
                    id = id,
                    isEnabled = enabled.remote,
                    isMutedForYou = !enabled.local)
            }.collect {
                emit(it)
            }
        }.distinctUntilChanged()
}