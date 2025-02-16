package com.kaleyra.video_common_ui

import androidx.appcompat.app.AppCompatActivity
import com.kaleyra.video.conference.Call
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ConferenceUITest {

    @Test
    fun testLegacyCallActionAddedToCallAsCallButtons() = runTest {
        val conference = ConferenceUI(mockk(relaxed = true) {
            every { create(any<List<String>>(), any(), any()) } returns Result.success(mockk(relaxed = true) {
                every { type } returns MutableStateFlow(Call.Type.audioVideo())
                every { state } returns MutableStateFlow(Call.State.Disconnected)
            })
        }, callActivityClazz = AppCompatActivity::class.java)
        conference.callActions = setOf(CallUI.Action.HangUp)

        val call = conference.call(listOf("userB")).getOrThrow()
        call.buttons.first { it.contains(CallUI.Button.HangUp) }

        Assert.assertEquals(1, call.buttons.value.size)
        Assert.assertEquals(true, call.buttons.value.first() is CallUI.Button.HangUp)
    }
}