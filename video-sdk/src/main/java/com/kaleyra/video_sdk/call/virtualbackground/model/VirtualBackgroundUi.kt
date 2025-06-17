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

import androidx.annotation.FloatRange
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

/**
 * Virtual Background Ui representation of the Virtual Background on the Ui
 * @property id String virtual background identifier
 * @constructor
 */
sealed class VirtualBackgroundUi(open val id: String) {

    /**
     * Blue Virtual Background
     * @property id String blur virtual background identifier
     * @property factor Float representation of blur amount
     * @constructor
     */
    data class Blur(override val id: String, @FloatRange(0.0, 1.0)  val factor: Float = 1f): VirtualBackgroundUi(id)

    /**
     * Image Virtual Background
     * @property id String image virtual background identifier
     * @property uri Uri image virtual background uri resource
     * @constructor
     */
    data class Image(override val id: String, val uri: ImmutableUri = ImmutableUri()): VirtualBackgroundUi(id)

    /**
     * None Virtual Background
     */
    data object None: VirtualBackgroundUi("None")
}
