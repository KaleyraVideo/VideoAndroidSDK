package com.kaleyra.video_sdk.call.settings.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video.noise_filter.DeepFilterNetLoader
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Represents the UI state for the noise filtering feature.
 *
 * This data class is annotated with [Immutable] to indicate to the Compose compiler
 * that its properties will not change after the object is constructed. This allows
 * for potential recomposition optimizations.
 *
 * It implements [UiState], suggesting it's part of a state management pattern
 * (e.g., MVI - Model-View-Intent) where UI states are explicitly defined.
 */
@Immutable
data class NoiseFilterUiState(
    /**
     * The current loading state of the deep filter's neural network model or resources.
     * Represented by [DeepFilterNetLoader.LoadingState], which could include states
     * like Unloaded, Loading, Loaded, or Error.
     * Defaults to [DeepFilterNetLoader.LoadingState.Unloaded].
     */
    val deepFilerLoadingState: DeepFilterNetLoader.LoadingState = DeepFilterNetLoader.LoadingState.Unloaded,

    /**
     * An immutable list of noise filter modes that are supported by the current
     * device or environment and should be presented to the user.
     * Each item in the list is an instance of [NoiseFilterModeUi].
     * Using [ImmutableList] ensures the list cannot be modified post-creation.
     * Defaults to an empty immutable list, indicating no specific modes are listed initially
     * (consider using `persistentListOf()` for cleaner empty list creation).
     */
    val supportedNoiseFilterModesUi: ImmutableList<NoiseFilterModeUi> = ImmutableList(listOf(NoiseFilterModeUi.None)),

    /**
     * The currently selected or active noise filter mode.
     * Represented by an instance of [NoiseFilterModeUi].
     * Defaults to [NoiseFilterModeUi.Standard].
     */
    val currentNoiseFilterModeUi: NoiseFilterModeUi = NoiseFilterModeUi.None,

    /**
     * Flag indicating whether the device is currently experiencing overheating.
     * This may be used by the UI to disable or modify the behavior of
     * resource-intensive features like advanced noise filtering.
     * Defaults to `false`.
     */
    val isDeviceOverHeating: Boolean = false,
) : UiState
