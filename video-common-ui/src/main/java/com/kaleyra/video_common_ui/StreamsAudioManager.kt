package com.kaleyra.video_common_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StreamsAudioManager(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private val jobs = mutableListOf<Job>()

    fun bind(call: Call) {
        stop()
        disableStreamAudioOnMute(call)
    }

    fun stop() {
        jobs.forEach { it.cancel() }
    }

    fun disableStreamAudioOnMute(call: Call) {
        var previousDevice: AudioOutputDevice? = null
        jobs += call.currentAudioOutputDevice
            .filterNotNull()
            .onEach { device ->
                when {
                    shouldDisableStreamsAudio(device, previousDevice) -> enableStreamsAudio(call, enable = false)
                    shouldEnableStreamsAudio(device, previousDevice) -> enableStreamsAudio(call, enable = true)
                }
                previousDevice = device
            }.launchIn(coroutineScope)
    }

    private fun shouldDisableStreamsAudio(
        device: AudioOutputDevice,
        previousDevice: AudioOutputDevice?
    ) = device is AudioOutputDevice.None && previousDevice !is AudioOutputDevice.None

    private fun shouldEnableStreamsAudio(
        device: AudioOutputDevice,
        previousDevice: AudioOutputDevice?
    ) = device !is AudioOutputDevice.None && previousDevice is AudioOutputDevice.None

    private fun enableStreamsAudio(call: Call, enable: Boolean) {
        val participants = call.participants
        val others = participants.value.others
        val streams = others.map { it.streams.value }.flatten()
        val audio = streams.map { it.audio.value }
        if (enable) audio.forEach { it?.tryEnable() }
        else audio.forEach { it?.tryDisable() }
    }
}