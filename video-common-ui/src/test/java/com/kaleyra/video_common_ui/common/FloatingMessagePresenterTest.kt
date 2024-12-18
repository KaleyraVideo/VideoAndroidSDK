@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.common

import com.kaleyra.video_common_ui.model.FloatingMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class FloatingMessagePresenterTest {

    @Test
    fun testFloatingMessagePresented() = runTest {
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(backgroundScope)

        floatingMessagePresenter.present(FloatingMessage("body"))

        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)
    }

    @Test
    fun testFloatingMessageDismissed() = runTest {
        val floatingMessage = FloatingMessage("body")
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.dismiss()
        advanceUntilIdle()

        Assert.assertEquals(null, floatingMessagePresenter.floatingMessages.first())
    }

    @Test
    fun testFloatingMessageUpdated() = runTest {
        val floatingMessage = FloatingMessage("body")
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.body = "body2"
        advanceUntilIdle()

        Assert.assertEquals("body2", floatingMessagePresenter.floatingMessages.first()?.body)
    }

    @Test
    fun testFloatingButtonUpdated() = runTest {
        val floatingMessage = FloatingMessage("body")
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.button = FloatingMessage.Button("title") { }
        advanceUntilIdle()

        Assert.assertEquals("title", floatingMessagePresenter.floatingMessages.first()?.button?.text)
    }

    @Test
    fun testFloatingButtonTextUpdated() = runTest {
        val floatingMessage = FloatingMessage("body", FloatingMessage.Button("title") { })
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.button?.text = "title2"
        advanceUntilIdle()

        Assert.assertEquals("title2", floatingMessagePresenter.floatingMessages.first()?.button?.text)
    }

    @Test
    fun testFloatingButtonIconUpdated() = runTest {
        val floatingMessage = FloatingMessage("body", FloatingMessage.Button("title", icon = -1) { })
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.button?.icon = 42
        advanceUntilIdle()

        Assert.assertEquals(42, floatingMessagePresenter.floatingMessages.first()?.button?.icon)
    }

    @Test
    fun testFloatingButtonActionUpdated() = runTest {
        val action = {}
        val action2 = {}
        val floatingMessage = FloatingMessage("body", FloatingMessage.Button("title", -1, action))
        val floatingMessagePresenter = CallUIFloatingMessagePresenter(this)
        floatingMessagePresenter.present(floatingMessage)
        Assert.assertEquals("body", floatingMessagePresenter.floatingMessages.first()?.body)

        floatingMessage.button?.action = action2
        advanceUntilIdle()

        Assert.assertEquals(action2, floatingMessagePresenter.floatingMessages.first()?.button?.action)
    }
}
