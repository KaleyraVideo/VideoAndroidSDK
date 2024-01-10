package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Input
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal object AudioMapper {

    fun StateFlow<Input.Audio?>.mapToAudioUi(): Flow<AudioUi?> =
        flow {
            val initialValue = value?.let { audio ->
                AudioUi(audio.id, audio.enabled.value)
            }

            emit(initialValue)

            val flow = this@mapToAudioUi.filterIsInstance<Input.Video>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.enabled },
            ) { id, enabled ->
                AudioUi(id, enabled)
            }.collect {
                emit(it)
            }
        }.distinctUntilChanged()
}
