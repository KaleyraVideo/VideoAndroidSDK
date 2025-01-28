package com.kaleyra.video_common_ui.call

import com.kaleyra.video_common_ui.CallUI
import org.junit.Assert
import org.junit.Test

class CallUIButtonsTest {

    @Test
    fun callUIButtonCustom_textUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {})
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.text = "2"
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_iconUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {})
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.icon = 2
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_actionUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {})
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.action = {}
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_isEnabledUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, isEnabled = false)
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.isEnabled = true
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_badgeValueUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, badgeValue = 1)
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.badgeValue = 2
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_accessibilityLabelUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, accessibilityLabel = "accessibility")
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.accessibilityLabel = "updated"
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_appareanceBackgroundUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, appearance = CallUI.Button.Custom.Configuration.Appearance(1, 2))
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.appearance?.background = 2
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_appareanceTintUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, appearance = CallUI.Button.Custom.Configuration.Appearance(1, 2))
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.appearance?.tint = 3
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_appareanceUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {}, appearance = CallUI.Button.Custom.Configuration.Appearance(1, 2))
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config.appearance = CallUI.Button.Custom.Configuration.Appearance(2, 3)
        Assert.assertEquals(true, callbackCalled)
    }

    @Test
    fun callUIButtonCustom_configUpdated_callbackInvoked() {
        val customButton = CallUI.Button.Custom(
            CallUI.Button.Custom.Configuration(icon = 1, text = "1", action = {})
        )
        var callbackCalled = false
        customButton.onButtonUpdated = { callbackCalled = true }
        customButton.config = CallUI.Button.Custom.Configuration(icon = 1, text = "2", action = {})
        Assert.assertEquals(true, callbackCalled)
    }
}
