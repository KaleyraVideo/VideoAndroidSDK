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

package com.kaleyra.video_common_ui.texttospeech

import android.content.Context
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.VoicePrompts
import com.kaleyra.video_common_ui.mapper.InputMapper
import com.kaleyra.video_common_ui.mapper.InputMapper.toMuteEvents
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallParticipantMutedTextToSpeechNotifierTest {

    private val callMock = mockk<CallUI>()

    private val proximitySensorMock = mockk<ProximitySensor>()

    private val callTextToSpeechMock = mockk<CallTextToSpeech>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        mockkObject(InputMapper)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        every { callMock.toMuteEvents() } returns MutableStateFlow(mockk())
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Enabled
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test participant muted utterance`() = runTest {
        val notifier = spyk(CallParticipantMutedTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))
        every { notifier.shouldNotify } returns true
        every { contextMock.getString(R.string.kaleyra_strings_description_admin_muted_you) } returns "text"

        notifier.start(backgroundScope)

        runCurrent()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_strings_description_admin_muted_you) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test participant muted utterance not reproduced with voice prompts disabled`() = runTest {
        every { contextMock.getString(R.string.kaleyra_strings_description_admin_muted_you) } returns "text"
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Disabled
        val notifier = spyk(CallParticipantMutedTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))
        notifier.start(backgroundScope)

        runCurrent()
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_strings_description_admin_muted_you) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun testDispose() = runTest(UnconfinedTestDispatcher()) {
        val notifier = spyk(CallParticipantMutedTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))
        every { notifier.shouldNotify } returns true

        notifier.start(backgroundScope)
        notifier.dispose()
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

    @Test
    fun `calling start again disposes previous notifier tts`() = runTest(UnconfinedTestDispatcher()) {
        val notifier = spyk(CallParticipantMutedTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock))
        every { notifier.shouldNotify } returns true

        notifier.start(backgroundScope)
        notifier.start(backgroundScope)
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }
}