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

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.VoicePrompts
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

internal class CallRecordingTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor,
    override val callTextToSpeech: CallTextToSpeech = CallTextToSpeech(),
    override val voicePromptsEnabled: Boolean = KaleyraVideo.voicePrompts == VoicePrompts.Enabled
) : TextToSpeechNotifier {

    companion object {
        const val CALL_RECORDING_DEBOUNCE_MILLIS = 500L
    }

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        currentJob = call.recording
            .combine(call.state) { recording, callState -> recording to callState }
            .takeWhile { (_, callState) -> callState !is Call.State.Disconnected.Ended }
            // add a debounce to avoid the text to be reproduced when the call is ended and the app is in background
            .debounce(CALL_RECORDING_DEBOUNCE_MILLIS)
            .onEach { (recording, _)  ->
                if (!shouldNotify || !voicePromptsEnabled) return@onEach
                when (recording.type) {
                    Call.Recording.Type.Automatic -> {
                        val text = context.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded)
                        callTextToSpeech.speak(text)
                    }
                    else -> Unit
                }
            }
            .flatMapLatest { (recording, _) ->  recording.state }
            .dropWhile { it is Call.Recording.State.Stopped }
            .onEach { recordingState ->
                if (!shouldNotify || !voicePromptsEnabled) return@onEach
                val text = when (recordingState) {
                    is Call.Recording.State.Started -> context.getString(R.string.kaleyra_utterance_recording_started)
                    is Call.Recording.State.Stopped.Error -> context.getString(R.string.kaleyra_utterance_recording_failed)
                    is Call.Recording.State.Stopped -> context.getString(R.string.kaleyra_utterance_recording_stopped)
                }
                callTextToSpeech.speak(text)
            }
            .onCompletion { callTextToSpeech.dispose(instantly = false) }
            .launchIn(scope)
    }

    override fun dispose() {
        currentJob?.cancel()
        currentJob = null
    }

}