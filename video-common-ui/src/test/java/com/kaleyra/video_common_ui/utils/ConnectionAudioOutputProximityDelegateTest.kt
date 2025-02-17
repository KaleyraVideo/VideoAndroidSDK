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

package com.kaleyra.video_common_ui.utils

import android.telecom.CallAudioState
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.video_common_ui.connectionservice.KaleyraCallConnection
import com.kaleyra.video_common_ui.proximity.AudioCallSessionAudioProximityDelegate
import com.kaleyra.video_common_ui.proximity.ConnectionAudioProximityDelegate
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

internal class ConnectionAudioOutputProximityDelegateTest {

    private val connection = mockk<KaleyraCallConnection>(relaxed = true)

    private val audioProximityDelegate = ConnectionAudioProximityDelegate(connection)

    @Before
    fun setUp() {
        every { connection.availableAudioDevices } returns MutableStateFlow(listOf(
            AudioOutputDevice.Loudspeaker(),
            AudioOutputDevice.Earpiece(),
            AudioOutputDevice.WiredHeadset()
        ))
    }

    @Test
    fun `change device to earpiece if the current one is loudspeaker`() {
        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE ) }
    }

    @Test
    fun `do not change device to earpiece if the current one is not loudspeaker`() {
        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.WiredHeadset())
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 0) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE ) }    }

    @Test
    fun `restore device to loudspeaker if it was previously active and the current device is earpiece`() {
        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE ) }

        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.Earpiece())
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER ) }
    }

    @Test
    fun `do not restore device to loudspeaker if it was previously active and the current device is not earpiece`() {
        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 1) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE ) }

        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.WiredHeadset())
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 0) { connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER ) }
    }

    @Test
    fun `do not restore device to loudspeaker if it was not previously active and the current device is earpiece`() {
        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.WiredHeadset())
        audioProximityDelegate.trySwitchToEarpiece()
        verify(exactly = 0) { connection.setAudioRoute(CallAudioState.ROUTE_EARPIECE ) }

        every { connection.currentAudioDevice } returns MutableStateFlow(AudioOutputDevice.Earpiece())
        audioProximityDelegate.tryRestoreToLoudspeaker()
        verify(exactly = 0) { connection.setAudioRoute(CallAudioState.ROUTE_SPEAKER ) }
    }
}