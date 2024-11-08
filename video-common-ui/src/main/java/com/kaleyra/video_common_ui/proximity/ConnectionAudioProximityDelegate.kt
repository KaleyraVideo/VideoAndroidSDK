package com.kaleyra.video_common_ui.proximity

import android.annotation.SuppressLint
import android.telecom.CallAudioState
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnection

internal class ConnectionAudioProximityDelegate(val connection: KaleyraCallConnection) : AudioProximityDelegate {

        private var wasLoudspeakerActive: Boolean = false

        @SuppressLint("NewApi")
        override fun trySwitchToEarpiece() {
            wasLoudspeakerActive = connection.currentAudioDevice.replayCache.firstOrNull() is AudioOutputDevice.Loudspeaker
            if (wasLoudspeakerActive) {
                connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE)
            }
        }

        @SuppressLint("NewApi")
        override fun tryRestoreToLoudspeaker() {
            val shouldEnableLoudspeaker = wasLoudspeakerActive && connection.currentAudioDevice.replayCache.firstOrNull() is AudioOutputDevice.Earpiece
            if (shouldEnableLoudspeaker) {
                connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
            }
            wasLoudspeakerActive = false
        }
}
