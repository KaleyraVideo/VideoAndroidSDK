package com.kaleyra.video_sdk.call.settings.model


/**
 * Represents the available noise filter modes that can be selected in the UI.
 * This sealed class ensures that only defined noise filter modes can be represented.
 */
sealed class NoiseFilterModeUi {
    /**
     * Represents the DeepFilter AI noise suppression mode.
     * This mode offers advanced, AI-powered noise reduction.
     */
    data object DeepFilterAi : NoiseFilterModeUi()

    /**
     * Represents a standard noise suppression mode.
     * This mode usually provides a balance between noise reduction and resource usage.
     */
    data object Standard : NoiseFilterModeUi()

    /**
     * Represents the state where no noise suppression is active.
     */
    data object None : NoiseFilterModeUi()
}