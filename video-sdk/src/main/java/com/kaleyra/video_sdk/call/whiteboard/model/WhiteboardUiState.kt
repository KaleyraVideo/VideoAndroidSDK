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

package com.kaleyra.video_sdk.call.whiteboard.model

import android.view.View
import androidx.compose.runtime.Immutable
import com.kaleyra.video.whiteboard.Whiteboard
import com.kaleyra.video_sdk.common.uistate.UiState

/**
 * Whiteboard Ui State representation of the Whiteboard State on the Ui
 * @property whiteboardView View? optional whiteboard rendering view
 * @property showingRequest Whiteboard.Event.Request? optional show or hide request
 * @property isLoading Boolean flag indicating if the whiteboard is loading, true if loading, false otherwise
 * @property isLoading Boolean flag indicating if the whiteboard is loading, true if loading, false otherwise
 * @property isOffline Boolean flag indicating if the whiteboard id offline, true if offline, false otherwise
 * @property isFileSharingSupported Boolean flag indicating if the file sharing is supported
 * @property upload WhiteboardUploadUi? optional whiteboard upload ui
 * @property text String? optional whiteboard text
 * @constructor
 */

@Immutable
internal data class WhiteboardUiState(
    val whiteboardView: View? = null,
    val showingRequest: Whiteboard.Event.Request? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isFileSharingSupported: Boolean = false,
    val upload: WhiteboardUploadUi? = null,
    val text: String? = null
) : UiState

