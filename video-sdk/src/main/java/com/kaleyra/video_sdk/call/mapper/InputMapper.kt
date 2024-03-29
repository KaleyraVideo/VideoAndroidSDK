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

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.mapper.InputMapper.toCameraStreamAudio
import com.kaleyra.video_common_ui.mapper.InputMapper.toMuteEvents
import com.kaleyra.video_common_ui.mapper.ParticipantMapper.toMe
import com.kaleyra.video_common_ui.utils.UsbCameraUtils
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.failedAudioOutputDevice
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal object InputMapper {

    fun Flow<Call>.toAudioConnectionFailureMessage(): Flow<AudioConnectionFailureMessage> =
        this.flatMapLatest { it.failedAudioOutputDevice }
            .filterNotNull()
            .map {
                if (it.isInSystemCall) AudioConnectionFailureMessage.InSystemCall
                else AudioConnectionFailureMessage.Generic
            }

    fun Flow<Call>.toMutedMessage(): Flow<MutedMessage> =
        this.toMuteEvents()
            .map { event -> event.producer.combinedDisplayName.first() }
            .map { MutedMessage(it) }

    fun Flow<Call>.isAudioOnly(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { !it.hasVideo() }
            .distinctUntilChanged()

    fun Flow<Call>.isAudioVideo(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { it.isVideoEnabled() }
            .distinctUntilChanged()

    fun Flow<Call>.hasAudio(): Flow<Boolean> =
        this.flatMapLatest { it.preferredType }
            .map { it.hasAudio() }
            .distinctUntilChanged()

    fun Flow<Call>.isMyCameraEnabled(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.firstOrNull { stream -> stream.id == CAMERA_STREAM_ID } }
            .flatMapLatest { it?.video ?: flowOf(null) }
            .flatMapLatest { it?.enabled ?: flowOf(false) }
            .distinctUntilChanged()

    fun Flow<Call>.isMyMicEnabled(): Flow<Boolean> =
        this.toCameraStreamAudio()
            .flatMapLatest { it?.enabled ?: flowOf(false) }
            .distinctUntilChanged()

    fun Flow<Call>.isSharingScreen(): Flow<Boolean> =
        this.toMe()
            .flatMapLatest { it.streams }
            .map { streams -> streams.any { stream -> stream.id == SCREEN_SHARE_STREAM_ID } }
            .distinctUntilChanged()

    fun Flow<Call>.hasUsbCamera(): Flow<Boolean> =
        this.flatMapLatest { it.inputs.availableInputs }
            .map { inputs -> inputs.firstOrNull { it is Input.Video.Camera.Usb } != null }
            .distinctUntilChanged()

    fun Flow<Call>.toUsbCameraMessage(): Flow<UsbCameraMessage> =
        this.flatMapLatest { it.inputs.availableInputs }
            .map { inputs ->
                val usbCamera = inputs.firstOrNull { it is Input.Video.Camera.Usb }
                when {
                    usbCamera != null && UsbCameraUtils.isSupported() -> UsbCameraMessage.Connected((usbCamera as Input.Video.Camera.Usb).name)
                    usbCamera != null                                 -> UsbCameraMessage.NotSupported
                    else                                              -> UsbCameraMessage.Disconnected
                }
            }

    fun Flow<Call>.isUsbCameraWaitingPermission(): Flow<Boolean> =
        this.map { it.inputs }
            .flatMapLatest { it.availableInputs }
            .map { inputs -> inputs.firstOrNull { it is Input.Video.Camera.Usb } }
            .flatMapLatest { it?.state ?: flowOf(null) }
            .map { it is Input.State.Closed.AwaitingPermission }

}