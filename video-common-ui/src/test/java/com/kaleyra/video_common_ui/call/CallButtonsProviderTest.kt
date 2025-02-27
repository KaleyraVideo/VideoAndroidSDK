@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class CallButtonsProviderTest {

    @Test
    fun testLegacyActionsMapped() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(CallUI.Button.HangUp, callUIButtonsProvider.buttons.value.first())
    }

    @Test
    fun testLegacyActionsUpdatedButtonsUpdated() = runTest {
        val legacyActions: MutableStateFlow<Set<CallUI.Action>> = MutableStateFlow(setOf(CallUI.Action.HangUp))
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = legacyActions,
            scope = backgroundScope
        )
        runCurrent()
        Assert.assertEquals(CallUI.Button.HangUp, callUIButtonsProvider.buttons.value.first())

        legacyActions.value = setOf(CallUI.Action.ToggleMicrophone)
        runCurrent()

        Assert.assertEquals(1, callUIButtonsProvider.buttons.value.size)
        Assert.assertEquals(CallUI.Button.Microphone, callUIButtonsProvider.buttons.value.first())
    }

    @Test
    fun testCallTypeAudioVideoCameraButtonsSet() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = null,
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(true, CallUI.Button.Collections.videoCall.all { it in callUIButtonsProvider.buttons.value })
    }

    @Test
    fun testCallTypeAudioUpgradableCameraButtonsSet() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioUpgradable()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = null,
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(true, CallUI.Button.Collections.videoCall.all { it in callUIButtonsProvider.buttons.value })
    }

    @Test
    fun testCallTypeAudioOnlyCameraButtonsSet() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioOnly()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = null,
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(true, CallUI.Button.Collections.audioCall.all { it in callUIButtonsProvider.buttons.value })
    }

    @Test
    fun testExternalButtonProviderReturnsValues() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioOnly()),
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = null,
            scope = backgroundScope
        )

        runCurrent()
        callUIButtonsProvider.buttonsProvider = {
            setOf()
        }

        Assert.assertEquals(true, callUIButtonsProvider.buttons.value.isEmpty())
    }

    @Test
    fun testCallTypeUpdatedButtonsUpdated() = runTest {
        val callType = MutableStateFlow<Call.Type>(Call.Type.audioOnly())
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = callType,
            callState = MutableStateFlow(Call.State.Disconnected),
            legacyActions = null,
            scope = backgroundScope
        )
        runCurrent()
        Assert.assertEquals(true, CallUI.Button.Collections.audioCall.all { it in callUIButtonsProvider.buttons.value })
        Assert.assertEquals(true, CallUI.Button.Collections.audioCall.size == callUIButtonsProvider.buttons.value.size)

        callType.value = Call.Type.audioVideo()
        runCurrent()

        Assert.assertEquals(true, CallUI.Button.Collections.videoCall.all { it in callUIButtonsProvider.buttons.value })
        Assert.assertEquals(true, CallUI.Button.Collections.videoCall.size == callUIButtonsProvider.buttons.value.size)
    }

    @Test
    fun testCallEndedLegacyActionsNotMapped() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = MutableStateFlow(Call.State.Disconnected.Ended),
            legacyActions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope
        )

        runCurrent()

        Assert.assertEquals(0, callUIButtonsProvider.buttons.value.size)
    }

    @Test
    fun testCallEndedButtonsProviderRemoved() = runTest {
        val callState: MutableStateFlow<Call.State> = MutableStateFlow(Call.State.Disconnected)
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = callState,
            legacyActions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope
        )
        runCurrent()

        callState.value = Call.State.Disconnected.Ended
        runCurrent()

        Assert.assertEquals(null, callUIButtonsProvider.buttonsProvider)
    }

    @Test
    fun testCallEndedButtonsSetToNullDuringCallDefaultButtonProviderSet() = runTest {
        val callState = MutableStateFlow(Call.State.Connected)
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = callState,
            legacyActions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope
        )
        runCurrent()

        callUIButtonsProvider.buttonsProvider = null
        runCurrent()

        Assert.assertEquals(true, callUIButtonsProvider.buttonsProvider != null)
    }

    @Test
    fun testCustomButtonsProviderInvocationUpdatedButtons() = runTest {
        val callUIButtonsProvider = DefaultCallUIButtonsProvider(
            callType = MutableStateFlow(Call.Type.audioVideo()),
            callState = MutableStateFlow(Call.State.Connected),
            legacyActions = MutableStateFlow(setOf(CallUI.Action.HangUp)),
            scope = backgroundScope
        )
        runCurrent()

        var count = 0
        callUIButtonsProvider.buttonsProvider = {
            count++
            if (count == 1) setOf(CallUI.Button.HangUp)
            else setOf(CallUI.Button.Camera)
        }
        callUIButtonsProvider.buttonsProvider?.invoke(mutableSetOf())
        runCurrent()

        Assert.assertEquals(CallUI.Button.Camera, callUIButtonsProvider.buttons.value.first())
    }
}
