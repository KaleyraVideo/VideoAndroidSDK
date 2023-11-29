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
 * Virtual Background Ui State representation of the Virtual Background State on the Ui
 * @property currentBackground VirtualBackgroundUi current virtual background ui
 * @property backgroundList ImmutableList<VirtualBackgroundUi> available virtual backgrounds
 * @constructor
 */
@Immutable
data class VirtualBackgroundUiState(
    val currentBackground: VirtualBackgroundUi = VirtualBackgroundUi.None,
    val backgroundList: ImmutableList<VirtualBackgroundUi> = ImmutableList(emptyList())
) : UiState
