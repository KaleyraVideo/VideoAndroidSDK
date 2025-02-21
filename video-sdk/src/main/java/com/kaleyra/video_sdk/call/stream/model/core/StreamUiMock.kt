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

package com.kaleyra.video_sdk.call.stream.model.core

import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo

/**
 * Stream Ui Mock representation
 */
val streamUiMock = StreamUi(
    id = "streamId",
    userInfo = UserInfo("userId", "username", ImmutableUri()),
    isMine = false,
    audio = AudioUi(id = "1", isEnabled = false, isMutedForYou = false, level = 1f),
    video = VideoUi(id = "1", view = null, zoomLevelUi = null, isEnabled = false, isScreenShare = false, pointers = ImmutableList(emptyList()))
)
