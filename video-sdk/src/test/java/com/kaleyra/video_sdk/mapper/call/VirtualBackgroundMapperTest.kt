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

package com.kaleyra.video_sdk.mapper.call

import android.net.Uri
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Effect
import com.kaleyra.video.conference.Effects
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.hasVirtualBackground
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.mapToVirtualBackgroundUi
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toCurrentVirtualBackgroundUi
import com.kaleyra.video_sdk.call.mapper.VirtualBackgroundMapper.toVirtualBackgroundsUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class VirtualBackgroundMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>()

    private val myStreamMock = mockk<Stream.Mutable>()

    private val myVideoMock = mockk<Input.Video.My>()

    private val effectsMock = mockk<Effects>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { callMock.buttons } returns MutableStateFlow(CallUI.Button.all)
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        with(myStreamMock) {
            every { id } returns CameraStreamConstants.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideoMock)
        }
        every { callMock.effects } returns effectsMock
    }

    @Test
    fun backgroundBlurEffect_toCurrentVirtualBackground_blurBackgroundUi() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur(id = "blurId", factor = .5f))
        
        val actual = callMock.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.Blur("blurId", .5f), actual)
    }

    @Test
    fun backgroundImageEffect_toCurrentVirtualBackground_imageBackgroundUi() = runTest {
        val uri = mockk<Uri>()
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image(id = "imageId", image = uri))
        
        val actual = callMock.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.Image("imageId", ImmutableUri(uri)), actual)
    }

    @Test
    fun backgroundNoneEffect_toCurrentVirtualBackground_noneBackgroundUi() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.None)
        
        val actual = callMock.toCurrentVirtualBackgroundUi().first()
        Assert.assertEquals(VirtualBackgroundUi.None, actual)
    }

    @Test
    fun backgroundBlurEffect_mapToVirtualBackgroundUi_blurBackgroundUi() = runTest {
        val effect = Effect.Video.Background.Blur(id = "blurId", factor = 1f)

        val actual = effect.mapToVirtualBackgroundUi()
        Assert.assertEquals(VirtualBackgroundUi.Blur("blurId"), actual)
    }

    @Test
    fun backgroundImageEffect_mapToVirtualBackgroundUi_imageBackgroundUi() = runTest {
        val image = mockk<Uri>()
        val effect = Effect.Video.Background.Image(id = "imageId", image = image)

        val actual = effect.mapToVirtualBackgroundUi()
        Assert.assertEquals(VirtualBackgroundUi.Image("imageId", ImmutableUri(image)), actual)
    }

    @Test
    fun backgroundNoneEffect_mapToVirtualBackgroundUi_noneBackgroundUi() = runTest {
        val effect = Effect.Video.None

        val actual = effect.mapToVirtualBackgroundUi()
        Assert.assertEquals(VirtualBackgroundUi.None, actual)
    }

    @Test
    fun emptyAvailableVideoEffect_toVirtualBackgroundsUi_listHasOnlyNoneBackground() = runTest {
        every { effectsMock.available } returns MutableStateFlow(setOf())
        every { effectsMock.preselected } returns MutableStateFlow(Effect.Video.None)
        
        val actual = callMock.toVirtualBackgroundsUi().first()
        Assert.assertEquals(listOf<VirtualBackgroundUi>(VirtualBackgroundUi.None), actual)
    }

    @Test
    fun availableVideoEffectList_toVirtualBackgroundsUi_mappedVirtualBackgroundUiList() = runTest {
        val uri = mockk<Uri>()
        every { effectsMock.available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(id = "blurId", factor = 1f), Effect.Video.Background.Image(id = "imageId", image = mockk()), Effect.Video.Background.Image(id = "imageId2", image = mockk())))
        every { effectsMock.preselected } returns MutableStateFlow(Effect.Video.Background.Image(id = "imageId", image = uri))
        
        val actual = callMock.toVirtualBackgroundsUi().first()
        Assert.assertEquals(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur(id = "blurId"), VirtualBackgroundUi.Image(id = "imageId", ImmutableUri(uri))), actual)
    }

    @Test
    fun emptyAvailableEffects_hasVirtualBackground_false() = runTest {
        every { effectsMock.available } returns MutableStateFlow(setOf())

        val actual = callMock.hasVirtualBackground().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun availableEffectsNotNull_hasVirtualBackground_true() = runTest {
        every { effectsMock.available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(factor = 1f)))

        val actual = callMock.hasVirtualBackground().first()
        Assert.assertEquals(true, actual)
    }
}
