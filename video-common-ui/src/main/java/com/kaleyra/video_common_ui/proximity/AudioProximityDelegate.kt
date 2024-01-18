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

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance

/**
 * Audio Proximity Delegate
 * @property audioCallSession AudioCallSessionInstance target AudioCallSessionInstance for the operation of switching audio outputs
 */
interface AudioProximityDelegate {

    val audioCallSession: AudioCallSessionInstance

    /**
     * Tries to switch to earpiece audio output if available
     */
    fun trySwitchToEarpiece()

    /**
     * Tries to restore loudspeaker audio output if available
     */
    fun tryRestoreToLoudspeaker()
}

internal class AudioProximityDelegateImpl(override val audioCallSession: AudioCallSessionInstance) : AudioProximityDelegate {

    private var wasLoudspeakerActive: Boolean = false

    override fun trySwitchToEarpiece() {
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    override fun tryRestoreToLoudspeaker() {
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
