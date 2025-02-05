@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.chat

import com.kaleyra.video_common_ui.ChatUI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ChatButtonsProviderTest {

    @Test
    fun testLegacyActionsMapped() = runTest {
        val callUIButtonsProvider = DefaultChatUIButtonsProvider(
            legacyActions = MutableStateFlow(setOf(ChatUI.Action.ShowParticipants)),
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(ChatUI.Button.Participants, callUIButtonsProvider.buttons.value.first())
    }

    @Test
    fun testLegacyActionsUpdatedButtonsUpdated() = runTest {
        val legacyActions: MutableStateFlow<Set<ChatUI.Action>> = MutableStateFlow(setOf(ChatUI.Action.ShowParticipants))
        val chatIButtonsProvider = DefaultChatUIButtonsProvider(
            legacyActions = legacyActions,
            scope = backgroundScope
        )
        runCurrent()
        Assert.assertEquals(ChatUI.Button.Participants, chatIButtonsProvider.buttons.value.first())

        legacyActions.value = setOf(ChatUI.Action.CreateCall())
        runCurrent()

        Assert.assertEquals(1, chatIButtonsProvider.buttons.value.size)
        Assert.assertEquals(true, chatIButtonsProvider.buttons.value.first() is ChatUI.Button.Call)
    }


    @Test
    fun testNoLegacyActionsDefaultButtonsSet() = runTest {
        val callUIButtonsProvider = DefaultChatUIButtonsProvider(scope = backgroundScope)

        runCurrent()

        Assert.assertEquals(ChatUI.Button.Collections.default, callUIButtonsProvider.buttons.value)
    }
}
