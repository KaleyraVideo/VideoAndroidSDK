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

package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video.conference.Effect
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

internal object VirtualBackgroundMapper {

    fun CallUI.toCurrentVirtualBackgroundUi(): Flow<VirtualBackgroundUi> =
        this.toCurrentCameraVideoEffect()
            .map { it.mapToVirtualBackgroundUi() }
            .filterNotNull()
            .distinctUntilChanged()

    fun CallUI.toVirtualBackgroundsUi(): Flow<List<VirtualBackgroundUi>> {
        return combine(effects.available, effects.preselected) { available, preselected ->
            val blur = available.firstOrNull { it is Effect.Video.Background.Blur }?.mapToVirtualBackgroundUi()
            val image = preselected.takeIf { it is Effect.Video.Background.Image }?.mapToVirtualBackgroundUi()
            listOfNotNull(VirtualBackgroundUi.None, blur, image)
        }.distinctUntilChanged()
    }

    fun CallUI.hasVirtualBackground(): Flow<Boolean> =
        effects.available.map {
            buttons.value.contains(CallUI.Button.CameraEffects) || buttons.value.contains(CallUI.Button.Settings) && it.isNotEmpty()
        }.distinctUntilChanged()

    fun Effect.mapToVirtualBackgroundUi(): VirtualBackgroundUi? {
        return when (this) {
            is Effect.Video.Background.Blur -> VirtualBackgroundUi.Blur(id = id, factor = factor)
            is Effect.Video.Background.Image -> VirtualBackgroundUi.Image(id = id, uri = ImmutableUri(image))
            is Effect.Video.None -> VirtualBackgroundUi.None
            else -> null
        }
    }

    private fun CallUI.toCurrentCameraVideoEffect(): Flow<Effect> =
        this.participants
            .mapNotNull { it.me }
            .flatMapLatest { it.streams }
            .map { streams ->
                streams.firstOrNull { it.id == CameraStreamConstants.CAMERA_STREAM_ID }
            }
            .filterNotNull()
            .flatMapLatest { it.video }
            .filterNotNull()
            .flatMapLatest { it.currentEffect }
            .distinctUntilChanged()
}