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
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.VoicePrompts
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallRecordingTextToSpeechNotifierTest {

    private val callMock = mockk<CallUI>()

    private val proximitySensorMock = mockk<ProximitySensor>()

    private val callTextToSpeechMock = mockk<CallTextToSpeech>(relaxed = true)

    private val recordingMock = mockk<Call.Recording>(relaxed = true)

    private val contextMock = mockk<Context>(relaxed = true)

    private lateinit var notifier: CallRecordingTextToSpeechNotifier


    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.getString(any()) } returns ""
        with(callMock) {
            every { recording } returns MutableStateFlow(recordingMock)
            every { state } returns MutableStateFlow(Call.State.Connected)
        }
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Enabled
    }

    @Test
    fun `test automatic recording utterance`() = runTest {
        createNotifier()
        val recordingMock = mockk<Call.Recording>(relaxed = true)
        every { recordingMock.type } returns Call.Recording.Type.Automatic
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        every { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) } returns "text"
        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
        advanceTimeBy(1)
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test automatic recording utterance with callRecordingUtteranceOptionDisabled not notified`() = runTest {
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Disabled
        createNotifier()
        val recordingMock = mockk<Call.Recording>(relaxed = true)
        every { recordingMock.type } returns Call.Recording.Type.Automatic
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        every { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) } returns "text"
        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
        advanceTimeBy(1)
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording never utterance`() = runTest {
        createNotifier()
        val recordingMock = mockk<Call.Recording>(relaxed = true)
        every { recordingMock.type } returns Call.Recording.Type.Never
        every { callMock.recording } returns MutableStateFlow(recordingMock)
        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 0) { callTextToSpeechMock.speak(any()) }
    }

    @Test
    fun `test recording started utterance`() = runTest {
        createNotifier()
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_started) } returns "text"

        notifier.start(backgroundScope)

        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_started) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording started utterance with voice prompts disabled not notified`() = runTest {
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Disabled
        createNotifier()
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_started) } returns "text"

        notifier.start(backgroundScope)

        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_started) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `initial recording stopped state is ignored`() = runTest {
        createNotifier()
        every { recordingMock.state } returns MutableStateFlow(mockk<Call.Recording.State.Stopped>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) } returns "text"

        notifier.start(backgroundScope)

        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording stopped utterance`() = runTest {
        createNotifier()
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()

        state.value = mockk<Call.Recording.State.Stopped>()
        runCurrent()

        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording stopped utterance with voice prompts disabled not notified`() = runTest {
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Disabled
        createNotifier()
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()

        state.value = mockk<Call.Recording.State.Stopped>()
        runCurrent()

        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_stopped) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording error utterance`() = runTest {
        createNotifier()
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_failed) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()

        state.value = mockk<Call.Recording.State.Stopped.Error>()
        runCurrent()

        verify(exactly = 1) { contextMock.getString(R.string.kaleyra_utterance_recording_failed) }
        verify(exactly = 1) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun `test recording error utterance with voice prompts disabled not notified`() = runTest {
        every { KaleyraVideo.voicePrompts } returns VoicePrompts.Disabled
        createNotifier()
        val state = MutableStateFlow<Call.Recording.State>(mockk<Call.Recording.State.Started>())
        every { contextMock.getString(R.string.kaleyra_utterance_recording_failed) } returns "text"
        every { recordingMock.state } returns state

        notifier.start(backgroundScope)
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()

        state.value = mockk<Call.Recording.State.Stopped.Error>()
        runCurrent()

        verify(exactly = 0) { contextMock.getString(R.string.kaleyra_utterance_recording_failed) }
        verify(exactly = 0) { callTextToSpeechMock.speak("text") }
    }

    @Test
    fun testDispose() = runTest(UnconfinedTestDispatcher()) {
        createNotifier()
        notifier.start(backgroundScope)
        notifier.dispose()
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

    @Test
    fun `calling start again disposes previous notifier tts`() = runTest(UnconfinedTestDispatcher()) {
        createNotifier()
        notifier.start(backgroundScope)
        notifier.start(backgroundScope)
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

    @Test
    fun `stop the job when the call is ended`() = runTest(UnconfinedTestDispatcher()) {
        createNotifier()
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { callMock.state } returns callState
        notifier.start(backgroundScope)
        callState.value = Call.State.Disconnected.Ended
        advanceTimeBy(CallRecordingTextToSpeechNotifier.CALL_RECORDING_DEBOUNCE_MILLIS)
        runCurrent()
        verify(exactly = 1) { callTextToSpeechMock.dispose(false) }
    }

    private fun createNotifier() {
        notifier = spyk(CallRecordingTextToSpeechNotifier(callMock, proximitySensorMock, callTextToSpeechMock)).apply {
            every { shouldNotify } returns true
        }
    }
}