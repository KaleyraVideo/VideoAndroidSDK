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

package com.kaleyra.video_sdk.call.stream.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

/**
 * Video Ui representation of a video component on the Ui
 * @property id String video ui identifier
 * @property view ImmutableView? optional video's rendering view
 * @property isEnabled Boolean flag identifying if the video ui is enabled, true if enabled, false otherwise
 * @property isScreenShare Boolean flag identifying if the video is a screen share, true if it is a screen share, false otherwise
 * @property pointers ImmutableList<PointerUi> list of currently displayed pointers on the video
 * @constructor
 */
@Immutable
data class VideoUi(
    val id: String,
    val view: ImmutableView? = null,
    val isEnabled: Boolean = false,
    val isScreenShare: Boolean = false,
    val pointers: ImmutableList<PointerUi> = ImmutableList(emptyList())
)
