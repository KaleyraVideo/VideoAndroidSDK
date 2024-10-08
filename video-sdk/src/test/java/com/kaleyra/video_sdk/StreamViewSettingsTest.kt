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

import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.defaultStreamViewSettings
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.pipSettings
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class StreamViewSettingsTest {

    @Test
    fun testPipSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.pipSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fit, videoStreamView.scaleType.value)
    }

    @Test
    fun testDefaultSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.defaultStreamViewSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(true, videoStreamView.zoomGesturesEnabled.value)
        assertEquals(StreamView.ScaleType.Fill(.2f), videoStreamView.scaleType.value)
    }

    @Test
    fun testPreCallStreamViewSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.preCallStreamViewSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(true, videoStreamView.zoomGesturesEnabled.value)
        assertEquals(StreamView.ScaleType.Fill(1f), videoStreamView.scaleType.value)
    }
}