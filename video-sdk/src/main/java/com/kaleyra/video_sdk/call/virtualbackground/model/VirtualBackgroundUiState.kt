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

package com.kaleyra.video_sdk.call.virtualbackground.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Represents the UI state for the virtual background feature.
 *
 * This data class is annotated with [Immutable] to inform the Compose compiler that
 * its properties will not change after construction, allowing for potential
 * recomposition optimizations.
 *
 * It implements [UiState], suggesting it's part of a state management pattern
 * (e.g., MVI - Model-View-Intent) where UI states are explicitly defined.
 */
@Immutable
data class VirtualBackgroundUiState(

    /**
     * The currently selected or active virtual background.
     * It's represented by an instance of [VirtualBackgroundUi], which could define
     * different types of backgrounds like None, Blur, or specific images.
     * Defaults to [VirtualBackgroundUi.None].
     */
    val currentBackground: VirtualBackgroundUi = VirtualBackgroundUi.None,

    /**
     * An immutable list of available virtual backgrounds that the user can choose from.
     * Each item in the list is a [VirtualBackgroundUi] instance.
     * Using [ImmutableList] ensures that the list itself cannot be modified after
     * this state object is created, contributing to the immutability of the state.
     * Defaults to an empty immutable list.
     */
    val backgroundList: ImmutableList<VirtualBackgroundUi> = ImmutableList(emptyList()), // Consider persistentListOf() for easier creation

    /**
     * Flag indicating whether the device is currently experiencing overheating.
     * This might be used to disable or alter the behavior of resource-intensive
     * features like virtual backgrounds.
     * Defaults to `false`.
     */
    val isDeviceOverHeating: Boolean = false
) : UiState
