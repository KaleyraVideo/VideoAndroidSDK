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

package com.kaleyra.video_common_ui

import android.app.Activity
import android.app.Application
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.call.ScreenShareOverlayProducer
import com.kaleyra.video_common_ui.overlay.AppViewOverlay
import com.kaleyra.video_common_ui.overlay.StatusBarOverlayView
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.requestOverlayPermission
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ScreenShareOverlayProducerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val activityMock = mockk<Activity>(relaxed = true)

    private val callMock = mockk<CallUI>()

    private val inputsMock = mockk<Inputs>()

    private val applicationMock = mockk<Application>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextExtensions)
        mockkObject(ActivityExtensions)
        mockkConstructor(AppViewOverlay::class)
        mockkConstructor(StatusBarOverlayView::class)
        every { anyConstructed<AppViewOverlay>().show(any()) } returns Unit
        every { anyConstructed<AppViewOverlay>().hide() } returns Unit
        with(callMock) {
            every { inputs } returns inputsMock
            every { activityClazz } returns activityMock::class.java
        }
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf())
        every { activityMock.requestOverlayPermission() } answers { }
    }

    @Test
    fun testBind() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        verify(exactly = 1) { applicationMock.registerActivityLifecycleCallbacks(screenShareOverlayProducer) }
        // check the two input jobs are active
        assertEquals(2, backgroundJob.children.count())
        assert(backgroundJob.children.all { it.isActive })
    }

    @Test
    fun testStop() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val backgroundJob = backgroundScope.coroutineContext[Job]!!
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        // check the two input jobs are active before stopping
        assertEquals(2, backgroundJob.children.count())
        assert(backgroundJob.children.all { it.isActive })
        screenShareOverlayProducer.dispose()
        verify(exactly = 1) { applicationMock.unregisterActivityLifecycleCallbacks(screenShareOverlayProducer) }
        // check the two input jobs are cancelled
        assert(backgroundJob.children.all { it.isCancelled })
    }

    @Test
    fun deviceScreenShareActive_overlayIsShown() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val screenMock = mockk<Input.Video.Screen.My>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.state } returns MutableStateFlow(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().show(activityMock) }
    }

    @Test
    fun deviceScreenShareActive_overlayPermissionRequested() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val screenMock = mockk<Input.Video.Screen.My>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.state } returns MutableStateFlow(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        verify(exactly = 1) { activityMock.requestOverlayPermission() }
    }

    @Test
    fun deviceScreenShareClosed_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val screenMock = mockk<Input.Video.Screen.My>()
        val state = MutableStateFlow<Input.State>(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.state } returns state
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        state.value = Input.State.Closed
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun deviceScreenShareIdle_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val screenMock = mockk<Input.Video.Screen.My>()
        val state = MutableStateFlow<Input.State>(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.state } returns state
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        state.value = Input.State.Idle
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun deviceScreenShareIsRemoved_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val screenMock = mockk<Input.Video.Screen.My>()
        val availableInputs = MutableStateFlow(setOf(screenMock))
        every { inputsMock.availableInputs } returns availableInputs
        every { screenMock.state } returns MutableStateFlow<Input.State>(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        availableInputs.value = setOf()
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun applicationScreenShareActive_overlayIsShown() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val applicationMock = mockk<Input.Video.Application>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.state } returns MutableStateFlow(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().show(activityMock) }
    }

    @Test
    fun applicationScreenShareIdle_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val applicationMock = mockk<Input.Video.Application>()
        val state = MutableStateFlow<Input.State>(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.state } returns state
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        state.value = Input.State.Idle
        verify(exactly = 1) {  anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun applicationScreenShareClosed_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val applicationMock = mockk<Input.Video.Application>()
        val state = MutableStateFlow<Input.State>(Input.State.Active)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.state } returns state
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        state.value = Input.State.Closed
        verify(exactly = 1) {  anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun applicationScreenShareIsRemoved_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val applicationMock = mockk<Input.Video.Application>()
        val availableInputs = MutableStateFlow(setOf(applicationMock))
        every { inputsMock.availableInputs } returns availableInputs
        every { applicationMock.state } returns MutableStateFlow<Input.State>(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        availableInputs.value = setOf()
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun cancelScopeWithOverlayActive_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenShareOverlayProducer = ScreenShareOverlayProducer(applicationMock, backgroundScope)
        val applicationMock = mockk<Input.Video.Application>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.state } returns MutableStateFlow(Input.State.Active)
        screenShareOverlayProducer.bind(callMock)
        screenShareOverlayProducer.onActivityCreated(activityMock, null)
        backgroundScope.cancel()
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

}