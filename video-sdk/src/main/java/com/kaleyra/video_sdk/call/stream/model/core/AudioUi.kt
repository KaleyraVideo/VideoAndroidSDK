package com.kaleyra.video_sdk.call.stream.model.core

import androidx.compose.runtime.Immutable

/**
 * Audio Ui representation of a audio component on the Ui
 * @property id String audio ui identifier
 * @property isEnabled Boolean flag identifying if the audio ui is enabled, true if enabled, false otherwise
 * @property isMutedForYou Boolean flag identifying if the audio ui is muted for you, true if muted, false otherwise
 * @property isSpeaking Boolean parameter indicating that the owner of the audio ui instance, represented by a call participant, is speaking or not
 * @constructor
 */
@Immutable
data class AudioUi(
    val id: String,
    val isEnabled: Boolean = false,
    val isMutedForYou: Boolean = false,
    val isSpeaking: Boolean = false
)