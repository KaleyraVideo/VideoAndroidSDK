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

package com.kaleyra.video_common_ui.proximity

import android.annotation.SuppressLint
import android.os.Build
import android.telecom.CallAudioState
import androidx.annotation.RequiresApi
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnection

/**
 * Audio Proximity Delegate
 * @property audioCallSession AudioCallSessionInstance? optional target AudioCallSessionInstance for the operation of switching audio outputs
 * @property connection KaleyraConnection? optional target AudioCallSessionInstance for the operation of switching audio outputs
 */
interface AudioProximityDelegate {

    val audioCallSession: AudioCallSessionInstance?

    val connection: KaleyraCallConnection?

    /**
     * Tries to switch to earpiece audio output if available
     */
    fun trySwitchToEarpiece()

    /**
     * Tries to restore loudspeaker audio output if available
     */
    fun tryRestoreToLoudspeaker()
}

internal class AudioProximityDelegateImpl(
    override val audioCallSession: AudioCallSessionInstance? = null,
    override val connection: KaleyraCallConnection? = null) : AudioProximityDelegate {

    private var wasLoudspeakerActive: Boolean = false

    @SuppressLint("NewApi")
    override fun trySwitchToEarpiece() {
        connection?.let { trySwitchToEarpieceOnConnection(it) }
        audioCallSession?.let { trySwitchToEarpieceOnAudioCallSession(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun trySwitchToEarpieceOnConnection(connection: KaleyraCallConnection) {
        wasLoudspeakerActive = connection.currentAudioDevice.replayCache.firstOrNull() is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE)
        }
    }

    private fun trySwitchToEarpieceOnAudioCallSession(audioCallSession: AudioCallSessionInstance) {
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    @SuppressLint("NewApi")
    override fun tryRestoreToLoudspeaker() {
        connection?.let { tryRestoreToLoudspeakerOnConnection(it) }
        audioCallSession?.let { tryRestoreToLoudspeakerOnAudioCallSession(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun tryRestoreToLoudspeakerOnConnection(connection: KaleyraCallConnection) {
        val shouldEnableLoudspeaker = wasLoudspeakerActive && connection.currentAudioDevice.replayCache.firstOrNull() is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        }
        wasLoudspeakerActive = false
    }

    private fun tryRestoreToLoudspeakerOnAudioCallSession(audioCallSession: AudioCallSessionInstance) {
        val shouldEnableLoudspeaker = wasLoudspeakerActive && audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Loudspeaker())
        }
        wasLoudspeakerActive = false
    }

    private fun AudioCallSessionInstance.tryEnableDevice(audioOutput: AudioOutputDevice) {
        val device = getAvailableAudioOutputDevices.firstOrNull { it.javaClass == audioOutput.javaClass }
        if (device != null) changeAudioOutputDevice(device)
    }
}
