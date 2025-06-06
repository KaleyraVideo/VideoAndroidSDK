package com.kaleyra.video_sdk.call.virtualbackground.state

import com.kaleyra.video.conference.Call
import com.kaleyra.video_utils.dispatcher.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VirtualBackgroundStateManagerTest {

    private lateinit var stateManager: VirtualBackgroundStateManager
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testDispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        testDispatcherProvider = object : DispatcherProvider {
            override val default: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val main: CoroutineDispatcher = testDispatcher
            override val mainImmediate: CoroutineDispatcher = testDispatcher
        }

        stateManager = VirtualBackgroundStateManagerImpl.createForTesting(testDispatcherProvider)
    }

    @Test
    fun initialState_isVirtualBackgroundEnabled_isFalse() = runTest(testDispatcher) {
        Assert.assertEquals(
            false,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun setVirtualBackgroundEnabled_setsToTrue() = runTest(testDispatcher) {
        stateManager.setVirtualBackgroundEnabled(true)

        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun setVirtualBackgroundEnabled_setsToFalse() = runTest(testDispatcher) {
        stateManager.setVirtualBackgroundEnabled(true)
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        stateManager.setVirtualBackgroundEnabled(false)
        Assert.assertEquals(
            false,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun updateActiveCall_callEnded_setsIsVirtualBackgroundEnabledToFalse() = runTest(testDispatcher) {
        stateManager.setVirtualBackgroundEnabled(true)
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        val mockCall = mockk<Call>()
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { mockCall.state } returns callState
        stateManager.updateActiveCall(mockCall)

        callState.value = Call.State.Disconnected.Ended
        testDispatcher.scheduler.runCurrent()

        Assert.assertEquals(
            false,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun updateActiveCall_callConnected_doesNotChangeIsVirtualBackgroundEnabled() = runTest(testDispatcher) {
        stateManager.setVirtualBackgroundEnabled(true)
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        val mockCall = mockk<Call>()
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { mockCall.state } returns callState
        stateManager.updateActiveCall(mockCall)

        testDispatcher.scheduler.runCurrent()
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        callState.value = Call.State.Connecting
        testDispatcher.scheduler.runCurrent()
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @Test
    fun updateActiveCall_switchesObservationToNewCall() = runTest(testDispatcher) {
        stateManager.setVirtualBackgroundEnabled(true)

        val oldCall = mockk<Call>()
        val oldCallState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { oldCall.state } returns oldCallState
        stateManager.updateActiveCall(oldCall)

        testDispatcher.scheduler.runCurrent()
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        val newCall = mockk<Call>()
        val newCallState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { newCall.state } returns newCallState
        stateManager.updateActiveCall(newCall)
        oldCallState.value = Call.State.Disconnected.Ended

        testDispatcher.scheduler.runCurrent()
        Assert.assertEquals(
            true,
            stateManager.isVirtualBackgroundEnabled.value
        )

        newCallState.value = Call.State.Disconnected.Ended
        testDispatcher.scheduler.runCurrent()
        Assert.assertEquals(
            false,
            stateManager.isVirtualBackgroundEnabled.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun isVirtualBackgroundEnabled_emitsCorrectValues() = runTest(testDispatcher) {
        val collectedStates = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            stateManager.isVirtualBackgroundEnabled.toList(collectedStates)
        }
        testDispatcher.scheduler.runCurrent() // Collect initial value

        // Simulate changes
        stateManager.setVirtualBackgroundEnabled(true)
        testDispatcher.scheduler.runCurrent()

        stateManager.setVirtualBackgroundEnabled(false)
        testDispatcher.scheduler.runCurrent()

        val mockCall = mockk<Call>()
        val callState = MutableStateFlow<Call.State>(Call.State.Connected)
        every { mockCall.state } returns callState
        stateManager.updateActiveCall(mockCall)
        testDispatcher.scheduler.runCurrent() // Should not change value if it was already false

        stateManager.setVirtualBackgroundEnabled(true) // Re-enable for call test
        testDispatcher.scheduler.runCurrent()

        callState.value = Call.State.Disconnected.Ended
        testDispatcher.scheduler.runCurrent()

        Assert.assertEquals(
            listOf(false, true, false, true, false),
            collectedStates
        )
    }
}