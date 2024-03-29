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

package com.kaleyra.video_sdk

import android.graphics.Matrix
import android.util.Size
import androidx.compose.ui.unit.IntSize
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getScale
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getSize
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getTranslation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class VideoStreamViewExtensionsTest {

    @Test
    fun testGetSize() = runTest(UnconfinedTestDispatcher()) {
        val view = mockk<VideoStreamView>()
        every { view.videoSize } returns MutableStateFlow(Size(300, 200))
        val actual = view.getSize().first()
        assertEquals(IntSize(300, 200), actual)
    }

    @Test
    fun `test get translation with an affine matrix`() = runTest(UnconfinedTestDispatcher()) {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setTranslate(.5f, .8f)
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getTranslation().first()
        assertEquals(.5f, actual[0])
        assertEquals(.8f, actual[1])
    }

    @Test
    fun `test get translation with a not affine matrix`() = runTest(UnconfinedTestDispatcher()) {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setValues(floatArrayOf(.6f, .7f, .8f, .3f, .1f, .2f, .3f, .6f, .4f))
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getTranslation().first()
        assertEquals(0f, actual[0])
        assertEquals(0f, actual[1])
    }

    @Test
    fun `test get scale with an affine matrix`() = runTest(UnconfinedTestDispatcher()) {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setScale(2f, 3f)
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getScale().first()
        assertEquals(2f, actual[0])
        assertEquals(3f, actual[1])
    }

    @Test
    fun `test get scale with a not affine matrix`() = runTest(UnconfinedTestDispatcher()) {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setValues(floatArrayOf(.6f, .7f, .8f, .3f, .1f, .2f, .3f, .6f, .4f))
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getScale().first()
        assertEquals(0f, actual[0])
        assertEquals(0f, actual[1])
    }
}