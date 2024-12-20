package com.kaleyra.video_common_ui.model

import org.junit.Test

class FloatingMessageTest {

    @Test
    fun floatingMessage_bodyUpdated_onBodyUpdatedInvoked() {
        val floatingMessage = FloatingMessage(body = "body")
        var onBodyUpdatedInvoked = false
        floatingMessage.onBodyUpdated = {
            onBodyUpdatedInvoked = true
        }

        floatingMessage.body = "body2"

        assert(onBodyUpdatedInvoked)
    }

    @Test
    fun floatingMessage_dismiss_onDismissInvoked() {
        val floatingMessage = FloatingMessage(body = "body")
        var onDismissedInvoked = false
        floatingMessage.onDismissed = {
            onDismissedInvoked = true
        }

        floatingMessage.dismiss()

        assert(onDismissedInvoked)
    }

    @Test
    fun floatingMessage_dismiss_callbacksRemoved() {
        val floatingMessage = FloatingMessage(body = "body")
        floatingMessage.onButtonUpdated = {}
        floatingMessage.onBodyUpdated = {}
        floatingMessage.onDismissed = {}

        floatingMessage.dismiss()

        assert(floatingMessage.onButtonUpdated == null)
        assert(floatingMessage.onBodyUpdated == null)
        assert(floatingMessage.onDismissed == null)
    }

    @Test
    fun floatingMessage_buttonUpdated_onButtonUpdatedInvoked() {
        val floatingMessage = FloatingMessage(body = "body", FloatingMessage.Button(text = "click", action = {}))
        var onButtonUpdatedInvoked = false
        floatingMessage.onButtonUpdated = {
            onButtonUpdatedInvoked = true
        }

        floatingMessage.button = FloatingMessage.Button(text ="click") { }

        assert(onButtonUpdatedInvoked)
    }

    @Test
    fun floatingMessageNoButton_buttonUpdated_onButtonUpdatedInvoked() {
        val floatingMessage = FloatingMessage(body = "body")
        var onButtonUpdatedInvoked = false
        floatingMessage.onButtonUpdated = {
            onButtonUpdatedInvoked = true
        }

        floatingMessage.button = FloatingMessage.Button(text = "click") { }

        assert(onButtonUpdatedInvoked)
    }

    @Test
    fun floatingMessage_buttonTextUpdated_onButtonTextUpdatedInvoked() {
        val floatingMessageButton = FloatingMessage.Button(text = "click", action = {})
        val floatingMessage = FloatingMessage(body = "body", floatingMessageButton)
        var onButtonTextUpdatedInvoked = false
        floatingMessage.onButtonUpdated = {
            onButtonTextUpdatedInvoked = true
        }

        floatingMessageButton.text = "test"

        assert(onButtonTextUpdatedInvoked)
    }

    @Test
    fun floatingMessage_buttonIconUpdated_onButtonIconUpdatedInvoked() {
        val floatingMessageButton = FloatingMessage.Button(text = "click", action = {})
        val floatingMessage = FloatingMessage(body = "body", floatingMessageButton)
        var onButtonTextUpdatedInvoked = false
        floatingMessage.onButtonUpdated = {
            onButtonTextUpdatedInvoked = true
        }

        floatingMessageButton.icon = 123

        assert(onButtonTextUpdatedInvoked)
    }

    @Test
    fun floatingMessage_buttonActionUpdated_onButtonUpdatedInvoked() {
        val floatingMessageButton = FloatingMessage.Button(text = "click", action = {})
        val floatingMessage = FloatingMessage(body = "body", floatingMessageButton)
        var onButtonTextUpdatedInvoked = false
        floatingMessage.onButtonUpdated = {
            onButtonTextUpdatedInvoked = true
        }

        floatingMessageButton.action = {}

        assert(onButtonTextUpdatedInvoked)
    }
}
