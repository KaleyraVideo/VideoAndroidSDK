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

import android.util.Rational
import android.util.Size
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallUiStateMapperTest {

    private val viewMock = mockk<VideoStreamView>()

    private val video = VideoUi(id = "videoId", view = ImmutableView(viewMock))

    private val stream = StreamUi(id = "streamId", username = "username", video = video)

    private val callUiState = CallUiState(featuredStreams = ImmutableList(listOf(stream)))

    @Test
    fun `first featured video width and height have greatest common divisor, the aspect ratio is the resolution divided by the divisor`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(1920, 1080))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(16, 9)
        assertEquals(expected, actual)
    }

    @Test
    fun `first featured video width and height does not have greatest common divisor, the aspect ratio is 1-1`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(234, 433))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(234, 433)
        assertEquals(expected, actual)
    }

    @Test
    fun `first featured video have invalid width or height, the aspect ratio is NaN`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(0, 0))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational.NaN
        assertEquals(expected, actual)
    }
}