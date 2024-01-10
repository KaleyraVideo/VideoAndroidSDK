package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable

/**
 * Audio Ui representation of an audio component on the Ui
 * @property id String audio ui identifier
 * @property isEnabled Boolean flag identifying if the audio ui is enabled, true if enabled, false otherwise
 * @constructor
 */
@Immutable
data class AudioUi(
    val id: String,
    val isEnabled: Boolean = false,
    val isEnabledForYou: Boolean = true,
)