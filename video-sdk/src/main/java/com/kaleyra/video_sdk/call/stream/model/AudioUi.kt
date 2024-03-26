package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable

/**
 * Audio Ui representation of a audio component on the Ui
 * @property id String audio ui identifier
 * @property isEnabled Boolean flag identifying if the audio ui is enabled, true if enabled, false otherwise
 * @property isMuted Boolean flag identifying if the audio ui is muted for you, true if muted, false otherwise
 * @constructor
 */
@Immutable
data class AudioUi(
    val id: String,
    val isEnabled: Boolean = false,
    val isMuted: Boolean = false
)