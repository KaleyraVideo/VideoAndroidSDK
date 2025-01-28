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

package com.kaleyra.video_common_ui.call

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraUIProvider
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class CallUITest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.ExecutorsKt")
        mockkStatic(Executors::class)
        every { Executors.newSingleThreadExecutor() } returns mockk {
            every { this@mockk.asCoroutineDispatcher() } returns object : ExecutorCoroutineDispatcher() {
                override val executor: Executor = Executor { command -> command.run() }
                override fun close() = Unit
                override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
            }
        }
        mockkObject(ContextExtensions)
        mockkObject(KaleyraUIProvider)
        mockkObject(ContextRetainer)
        mockkObject(AppLifecycle)
        every { ContextRetainer.context } returns context
        every { KaleyraUIProvider.startCallActivity(any()) } answers { }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun appInForeground_callShowSuccessful() {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        val success = callUI.show()
        assertEquals(true, success)
        verify(exactly = 1) { KaleyraUIProvider.startCallActivity(callUI.activityClazz) }
    }

    @Test
    fun appInBackground_callShowFailed() {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        val success = callUI.show()
        assertEquals(false, success)
        verify(exactly = 0) { KaleyraUIProvider.startCallActivity(callUI.activityClazz) }
    }

    @Test
    fun activityRunning_setDisplayModeSuccessful() {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        every { context.isActivityRunning(callUI.activityClazz) } returns true
        val success = callUI.change(CallUI.PresentationMode.PictureInPicture)
        val presentationMode = callUI.presentationModeEvent.replayCache.firstOrNull()?.presentationMode
        assertEquals(true, success)
        assertEquals(CallUI.PresentationMode.PictureInPicture, presentationMode)
    }

    @Test
    fun activityDestroyed_setDisplayModeFailed() {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        every { context.isActivityRunning(callUI.activityClazz) } returns false
        val success = callUI.change(CallUI.PresentationMode.PictureInPicture)
        val actualPresentationMode = callUI.presentationModeEvent.replayCache.firstOrNull()?.presentationMode
        assertEquals(false, success)
        assertEquals(null, actualPresentationMode)
    }

    @Test
    fun displayMessage_customMessageAdded() = runTest {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        val floatingMessage = FloatingMessage(body = "customMessage")

        callUI.present(floatingMessage)

        Assert.assertEquals(floatingMessage, callUI.floatingMessages.replayCache.firstOrNull())
    }

    @Test
    fun displayMessage_dismissMessage_customMessageRemoved() = runTest {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        val floatingMessage = FloatingMessage(body = "customMessage")

        callUI.present(floatingMessage)
        floatingMessage.dismiss()
        Assert.assertEquals(true, callUI.floatingMessages.replayCache.firstOrNull() == null)
    }

    @Test
    fun displayMessageA_dismissMessageB_customMessageNotRemoved() = runTest {
        val callUI = CallUI(call = mockk(relaxed = true), activityClazz = this::class.java)
        val floatingMessage1 = FloatingMessage(body = "customMessage1")
        val floatingMessage2 = FloatingMessage(body = "customMessage2")

        callUI.present(floatingMessage1)
        callUI.present(floatingMessage2)
        Assert.assertEquals(false, callUI.floatingMessages.replayCache.firstOrNull() == null)
    }

    @Test
    fun legacyActionsSet_callButtonsUpdated() = runTest {
        val callUI = CallUI(
            call = mockk(relaxed = true) {
                every { state } returns MutableStateFlow(Call.State.Disconnected)
            },
            activityClazz = this::class.java,
            actions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope)

        runCurrent()

        Assert.assertEquals(1, callUI.buttons.value.size)
        Assert.assertEquals(CallUI.Button.HangUp, callUI.buttons.value.first())
    }
}
